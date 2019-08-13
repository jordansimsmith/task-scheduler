package task.scheduler.schedule.astar;

import task.scheduler.common.Config;
import task.scheduler.common.Tuple;
import task.scheduler.graph.INode;
import task.scheduler.schedule.ISchedule;

import java.util.*;

public class AStarSchedule implements ISchedule, Comparable<AStarSchedule> {

    private int maxBottomLevelCost;
    private int idleTime;
    private int[] earliestTimes;
    private int idleTimeHeuristicValue;
    private int heuristicValue;
    private final Set<String> scheduleString = new HashSet<>();

    private List<INode> free;
    private Map<INode, Tuple<Integer, Integer>> schedule;
    private Map<INode, Integer> parentCounter;

    private AStarSchedule() {
    }

    /**
     * Constructor for creating the initial state.
     *
     * @param free          List of nodes that can be immediately scheduled. i.e. start nodes of the graph.
     * @param parentCounter Map of whether children have unresolved dependencies. Initially in-degree for each node.
     */
    public AStarSchedule(List<INode> free, Map<INode, Integer> parentCounter) {
        this.free = free;
        this.parentCounter = parentCounter;
        this.schedule = new HashMap<>();
        this.earliestTimes = new int[Config.getInstance().getNumberOfCores()];
    }

    /**
     * Expands the partial solution, generating a new child by scheduling a given node on a given processor.
     *
     * @param node      to be scheduled.
     * @param processor to schedule the node on (1 indexed)
     * @return the newly created child.
     */
    public AStarSchedule expand(INode node, int processor) {
        AStarSchedule s = new AStarSchedule();

        int lastNodeStartTime = minStartTime(node, processor);

        s.maxBottomLevelCost = Math.max(this.maxBottomLevelCost, lastNodeStartTime + AStar.bottomLevelCache.get(node));
        s.idleTime = this.idleTime + lastNodeStartTime - this.earliestTimes[processor - 1];
        s.idleTimeHeuristicValue = (s.idleTime + AStar.totalNodeWeighting) / Config.getInstance().getNumberOfCores();

        s.earliestTimes = this.earliestTimes.clone();
        s.earliestTimes[processor - 1] = lastNodeStartTime + node.getProcessingCost();

        List<INode> free = new ArrayList<>(this.free);
        free.remove(node);
        Map<INode, Integer> parentCounter = new HashMap<>(this.parentCounter);
        for (INode child : node.getChildren().keySet()) {
            // decrement unresolved dependencies to child
            int count = parentCounter.get(child);
            parentCounter.put(child, --count);

            // is now free
            if (count == 0) {
                free.add(child);
            }
        }

        Map<INode, Tuple<Integer, Integer>> schedule = new HashMap<>(this.schedule);
        schedule.put(node, new Tuple<>(lastNodeStartTime, processor));

        s.free = free;
        s.parentCounter = parentCounter;
        s.schedule = schedule;

        s.heuristicValue = Math.max(s.maxBottomLevelCost, s.idleTimeHeuristicValue);
        s.populateScheduleString();

        return s;
    }

    private int minStartTime(INode node, int processor) {
        int startTime = 0;
        for (Map.Entry<INode, Integer> entry : node.getParents().entrySet()) {
            INode parent = entry.getKey();
            Tuple<Integer, Integer> nodeSchedule = this.schedule.get(parent);

            if (nodeSchedule.y != processor) {
                // parent on different processor
                startTime = Math.max(startTime, nodeSchedule.x + parent.getProcessingCost() + entry.getValue());
            }
            startTime = Math.max(startTime, this.earliestTimes[processor]);
        }
        return startTime;
    }

    private void populateScheduleString(){
        StringJoiner[] joiners = new StringJoiner[Config.getInstance().getNumberOfCores()];

        // for each processor add node scheduled
        for(Map.Entry<INode, Tuple<Integer, Integer>> entry : schedule.entrySet()){
            joiners[entry.getValue().y].add(String.valueOf(entry.getValue().x)).add(entry.getKey().getLabel());
        }

        // add strings to cached set
        for(StringJoiner joiner : joiners){
            scheduleString.add(joiner.toString());
        }
    }

    @Override
    public Tuple<Integer, Integer> getNodeSchedule(INode node) {
        return this.schedule.get(node);
    }

    @Override
    public int getTotalCost() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int compareTo(AStarSchedule o) {
        return Integer.compare(this.heuristicValue, o.heuristicValue);
    }
    
    @Override
    public boolean equals(Object o) {
        if(o instanceof AStarSchedule){
            return this.scheduleString.equals(((AStarSchedule) o).scheduleString);
        }
        return false;
    }
}
