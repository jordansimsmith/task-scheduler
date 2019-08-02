package task.scheduler;

import java.util.List;

public interface INode {
    public List<Tuple<INode,Integer>> getChildren();
    public List<Tuple<INode,Integer>> getParents();
    public int getProcessingCost();
    public String getLabel();
}
