package task.scheduler;

import task.scheduler.exception.DotFormatException;
import task.scheduler.exception.DotNodeMissingException;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Encapsulates the loading and storage of a graph from a Dot File
 * TODO: Needs to handle things like DotFormat comments, etc
 * TODO: Can nodes have multiple char names?
 */
public class Graph implements IGraph {
    private List<Node> nodes;
    private Node startNode;

    public Graph(File inputFile) throws IOException, DotFormatException {
        this.loadGraphFromDotFile(inputFile);
    }

    private void loadGraphFromDotFile(File inputFile) throws IOException, DotFormatException {
        nodes = new LinkedList<>();

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        String line = reader.readLine();
        while (line != null) {
            this.readDotFileLine(line);
            line = reader.readLine();
        }

        // Find start and end nodes
        for (Node node : nodes) {
            if (node.getParents().size() == 0) {
                if (startNode != null) {
                    throw new DotFormatException("Found two start nodes labelled " + startNode.getLabel() + " and " + node.getLabel());
                }

                startNode = node;
            }
        }

        // Check start/end nodes
        if (startNode == null) {
            throw new DotNodeMissingException("No start node");
        }

        for (Node node : nodes) {
            System.out.println(node);
        }
    }

    private void readDotFileLine(String line) throws DotFormatException {
        line = line.replaceAll("\\s", "");

        if (line.startsWith("digraph")) {
            return;
        }

        // Find tasks
        if (line.matches("^[a-z]\\[(.*)Weight=\\d+(.*)")) {
            nodes.add(new Node(Integer.parseInt(line.replaceAll("[^0-9]", "")),
                    line.substring(0, 1)));
        }

        // Find dependencies
        if (line.matches("^[a-z]->[a-z]\\[(.*)Weight=\\d+(.*)")) {
            Node dependent = getNodeByLabel(line.substring(3, 4));
            Node parent = getNodeByLabel(line.substring(0, 1));

            addDependency(parent, dependent, Integer.parseInt(line.replaceAll("[^0-9]", "")));
        }
    }

    private Node getNodeByLabel(String label) throws DotNodeMissingException {
        for (Node node : nodes) {
            if (node.getLabel().equals(label)) {
                return node;
            }
        }

        throw new DotNodeMissingException("Node " + label + " could not be found");
    }

    /**
     * Adds a dependency information to both sides of a dependency in the Graph
     */
    private void addDependency(Node parent, Node dependent, int cost) {
        parent.addDependent(dependent, cost);
        dependent.addDependency(parent, cost);
    }

    @Override
    public List<INode> getNodes() {
        return new LinkedList<INode>(nodes);
    }

    @Override
    public INode getStartNode() {
        return startNode;
    }

    @Override
    public int getNodeCount() {
        return nodes.size();
    }
}
