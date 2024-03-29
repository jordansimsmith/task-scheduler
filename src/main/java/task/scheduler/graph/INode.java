package task.scheduler.graph;

import java.util.Map;

/**
 * INode represents a node in the input graph. INode's are immutable, they should not be modified by the scheduler.
 */
public interface INode {
    /**
     * Method getChildren is used to retrieve the outgoing edges from this node.
     * The edge is stored as a 2-tuple, containing the destination node and the path weighting to that node.
     * The path weighting represents the cost of switching to a new processor.
     *
     * @return A List of edge tuples.
     */
    Map<INode, Integer> getChildren();

    /**
     * Method getParents is similar to getChildren; retrieving all incoming edges to this node.
     * Edges are stored as 2-tuples of the source node and the path weighting of the edge.
     *
     * @return A list of edge tuples.
     */
    Map<INode, Integer> getParents();

    /**
     * Method getProcessingCost is a getter for the processing cost associated with this node.
     * i.e. how long will it take to process this node on any processor.
     *
     * @return an int corresponding to the processing cost.
     */
    int getProcessingCost();

    /**
     * Method get getLabel is a getter for the human-readable identifier for this node.
     * Consistent with input.
     *
     * @return a string containing the node identifier/label.
     */
    String getLabel();
}
