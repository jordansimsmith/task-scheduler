package task.scheduler.ui;

import task.scheduler.graph.INode;

import java.util.Map;

/**
 * VisualNode implements INode and it is a wrapper class for INode with added information that allows it to keep state
 * on the chart
 */
public class VisualNode implements INode {
    private INode node;
    private String colour;
    private boolean selected, parent, child;

    public VisualNode(INode node) {
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

    /**
     * Returns the colour for the VisualNode. The string returned is a reference to the stylesheet object added to the
     * chart at the beginning
     */
    public String getColour() {
        if (selected) {
            return "status-selected";
        } else if (parent) {
            return "status-parent";
        } else if (child) {
            return "status-child";
        } else {
            return colour;
        }
    }

    /**
     * Sets the default colour of the VisualNode
     *
     * @param colour
     */
    public void setColour(String colour) {
        this.colour = colour;
    }


    /**
     * Set to true by the setOnClicked() listener if this VisualNode is clicked on the graph
     */
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    /**
     * Set to true by the setOnClicked() listener if this VisualNode is the parent of the node clicked on the graph
     */
    public void setParent(Boolean parent) {
        this.parent = parent;
    }

    /**
     * Set to true by the setOnClicked() listener if this VisualNode is the child of the node clicked on the graph
     */
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
