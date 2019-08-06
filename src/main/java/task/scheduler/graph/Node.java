package task.scheduler.graph;

import task.scheduler.common.Tuple;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a task to be scheduled, and its dependencies
 */
public class Node implements INode {
    private String label;
    private int cost;
    private List<Tuple<INode, Integer>> dependencies = new LinkedList<>();
    private List<Tuple<INode, Integer>> dependents = new LinkedList<>();

    public Node(int cost, String label) {
        this.label = label;
        this.cost = cost;
    }

    /**
     * Sets the task to be dependent on the given task, with given cost for transfer between processors
     */
    public void addDependency(INode task, int cost) {
        dependencies.add(new Tuple<>(task, cost));
    }

    /**
     * Sets the task to be depended on by the given task, with given cost for transfer between processors
     */
    public void addDependent(INode task, int cost) {
        dependents.add(new Tuple<>(task, cost));
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder(label + " (" + cost + ") [");

        for (Tuple<INode, Integer> dependency : dependencies) {
            string.append(dependency.x.getLabel()).append("(").append(dependency.y).append("),");
        }
        string.append("]");

        return string.toString();
    }

    @Override
    public List<Tuple<INode, Integer>> getChildren() {
        return new LinkedList<>(dependents);
    }

    @Override
    public List<Tuple<INode, Integer>> getParents() {
        return new LinkedList<>(dependencies);
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