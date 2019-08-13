package task.scheduler.schedule.astar;

import task.scheduler.common.Config;
import task.scheduler.common.Tuple;
import task.scheduler.graph.INode;
import task.scheduler.schedule.ISchedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AStarSchedule implements ISchedule {

    private AStarSchedule parent;

    private int maxBottomLevelCost;
    private INode lastNode;
    private int lastNodeProcessor;
    private int lastNodeStartTime;

    private int idleTime;
    private int[] earliestTimes;
    private int idleTimeHeuristicValue;

    private List<INode> free;
    private Map<INode, Tuple<Integer, Integer>> schedule;
    private Map<INode, Integer> parentCounter;

    public AStarSchedule() {
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

        s.parent = this;
        s.maxBottomLevelCost = Math.max(this.maxBottomLevelCost, lastNodeStartTime + AStar.bottomLevelCache.get(node));
        s.lastNode = node;
        s.lastNodeProcessor = processor;
        s.lastNodeStartTime = lastNodeStartTime;
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

    @Override
    public Tuple<Integer, Integer> getNodeSchedule(INode node) {
        return this.schedule.get(node);
    }

    @Override
    public int getTotalCost() {
        throw new RuntimeException("not implemented");
    }
}
