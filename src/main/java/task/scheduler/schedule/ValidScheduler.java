package task.scheduler.schedule;


import task.scheduler.IGraph;
import task.scheduler.INode;
import task.scheduler.ISchedule;
import task.scheduler.Tuple;

import java.util.*;

/**
 * Valid Schedule is a class designed to find a valid schedule it does this by adding all Tasks to the first process
 * regardless of how many process there are. This is done for the first milestone.
 */
public class ValidScheduler implements IScheduler {

    public ValidScheduler() {
    }

    /**
     * Uses the BFS algorithm to identify all the nodes.
     *
     * @Author : Reshad Contractor
     */
    @Override
    public ISchedule execute(IGraph graph) {
        INode startNode = graph.getStartNode();
        Map<INode, List<Tuple<INode, Integer>>> dependencies = new HashMap<>();

        Queue<INode> queue = new LinkedList<>();
        queue.add(startNode);
        int startTime = 0;
        ValidSchedulerSchedule schedule = new ValidSchedulerSchedule();
        while (!queue.isEmpty()) {
            INode node = queue.poll();
            if (node == null) {
                break;
            }
            List<INode> toAdd = removeDependencies(node, dependencies);
            queue.addAll(toAdd);
            schedule.addSchedule(node, startTime, 1);
            startTime += node.getProcessingCost();
        }
        return schedule;

    }

    /**
     * This function will remove the dependencies from the child nodes and return the child node if upon removing the
     * parent node it does not contain any dependencies.
     *
     * @Author : Reshad Contractor
     */
    private List<INode> removeDependencies(INode node, Map<INode, List<Tuple<INode, Integer>>> dependencies) {
        List<Tuple<INode, Integer>> children = node.getChildren();
        List<INode> toRet = new ArrayList<>();
        for (Tuple<INode, Integer> child : children) {
            // If the child node is not contained add it to the map and fill it with all its dependencies (Parents)
            if (!dependencies.containsKey(child.x)) {
                dependencies.put(child.x, child.x.getParents());
            }
            List<Tuple<INode, Integer>> parents = dependencies.get(child.x);
            for (Tuple<INode, Integer> parent : parents) {
                if (parent.x == node) {
                    parents.remove(parent);
                    break;
                }
            }
            if (parents.size() == 0) {
                toRet.add(child.x);
            }

        }
        return toRet;
    }
}
