package task.scheduler.common;

import task.scheduler.exception.GraphException;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;

import java.util.*;

public class InputValidator {

    /**
     * Method validateGraph validates a task graph against the provided criteria.
     * The nodes must not be null, there must be exactly one start node, the graph must be acyclic,
     * the node labels must be unique.
     * @param graph to be validated
     * @throws GraphException if the graph is invalid.
     */
    public void validateGraph(IGraph graph) throws GraphException {
        // graph start node should not be null
        List<INode> startNodes = graph.getStartNodes();
        if (startNodes.isEmpty()) {
            throw new GraphException("start node cannot be empty");
        }

        // start node should have no parents
        for(INode node:startNodes){
            if (!node.getParents().isEmpty()) {
                throw new GraphException("start nodes should not have any parents");
            }
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
        for (INode node : nodes) {
            // node should not be null
            if (node == null) {
                throw new GraphException("node shouldn't be null");
            }

            if (node.getProcessingCost() < 1) {
                throw new GraphException("node processing cost cannot be less than 1");
            }

            // node labels should be unique
            String label = node.getLabel();
            if (labels.contains(label)) {
                throw new GraphException("node labels must be unique");
            } else {
                labels.add(label);
            }
        }

        // check for cycles
        if (isCyclic(graph)) {
            throw new GraphException("cycle detected in graph");
        }
    }

    /**
     * Detects cycles in digraphs
     *
     * @param graph the graph consider
     * @return whether the graph is cyclic or not
     */
    private boolean isCyclic(IGraph graph) {

        // initialise maps
        List<INode> nodes = graph.getNodes();
        Map<INode, Boolean> visited = new HashMap<>();
        Map<INode, Boolean> recStack = new HashMap<>();
        for (INode node : nodes) {
            visited.put(node, false);
            recStack.put(node, false);
        }

        // detect cycle in DFS trees starting from each node
        for (INode node : nodes) {
            if (isCyclicRecursive(node, visited, recStack)) {
                return true;
            }
        }

        // no cycle
        return false;
    }

    /**
     * Recursively called to detect cycles in DFS trees.
     * https://www.geeksforgeeks.org/detect-cycle-in-a-graph/
     *
     * @param node     current node
     * @param visited  map of whether nodes have been visited or not
     * @param recStack recursion stack used for detecting back edges. Nodes currently being considered.
     * @return whether there is a cycle in this sub problem
     */
    private boolean isCyclicRecursive(INode node, Map<INode, Boolean> visited, Map<INode, Boolean> recStack) {

        // node already in the recursion stack, cycle detected
        if (recStack.get(node)) {
            return true;
        }

        // node already visited
        if (visited.get(node)) {
            return false;
        }

        visited.put(node, true);
        recStack.put(node, true);

        // recursively call on children
        for (INode child : node.getChildren().keySet()) {
            if (isCyclicRecursive(child, visited, recStack)) {
                return true;
            }
        }

        // no cycle in this DFS tree
        recStack.put(node, false);
        return false;
    }
}
