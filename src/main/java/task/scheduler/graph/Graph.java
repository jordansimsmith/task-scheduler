package task.scheduler.graph;

import task.scheduler.common.Config;
import task.scheduler.common.Triplet;
import task.scheduler.exception.DotFormatException;
import task.scheduler.exception.DotNodeMissingException;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates the loading and storage of a graph from a Dot File
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

        List<Triplet<String, String, Integer>> edges = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            this.readDotFileLine(line, edges);
            line = reader.readLine();
        }

        // Add dependencies to graph
        for (Triplet<String, String, Integer> edge : edges)    {
            Node dependent = getNodeByLabel(edge.x);
            Node parent = getNodeByLabel(edge.y);

            addDependency(parent, dependent, edge.z);
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

    private void readDotFileLine(String line, List<Triplet<String, String, Integer>> edges) throws DotFormatException {
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
            edges.add(new Triplet<String, String, Integer>(m.group(2), m.group(1), Integer.parseInt(m.group(4))));
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
        return new LinkedList<>(nodes);
    }

    @Override
    public List<INode> getStartNodes() {
        return new ArrayList<>(startNodes);
    }

    @Override
    public int getNodeCount() {
        return nodes.size();
    }

    /**
     * Log of the upper bound on the number of potential schedules
     * Does the arithmetic in log (log(a*b) = log(a) + log(b) to avoid dealing with really large numbers
     * @return
     */
    @Override
    public double getSchedulesUpperBoundLog() {
        double P = Config.getInstance().getNumberOfCores();
        int n = nodes.size();
        double nFactorialLog = 0;

        while (n > 0)   {
            nFactorialLog +=  Math.log(n);
            n--;
        }

        return Math.log(Math.pow(P, nodes.size())) + nFactorialLog;
    }

    @Override
    public BigDecimal getSchedulesUpperBound() {
        int P = Config.getInstance().getNumberOfCores();
        int n = nodes.size();
        BigDecimal nFactorial = BigDecimal.valueOf(1);

        while (n > 0)   {
            nFactorial = BigDecimal.valueOf(n).multiply(nFactorial);
            n--;
        }
        return nFactorial.multiply( BigDecimal.valueOf( (int) Math.pow(P, n)));
    }


}
