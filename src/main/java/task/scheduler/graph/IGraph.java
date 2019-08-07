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
     * Method getStartNodes gets the start node for the graph. There is always only one start node for a valid graph.
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
}
