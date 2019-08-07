package task.scheduler.graph;

import task.scheduler.common.Tuple;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents a task to be scheduled, and its dependencies
 */
public class Node implements INode {
    private String label;
    private int cost;
    private Map<INode, Integer> dependencies = new HashMap<>();
    private Map<INode, Integer> dependents = new HashMap<>();

    public Node(int cost, String label) {
        this.label = label;
        this.cost = cost;
    }

    /**
     * Sets the task to be dependent on the given task, with given cost for transfer between processors
     */
    public void addDependency(INode task, int cost) {
        dependencies.put(task, cost);
    }

    /**
     * Sets the task to be depended on by the given task, with given cost for transfer between processors
     */
    public void addDependent(INode task, int cost) {
        dependents.put(task, cost);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder(label + " (" + cost + ") [");

        for (Map.Entry<INode, Integer> dependency : dependencies.entrySet()) {
            string.append(dependency.getKey().getLabel()).append("(").append(dependency.getValue()).append("),");
        }
        string.append("]");

        return string.toString();
    }

    @Override
    public Map<INode, Integer> getChildren() {
        return new HashMap<>(dependents);
    }

    @Override
    public Map<INode, Integer> getParents() {
        return new HashMap<>(dependencies);
    }

    @Override
    public int getProcessingCost() {
        return cost;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
