package task.scheduler.schedule.astar;

import task.scheduler.common.Config;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;

import java.util.*;
import java.util.stream.Collectors;

public class AStar implements IScheduler {
    public static int totalNodeWeighting;
    public static final Map<INode, Integer> bottomLevelCache = new HashMap<>();
    public static final List<INode> sortedNodes = new ArrayList<>();

    public AStar() {
    }

    @Override
    public ISchedule execute(IGraph graph) {
        totalNodeWeighting = getTotalNodeWeighting(graph);
        Map<INode, Integer> parentCounter = new HashMap<>();

        bottomLevelCache.clear();
        for (INode node : graph.getNodes()) {
            computeCriticalPath(node, graph);
            parentCounter.put(node, node.getParents().size());
        }

        sortedNodes.clear();
        graph.getNodes().stream().sorted(Comparator.comparing(INode::getLabel)).forEachOrdered(sortedNodes::add);

        PriorityQueue<AStarSchedule> open = new PriorityQueue<>();
        Set<String> closed = new HashSet<>();
        open.add(new AStarSchedule(graph.getStartNodes(), parentCounter));
        int searchCount = 0;

        while (!open.isEmpty()) {
            AStarSchedule s = open.peek();
            open.remove(s);

            if (s.getScheduledNodeCount() == graph.getNodeCount()) {
                System.out.println(searchCount + " states searched");
                return s; // optimal schedule found
            }

            for (INode node : s.getFree()) {
                for (int i = 1; i <= Config.getInstance().getNumberOfCores(); i++) {
                    AStarSchedule child = s.expand(node, i);
                    if (!closed.contains(child.getScheduleString())) {
                        open.add(child);
                        closed.add(child.getScheduleString());
                        searchCount++;
                    }
                }
            }
        }
        return null;
    }

    private int getTotalNodeWeighting(IGraph graph) {
        int totalNodeWeighting = 0;

        for (INode node : graph.getNodes()) {
            totalNodeWeighting += node.getProcessingCost();
        }

        return totalNodeWeighting;
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


}
