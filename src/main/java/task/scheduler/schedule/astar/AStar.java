package task.scheduler.schedule.astar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import task.scheduler.common.Config;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;

import java.util.*;

public class AStar implements IScheduler {
    private static final Logger logger = LoggerFactory.getLogger(AStar.class);

    public static int totalNodeWeighting;
    public static final Map<INode, Integer> bottomLevelCache = new HashMap<>();
    public static final List<INode> sortedNodes = new ArrayList<>();

    private ISchedule currentSchedule;
    private int schedulesSearched;

    public AStar() {
    }

    @Override
    public ISchedule execute(IGraph graph) {
        populateTotalNodeWeighting(graph);
        populateBottomLevelCache(graph);
        populateSortedNodes(graph);

        PriorityQueue<AStarSchedule> open = new PriorityQueue<>();
        Set<String> closed = new HashSet<>();

        open.add(new AStarSchedule(graph.getStartNodes(), getParentCountMap(graph)));
        int searchCount = 0;

        while (!open.isEmpty()) {
            AStarSchedule s = open.peek();
            open.remove(s);

            if (s.getScheduledNodeCount() == graph.getNodeCount()) {
                logger.info(searchCount + " states searched");
                return s; // optimal schedule found
            }

            for (INode node : s.getFree()) {
                for (int i = 1; i <= Config.getInstance().getNumberOfCores(); i++) {
                    AStarSchedule child = s.expand(node, i);

                    // do not add duplicate states to the priority queue
                    if (!closed.contains(child.getScheduleString())) {
                        open.add(child);
                        closed.add(child.getScheduleString());
                        this.schedulesSearched++;
                        this.currentSchedule = child;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Populates the sortedNodes field with all INodes of the given graph sorted according to their
     * node label.
     *
     * @param graph of which to populate the sortedNodes field with
     */
    private void populateSortedNodes(IGraph graph) {
        // the use of streams here is justified because it is only called once
        graph.getNodes().stream().sorted(Comparator.comparing(INode::getLabel)).forEachOrdered(sortedNodes::add);
    }

    /**
     * Returns a map of INode to a parent count Integer. The parent count Integer represents the
     * remaining number of parents the INode has. The map contains this information for all INodes
     * of the given IGraph.
     *
     * @param graph for which to calculate the parentCountMap
     * @return a parentCountMap
     */
    private Map<INode, Integer> getParentCountMap(IGraph graph) {
        Map<INode, Integer> parentCount = new HashMap<>();

        for (INode node : graph.getNodes()) {
            parentCount.put(node, node.getParents().size());
        }
        return parentCount;
    }

    /**
     * Calculates and populates the totalNodeWeighting field with sum of the
     * processing costs of the INodes in the given IGraph.
     *
     * @param graph of which to calculate the total node weighting
     */
    private void populateTotalNodeWeighting(IGraph graph) {
        for (INode node : graph.getNodes()) {
            totalNodeWeighting += node.getProcessingCost();
        }
    }


    /**
     * Populates the bottomLevelCache field using the given IGraph object. The bottomLevelCache
     * is a map of all INodes of the graph to their bottom level Integer. The bottom level of an
     * INode is the critical path from the INode.
     *
     * @param graph the IGraph of which to calculate the bottomLevelCache
     */
    private void populateBottomLevelCache(IGraph graph) {
        bottomLevelCache.clear();
        for (INode node : graph.getNodes()) {
            computeCriticalPath(node, graph);
        }
    }


    /**
     * computeCriticalPath is used to compute the greatest cost path from the provided source node.
     * It updates the critical paths field with the maxmimal cost path.
     * https://www.geeksforgeeks.org/find-longest-path-directed-acyclic-graph/
     *
     * @param source node to be considered
     */
    private void computeCriticalPath(INode source, IGraph graph) {
        List<INode> nodes = graph.getNodes();

        Map<INode, Integer> distances = new HashMap<>();
        Map<INode, Boolean> visited = new HashMap<>();
        Stack<INode> stack = new Stack<>();

        // Initialise distances to all vertices as negative infinity
        // distances to source node are zero
        for (INode node : nodes) {
            distances.put(node, Integer.MIN_VALUE);
            visited.put(node, false);
        }
        distances.put(source, source.getProcessingCost());

        // topologically sort nodes into the stack
        for (INode node : nodes) {
            if (!visited.get(node)) {
                topologicalSortUtil(node, visited, stack);
            }
        }

        // process nodes in topological order
        while (!stack.empty()) {
            INode node = stack.pop();

            if (distances.get(node) != Integer.MIN_VALUE) {
                // iterate over node children
                for (INode child : node.getChildren().keySet()) {

                    // update distances if greater
                    if (distances.get(child) < distances.get(node) + child.getProcessingCost()) {
                        distances.put(child, distances.get(node) + child.getProcessingCost());
                    }
                }
            }
        }

        int max = 0;

        for (int distance : distances.values()) {
            max = Math.max(max, distance);
        }

        bottomLevelCache.put(source, max);
    }

    /**
     * Topological sort recursive function
     *
     * @param node    current node
     * @param visited map of whether nodes have been visited or not
     * @param stack   stack to push topological ordering to
     */
    private void topologicalSortUtil(INode node, Map<INode, Boolean> visited, Stack<INode> stack) {
        // mark current node as visited
        visited.put(node, true);

        // recursively call topological sort on unvisited nodes
        for (Map.Entry<INode, Integer> child : node.getChildren().entrySet()) {
            if (!visited.get(child.getKey())) {
                topologicalSortUtil(child.getKey(), visited, stack);
            }
        }

        stack.push(node);
    }

    @Override
    public ISchedule getCurrentSchedule() {
        return this.currentSchedule;
    }

    @Override
    public int getSchedulesSearched() {
        return this.schedulesSearched;
    }
}
