package task.scheduler.ui;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;


import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;

public class InputGraphGenerator {
    private IGraph graph;

    public InputGraphGenerator(IGraph input){
        this.graph = input;
    }

    /**
     * Generate graph generates the visualization of the graph from the provided IGraph
     */
    public void GenerateGraph(){
        Graph visualization = new SingleGraph("Initial Graph");

        visualization.display();
        visualization.setStrict(false);
        visualization.setAutoCreate(true);

        List<INode> nodes = graph.getNodes();

        for (INode parent : nodes){
            Map<INode, Integer> children = parent.getChildren();
            Set<INode> childrenList = children.keySet();

            for ( INode child : childrenList){
                visualization.addEdge(parent.getLabel() + child.getLabel(), parent.getLabel(), child.getLabel());

            }
        }
        //Adding label
        for (Node node : visualization) {
            node.addAttribute("ui.label", node.getId());
            node.addAttribute("ui.style", "text-size: 20px;");
            node.addAttribute("ui.style", "size: 20px;");
        }

    }

    public IGraph getGraph() {
        return graph;
    }
}
