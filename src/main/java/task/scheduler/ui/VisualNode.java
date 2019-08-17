package task.scheduler.ui;

import task.scheduler.graph.INode;

import java.util.Map;
import java.util.Objects;

public class VisualNode implements INode {
    INode node;
    String colour;

    public VisualNode(INode node){
        this.node = node;
    }

    @Override
    public Map<INode, Integer> getChildren() {
        return node.getChildren();
    }

    @Override
    public Map<INode, Integer> getParents() {
        return node.getParents();
    }

    @Override
    public int getProcessingCost() {
        return node.getProcessingCost();
    }

    @Override
    public String getLabel() {
        return node.getLabel();
    }

    public INode getNode() {
        return node;
    }

    public void setNode(INode node) {
        this.node = node;
    }

    public String getColour() {
            return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }
}
