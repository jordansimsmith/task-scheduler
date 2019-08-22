package task.scheduler.schedule;

import task.scheduler.common.Config;
import task.scheduler.common.Tuple;
import task.scheduler.graph.INode;

import java.util.*;

public class Schedule implements ISchedule, Comparable<Schedule> {

    public Schedule parent;
    private int maxBottomLevelCost;
    private int idleTime;
    private int[] earliestTimes;
    private int idleTimeHeuristicValue;
    private int heuristicValue;
    private String scheduleString = "";

    private List<INode> free;
    private Map<INode, Tuple<Integer, Integer>> schedule;
    private Map<INode, Integer> parentCounter;
    private Set<Schedule> childStates;
    private List<INode> fixedTaskOrderings = new ArrayList<>();

    private int scheduledNodeCount;

    private Schedule() {
    }

    /**
     * Constructor for creating the initial state.
     *
     * @param free          List of nodes that can be immediately scheduled. i.e. start nodes of the graph.
     * @param parentCounter Map of whether children have unresolved dependencies. Initially in-degree for each node.
     */
    public Schedule(List<INode> free, Map<INode, Integer> parentCounter) {
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
    public Schedule expand(INode node, int processor) {
        Schedule s = new Schedule();

        int lastNodeStartTime = minStartTime(node, processor);
        s.maxBottomLevelCost = Math.max(this.maxBottomLevelCost, lastNodeStartTime + SchedulerCache.bottomLevelCache.get(node));
        s.idleTime = this.idleTime + lastNodeStartTime - this.earliestTimes[processor - 1];
        s.idleTimeHeuristicValue = (s.idleTime + SchedulerCache.totalNodeWeighting) / Config.getInstance().getNumberOfCores();

        s.earliestTimes = this.earliestTimes.clone();
        s.earliestTimes[processor - 1] = lastNodeStartTime + node.getProcessingCost();

        List<INode> free = new ArrayList<>(this.free);
        free.remove(node);

        fixedTaskOrderings.remove(node);

        int sizeBefore = free.size();

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

        s.scheduledNodeCount = scheduledNodeCount + 1;
        Map<INode, Tuple<Integer, Integer>> schedule = new HashMap<>(this.schedule);
        schedule.put(node, new Tuple<>(lastNodeStartTime, processor));

        s.free = free;
        s.parentCounter = parentCounter;
        s.schedule = schedule;

        s.fixedTaskOrderings = new ArrayList<>(this.fixedTaskOrderings);
        s.recalculateFixedTaskOrderings();

        s.heuristicValue = Math.max(s.maxBottomLevelCost, s.idleTimeHeuristicValue);
        s.populateScheduleString();

        return s;
    }

    /**
     * Returns the earliest time at which the given node can be scheduled on the given processor.
     *
     * @param node      to be scheduled
     * @param processor on which the node would be scheduled
     * @return earliest time available at which the node can be scheduled
     */
    private int minStartTime(INode node, int processor) {
        int startTime = 0;
        for (Map.Entry<INode, Integer> entry : node.getParents().entrySet()) {
            INode parent = entry.getKey();
            Tuple<Integer, Integer> nodeSchedule = this.schedule.get(parent);

            if (nodeSchedule.y != processor) {
                // parent on different processor
                startTime = Math.max(startTime, nodeSchedule.x + parent.getProcessingCost() + entry.getValue());
            }
        }
        startTime = Math.max(startTime, earliestTimes[processor - 1]);
        return startTime;
    }

    /**
     * Populates the scheduleString field by converting the schedule field into a string.
     * The schedule field allows for a more efficient equality check of Schedule and
     * thus faster duplicate detection.
     */
    private void populateScheduleString() {
        StringJoiner joiner = new StringJoiner(" ");
        for (INode node : SchedulerCache.sortedNodes) {
            Tuple<Integer, Integer> nodeSchedule = this.schedule.get(node);
            if (nodeSchedule != null) {
                joiner.add(node.getLabel() + " " + nodeSchedule.x);
            }
        }

        this.scheduleString = joiner.toString();
    }

    public Set<Schedule> getChildStates() {
        if(childStates == null) {
            childStates = new HashSet<>();

            if(!fixedTaskOrderings.isEmpty()){
                INode node = fixedTaskOrderings.get(0);
                for (int i = 1; i <= Config.getInstance().getNumberOfCores(); i++) {
                    childStates.add(expand(node, i));
                }

            } else {
                for (INode node : this.getFree()) {
                    for (int i = 1; i <= Config.getInstance().getNumberOfCores(); i++) {
                        childStates.add(expand(node, i));
                    }
                }
            }
        }
        return childStates;

    }

    private void recalculateFixedTaskOrderings(){
        if(fixedTaskOrderable()) {
            this.fixedTaskOrderings = this.getFree();
            sortFixedTaskOrderings();
        }
    }

    private boolean fixedTaskOrderable() {
        INode sharedChild = null;
        int sharedParentProcessor =  -2;

        for(INode node : this.getFree()) {

            // 1. must all have at most one parent and at most one child
            if(node.getParents().size() > 1 || node.getChildren().size() > 1) {
                return false;
            }

            // 2. if node has a child, then it must be the same child as for any other task in free
            if(node.getChildren().size() == 1) {

                if (sharedChild == null) {

                    sharedChild = (INode)node.getChildren().keySet().toArray()[0];

                } else {

                    if(!node.getChildren().containsKey(sharedChild)){
                        return false;
                    }
                }
            }

            // 3. if node has a parent, then all other parents of nodes in free must be allocated to the same processor
            if(node.getParents().size() == 1){
                int parentProcessor = getNodeSchedule((INode)node.getParents().keySet().toArray()[0]).y;
                if (sharedParentProcessor == -2){

                    sharedParentProcessor = parentProcessor;
                } else {

                    if (sharedParentProcessor != parentProcessor) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void sortFixedTaskOrderings() {
        Collections.sort(fixedTaskOrderings, (node1, node2) -> {
            // 1.  Sort tasks of free by their non-decreasing data ready time.
            //     Data ready time is the finish time of the nodes parent plus the weight of the edge in between.
            int drtComp = Integer.compare(getDataReadyTime(node1), getDataReadyTime(node2));

            if (drtComp != 0) {
                return drtComp;
            }

            // 2.  Break ties by sorting according to non-increasing out-edge costs.
            //     If there is no out-edge, set cost to zero
            return Integer.compare(getOutEdgeCost(node2), getOutEdgeCost(node1));
        });

        //  Verify that tasks of free are in non-increasing out-edge cost order
        if (!verifyFixedTaskOrder()){
            fixedTaskOrderings.clear();
        }
    }

    private boolean verifyFixedTaskOrder() {
        for(int i = 1; i < fixedTaskOrderings.size(); i++) {
            if(Integer.compare(getOutEdgeCost(fixedTaskOrderings.get(i - 1)), getOutEdgeCost(fixedTaskOrderings.get(i))) < 0) {
                return false;
            }
        }
        return true;
    }

    // assumption: node has at most one parent
    private int getDataReadyTime(INode node) {
        if (node.getParents().size() > 0) {

            // TODO: could optimise this key retrieval?
            INode parent = (INode)node.getParents().keySet().toArray()[0];
            return getNodeSchedule(parent).x + parent.getProcessingCost();
        }
        return 0;
    }

    // assumption: node has at most one child
    private int getOutEdgeCost(INode node) {
        if (node.getChildren().size() > 0) {
            return (int) node.getChildren().values().toArray()[0];
        }
        return 0;
    }

    public int getScheduledNodeCount() {
        return scheduledNodeCount;
    }

    public List<INode> getFree() {
        return free;
    }

    public String getScheduleString() {
        return this.scheduleString;
    }

    @Override
    public Tuple<Integer, Integer> getNodeSchedule(INode node) {
        return this.schedule.get(node);
    }

    @Override
    public int getTotalCost() {
        // the use of streams here is appropriate as this method is only called once
        return Arrays.stream(earliestTimes).max().getAsInt();
    }

    @Override
    public int compareTo(Schedule o) {
        return Integer.compare(this.heuristicValue, o.heuristicValue);
    }

    @Override
    public int hashCode() {
        return this.scheduleString.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Schedule) {
            return this.scheduleString.equals(((Schedule) o).scheduleString);
        }
        return false;
    }

    public int getHeuristicValue() {
        return heuristicValue;
    }
}
