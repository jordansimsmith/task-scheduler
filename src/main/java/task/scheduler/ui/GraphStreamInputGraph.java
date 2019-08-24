package task.scheduler.ui;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import task.scheduler.graph.Graph;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static guru.nidi.graphviz.model.Factory.*;
import static guru.nidi.graphviz.model.Factory.mutNode;

public class GraphStreamInputGraph {

    private IGraph graph;

    public GraphStreamInputGraph (IGraph input){
        this.graph = input;
        generateGraph();
    }


    /**
     * The generateGraph() method adds the nodes of the INode graph and adds them to the DAG diagram representing the graph
     */
    private void generateGraph() {
        MutableGraph g = mutGraph("Graph1").setDirected(true).use((gr, ctx) -> {

            List<INode> nodes = graph.getNodes();

            for (INode parent : nodes) {
                Map<INode, Integer> children = parent.getChildren();
                Set<INode> childrenList = children.keySet();


                for (INode child : childrenList) {
                    mutNode(parent.getLabel()).addLink(to(mutNode(child.getLabel())).with(Label.of(children.get(child).toString())));
                }
            }

            //Edge case if there is only one node
            if (nodes.size() == 1) {
                mutNode(nodes.get(0).getLabel());
            }
        });


        try {
            Graphviz.fromGraph(g).width(900).render(Format.PNG).toFile(new File(".tmp/input-graph.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
