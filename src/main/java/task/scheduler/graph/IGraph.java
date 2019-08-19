package task.scheduler.graph;

import java.util.List;

/**
 * IGraph represents the collection of INodes, which makes up the input graph to the scheduling.
 * IGraph's are immutable, the problem should not be modified by the scheduler.
 */
public interface IGraph {
    /**
     * Method getNodes is returns a list of all the nodes in this graph.
     *
     * @return List of INodes
     */
    List<INode> getNodes();

    /**
     * Method getStartNodes gets the start nodes for the graph.
     * There may be multiple start nodes in a graph. 
     *
     * @return start INode
     */
    List<INode> getStartNodes();

    /**
     * Method getNodeCount gets the number of nodes in this graph.
     *
     * @return an int for the number of nodes.
     */
    int getNodeCount();

    /**
     * Returns the theoretical upper bound on the number of schedules that could be searched for this graph
     * @return logged double of the result
     */
    double getSchedulesUpperBoundLog();
}
