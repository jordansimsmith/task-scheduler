package task.scheduler.schedule;

import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;

import java.util.*;

/**
 * SchedulerState stores global state used in the A* and BNB algorithms. The use of public static fields is
 * justified because these fields are used very frequently, and thus the overhead from getters/object instantiation
 * is removed.
 */
public class SchedulerCache {
    public static int totalNodeWeighting;
    public static Map<INode, Integer> bottomLevelCache = new HashMap<>();
    public static List<INode> sortedNodes = new ArrayList<>();
    private static SchedulerUtils schedulerUtils = new SchedulerUtils();

    /**
     * Calculates and populates the totalNodeWeighting field with sum of the
     * processing costs of the INodes in the given IGraph.
     *
     * @param graph of which to calculate the total node weighting
     */
    public static void populateTotalNodeWeighting(IGraph graph) {
        totalNodeWeighting = 0;
        for (INode node : graph.getNodes()) {
            totalNodeWeighting += node.getProcessingCost();
        }
    }

    /**
     * Populates the sortedNodes field with all INodes of the given graph sorted according to their
     * node label.
     *
     * @param graph of which to populate the sortedNodes field with
     */
    public static void populateSortedNodes(IGraph graph) {
        // the use of streams here is justified because it is only called once
        sortedNodes.clear();
        graph.getNodes().stream().sorted(Comparator.comparing(INode::getLabel)).forEachOrdered(sortedNodes::add);
    }

    /**
     * Calculates the bottom level costs for each node in the provided graph. The bottom levels are
     * a map of all INodes of the graph to their bottom level Integer. The bottom level of an
     * INode is the critical path from the INode.
     *
     * @param graph the IGraph of which to calculate the bottomLevelCache
     */
    public static void populateBottomLevelCache(IGraph graph) {
        bottomLevelCache.clear();
        for (INode node : graph.getNodes()) {
            int bottomLevel = schedulerUtils.computeCriticalPath(node, graph);
            bottomLevelCache.put(node, bottomLevel);
        }
    }
}
