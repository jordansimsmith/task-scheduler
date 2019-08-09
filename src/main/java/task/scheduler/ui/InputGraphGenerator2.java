package task.scheduler.ui;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import org.graphstream.graph.Node;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;



public class InputGraphGenerator2 {
    private IGraph graph;

    public InputGraphGenerator2(IGraph input){
        this.graph = input;
    }

    public void generateGraph() {
        MutableGraph g = mutGraph("example1").setDirected(true).use((gr, ctx) -> {

            List<INode> nodes = graph.getNodes();

            for (INode parent : nodes) {
                Map<INode, Integer> children = parent.getChildren();
                Set<INode> childrenList = children.keySet();

                for (INode child : childrenList) {
                    mutNode(parent.getLabel()).addLink(mutNode(child.getLabel()));

                }
            }

            //Edge case if there is only one node
            if (nodes.size() == 1) {
                mutNode(nodes.get(0).getLabel());
            }
        });
        try {
            Graphviz.fromGraph(g).width(200).render(Format.PNG).toFile(new File("example/ex1i.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
