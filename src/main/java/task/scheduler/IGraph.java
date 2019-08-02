package task.scheduler;

import java.util.List;

public interface IGraph {
    public List<INode> getNodes();
    public INode getStartNode();
    public INode getEndNode();
    public int getNodeCount();
}
