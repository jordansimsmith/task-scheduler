package task.scheduler;

/**
 * ISchedule represents a schedule for a graph on a number of processors.
 * A schedule consists of a start time and a processor for each node of the graph.
 */
public interface ISchedule {
    /**
     * Method getNodeSchedule returns the start time and allocated processor for a node in a graph.
     *
     * @param node INode of the schedule information to return.
     * @return A 2-tuple of the start time Integer and processor number Integer.
     */
    public Tuple<Integer, Integer> getNodeSchedule(INode node);
}