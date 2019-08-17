package task.scheduler.ui;

import task.scheduler.graph.INode;

import java.util.Map;

public class VisualNode implements INode {
    INode node;
    String colour;
    boolean selected, parent, child;

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
        if (selected){
            return "status-selected";
        } else if (parent){
            return "status-parent";
        } else if (child){
            return "status-child";
        } else {
            return colour;
        }
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    //Only one item can be selected at a time
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public void setParent(Boolean parent) {
        this.parent = parent;
    }

    public void setChild(Boolean child) {
        this.child = child;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isParent() {
        return parent;
    }

    public boolean isChild() {
        return child;
    }
}
