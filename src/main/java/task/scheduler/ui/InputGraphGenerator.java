package task.scheduler.ui;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.org.apache.bcel.internal.generic.INEG;
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

        visualization.setStrict(false);
        visualization.setAutoCreate(true);
        visualization.display();

        List<INode> nodes = graph.getNodes();

        for (INode parent : nodes){
            Map<INode, Integer> children = parent.getChildren();
            Set<INode> childrenList = children.keySet();

            for ( INode child : childrenList){
                visualization.addEdge(parent.getLabel() + child.getLabel(), parent.getLabel(), child.getLabel());
            }
        }


    }

}
