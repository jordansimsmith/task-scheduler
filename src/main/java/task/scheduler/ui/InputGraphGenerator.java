package task.scheduler.ui;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static guru.nidi.graphviz.model.Factory.*;


public class InputGraphGenerator {
    private IGraph graph;
    private Image image;

    /**
     * InputGraphGenerator takes in a graph and produces a DAG visualization of the graph as a png
     *
     * @param input
     */
    public InputGraphGenerator(IGraph input) {

        this.graph = input;
        generateGraph();
    }

    /**
     * The generateGraph() method adds the nodes of the INode graph and adds them to the DAG diagram representing the graph
     */
    public void generateGraph() {
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


    /**
     * The getGraph() function returns the ImageView associated with the DAG graph image.
     *
     * @return returns an ImageView object
     */
    public ImageView getGraph() {

        try {
            image = new Image(new FileInputStream(".tmp/input-graph.png"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ImageView imageView = new ImageView(image);

        imageView.setPreserveRatio(true);
        imageView.setFitHeight(image.getHeight());
        imageView.setFitWidth(image.getWidth());

        return imageView;
    }
}
