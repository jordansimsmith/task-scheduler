package task.scheduler;

import task.scheduler.exception.GraphException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        // graph node list should not be null
        List<INode> nodes = graph.getNodes();
        if (nodes == null) {
            throw new GraphException("node list should not be null");
        }

        // graph node count should match length of node list
        if (graph.getNodeCount() != nodes.size()) {
            throw new GraphException("node count doesn't match node list size");
        }

        // iterate and validate each node
        Set<String> labels = new HashSet<>();
        for (INode node: nodes) {
            // node should not be null
            if (node == null) {
                throw new GraphException("node shouldn't be null");
            }

            if (node.getProcessingCost() < 1) {
                throw new GraphException("node processing cost cannot be less than 1");
            }

            // node should have at least one parent
            if (node != startNode && node.getParents().isEmpty()) {
                throw new GraphException("there should only be one start node");
            }

            // node should have at least one child
            if (node != endNode && node.getChildren().isEmpty()) {
                throw new GraphException("there should be only one end node");
            }

            // node labels should be unique
            String label = node.getLabel();
            if (labels.contains(label)) {
                throw new GraphException("node labels must be unique");
            } else {
                labels.add(label);
            }

        }
    }
}
