package task.scheduler.schedule;

import task.scheduler.common.Config;
import task.scheduler.common.Tuple;
import task.scheduler.graph.INode;

import java.util.*;

public class Schedule implements ISchedule, Comparable<Schedule> {

    private int maxBottomLevelCost;
    private int idleTime;
    private int[] earliestTimes;
    private int idleTimeHeuristicValue;
    private int heuristicValue;
    private String scheduleString = "";

    private List<INode> free;
    private Map<INode, Tuple<Integer, Integer>> schedule;
    private Map<INode, Integer> parentCounter;

    private int scheduledNodeCount;
    private int totalCost;
    private int hashCode;

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

        s.heuristicValue = Math.max(s.maxBottomLevelCost, s.idleTimeHeuristicValue);
        s.populateScheduleString();
        s.populateTotalCost();
        s.populateHashCode();

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

    private void populateTotalCost() {
        // the use of streams here is appropriate as this method is only called once
        this.totalCost = Arrays.stream(earliestTimes).max().getAsInt();
    }

    private void populateHashCode() {
        this.hashCode = this.scheduleString.hashCode();
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
        return this.totalCost;
    }

    @Override
    public int compareTo(Schedule o) {
        return Integer.compare(this.heuristicValue, o.heuristicValue);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
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
