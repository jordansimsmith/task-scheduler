package task.scheduler;

import org.junit.Test;
import task.scheduler.exception.DotFormatException;
import task.scheduler.graph.Graph;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class TestGraphLoading {
    private String dotFiles = "src/test/resources/dot_files/";

    @Test
    public void testValidNoComments() throws Exception {
        // arrange
        String file = "valid_no_comments.dot";

        // act
        IGraph g = new Graph(new File(dotFiles + file));

        // assert
        assertEquals(g.getNodeCount(), 4);
        assertEquals(g.getStartNode().getLabel(), "a");

        for (INode node : g.getNodes()) {
            if (node.getLabel().equals("b")) {
                assertEquals(1, node.getParents().size());
                assertEquals("a", node.getParents().get(0).x.getLabel());
                assertEquals(1, (int) node.getParents().get(0).y);

                assertEquals(1, node.getChildren().size(), 1);
                assertEquals("d", node.getChildren().get(0).x.getLabel());
                assertEquals(2, (int) node.getChildren().get(0).y);
            }
        }
    }

    @Test(expected = DotFormatException.class)
    public void testLoopedGraph() throws Exception {
        // arrange
        String file = "invalid_looped.dot";

        // act
        IGraph g = new Graph(new File(dotFiles + file));
    }
}
