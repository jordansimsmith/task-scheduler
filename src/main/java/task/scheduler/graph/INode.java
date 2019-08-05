package task.scheduler.graph;

import task.scheduler.common.Tuple;

import java.util.List;

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
    public List<Tuple<INode, Integer>> getChildren();

    /**
     * Method getParents is similar to getChildren; retrieving all incoming edges to this node.
     * Edges are stored as 2-tuples of the source node and the path weighting of the edge.
     *
     * @return A list of edge tuples.
     */
    public List<Tuple<INode, Integer>> getParents();

    /**
     * Method getProcessingCost is a getter for the processing cost associated with this node.
     * i.e. how long will it take to process this node on any processor.
     *
     * @return an int corresponding to the processing cost.
     */
    public int getProcessingCost();

    /**
     * Method get getLabel is a getter for the human-readable identifier for this node.
     * E.g. "a", "b" etc.
     *
     * @return a string containing the node identifier/label.
     */
    public String getLabel();
}
