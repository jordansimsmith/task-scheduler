package task.scheduler.schedule.astar;

import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;

import java.util.*;

public class IterativeDeepeningAStar implements IScheduler {
    private PriorityQueue<AStarSchedule> solutions;

    public static int totalNodeWeighting;
    public static final Map<INode, Integer> bottomLevelCache = new HashMap<>();
    private static final int NOT_FOUND = -1;
    private static final int FOUND = -2;
    private IGraph graph;
    private int searchCount = 0;

    public IterativeDeepeningAStar() {

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

        this.graph = graph;

        AStarSchedule initialState = new AStarSchedule(graph.getStartNodes(), parentCounter);
        int limit = initialState.getHeuristicValue();
        Stack<AStarSchedule> stack = new Stack<>();
        Set<String> closed = new HashSet<>();
        stack.push(initialState);
        closed.add(initialState.getScheduleString());

        while(!stack.isEmpty()) {
            int result = DepthLimitedSearchRecursive(stack, closed, limit);

            if(result == FOUND){
                System.out.println(searchCount + " states searched");
                return stack.peek();
            }

            if(result == Integer.MAX_VALUE) {
                return null;
            }

            limit = result;
        }
        return null;
    }

    private int DepthLimitedSearchRecursive(Stack<AStarSchedule> stack, Set<String> closed, int limit) {
        AStarSchedule currentState = stack.peek();
        int f = currentState.getHeuristicValue();

        // exit depth limit search if we have reached the limit
        if ( f > limit) {
            return f;
        }

        // goal test
        if (currentState.getScheduledNodeCount() == graph.getNodeCount()) {
            return FOUND;
        }

        int min = Integer.MAX_VALUE;

        Set<AStarSchedule> childStates = currentState.getChildStates();

        for (AStarSchedule childState : childStates) {
            if(!closed.contains(childState.getScheduleString())){
                stack.push(childState);
                closed.add(childState.getScheduleString());
                int t = DepthLimitedSearchRecursive(stack, closed, limit);

                if ( t == FOUND) {
                    return FOUND;
                }

                min = Math.min(t, min);
                stack.pop();
                closed.remove(childState.getScheduleString());
                searchCount++;
            }
        }
        return min;
    }

    private int getTotalNodeWeighting(IGraph graph) {
        int totalNodeWeighting = 0;

        for(INode node : graph.getNodes()) {
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
