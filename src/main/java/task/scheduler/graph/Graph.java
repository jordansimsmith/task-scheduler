package task.scheduler.graph;

import task.scheduler.exception.DotFormatException;
import task.scheduler.exception.DotNodeMissingException;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates the loading and storage of a graph from a Dot File
 * TODO: Needs to handle things like DotFormat comments, etc
 * TODO: Can nodes have multiple char names?
 */
public class Graph implements IGraph {
    private List<Node> nodes;
    private List<Node> startNodes;

    private Pattern nodeMatcher, edgeMatcher;


    public Graph(File inputFile) throws IOException, DotFormatException {
        this.startNodes = new ArrayList<>();
        nodeMatcher = Pattern.compile("^([a-zA-Z0-9_]*)\\[(.*)Weight=(\\d+)(.*)");
        edgeMatcher = Pattern.compile("^([a-zA-Z0-9_]*)->([a-zA-Z0-9_]*)\\[(.*)Weight=(\\d+)(.*)");

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

        // Find start nodes
        for (Node node : nodes) {
            if (node.getParents().size() == 0) {
                startNodes.add(node);
            }
        }

        // Check start/end nodes
        if (startNodes.isEmpty()) {
            throw new DotNodeMissingException("No start node");
        }
    }

    private void readDotFileLine(String line) throws DotFormatException {
        line = line.replaceAll("\\s", "");

        if (line.startsWith("digraph")) {
            return;
        }

        // Find tasks
        Matcher m = nodeMatcher.matcher(line);
        if (m.matches()) {
            nodes.add(new Node(Integer.parseInt(m.group(3)),
                    m.group(1)));
        }

        // Find dependencies
        m = edgeMatcher.matcher(line);
        if (m.matches()) {
            Node dependent = getNodeByLabel(m.group(2));
            Node parent = getNodeByLabel(m.group(1));

            addDependency(parent, dependent, Integer.parseInt(m.group(4)));
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
    public List<INode> getStartNodes() {
        return new ArrayList<INode>(startNodes);
    }

    @Override
    public int getNodeCount() {
        return nodes.size();
    }
}
