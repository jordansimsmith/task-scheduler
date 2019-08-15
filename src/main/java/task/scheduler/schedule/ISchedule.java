package task.scheduler.schedule;

import task.scheduler.common.Tuple;
import task.scheduler.graph.INode;

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
    Tuple<Integer, Integer> getNodeSchedule(INode node);

    /**
     * Method getTotalCost gets the total cost of the schedule.
     *
     * @return int for the total cost.
     */
    int getTotalCost();
}