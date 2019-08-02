package task.scheduler;

import task.scheduler.exception.GraphException;

public class InputValidator {

    public void validateGraph(IGraph graph) throws GraphException {
        // graph start node should not be null
        INode startNode = graph.getStartNode();
        if (startNode == null) {
            throw new GraphException("start node cannot be null");
        }

        // start node should have no parents
        if (!startNode.getParents().isEmpty()) {
            throw new GraphException("start node should not have any parents");
        }

        // end node should not be null
        INode endNode = graph.getEndNode();
        if (endNode == null) {
            throw new GraphException("end node should not be null");
        }

        // end node should not have any children
        if (!endNode.getChildren().isEmpty()) {
            throw new GraphException("end node should not have any children");
        }
    }
}
