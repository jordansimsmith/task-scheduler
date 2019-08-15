package task.scheduler.schedule.valid;


import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;

import java.util.*;

/**
 * Valid Schedule is a class designed to find a valid schedule it does this by adding all Tasks to the first process
 * regardless of how many process there are. This is done for the first milestone.
 */
public class ValidScheduler implements IScheduler {

    private ISchedule currentSchedule = new ValidSchedulerSchedule();

    public ValidScheduler() {
    }

    /**
     * Uses the BFS algorithm to identify all the nodes.
     *
     */
    @Override
    public ISchedule execute(IGraph graph) {
        List<INode> startNodes = graph.getStartNodes();
        Map<INode, Map<INode, Integer>> dependencies = new HashMap<>();

        Queue<INode> queue = new LinkedList<>();
        queue.addAll(startNodes);
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

        this.currentSchedule = schedule;
        return schedule;
    }

    @Override
    public ISchedule getCurrentSchedule() {
        return this.currentSchedule;
    }

    @Override
    public int getSchedulesSearched() {
        return 0;
    }

    /**
     * This function will remove the dependencies from the child nodes and return the child node if upon removing the
     * parent node it does not contain any dependencies.
     *
     */
    private List<INode> removeDependencies(INode node, Map<INode, Map<INode, Integer>> dependencies) {
        Map<INode, Integer> children = node.getChildren();
        List<INode> toRet = new ArrayList<>();
        for (INode child : children.keySet()) {
            // If the child node is not contained add it to the map and fill it with all its dependencies (Parents)
            if (!dependencies.containsKey(child)) {
                dependencies.put(child, child.getParents());
            }
            Map<INode, Integer> parents = dependencies.get(child);
            parents.remove(node);
            if (parents.size() == 0) {
                toRet.add(child);
            }

        }
        return toRet;
    }
}
