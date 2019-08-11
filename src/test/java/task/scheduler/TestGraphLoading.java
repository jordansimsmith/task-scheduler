package task.scheduler;

import org.junit.After;
import org.junit.Test;
import task.scheduler.exception.DotFormatException;
import task.scheduler.mockclasses.MockLogger;
import task.scheduler.graph.Graph;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;

import java.io.File;

import static org.junit.Assert.*;

public class TestGraphLoading {
    private String dotFiles = "src/test/resources/dot_files/";
    private final MockLogger mockLogger;

    public TestGraphLoading() {
        this.mockLogger = new MockLogger();
    }

    @After
    public void tearDown() {
        // clear all strings that have been logged between tests
        mockLogger.clearLoggedItems();
    }

    /**
     * Check a basic valid graph is loaded correctly
     */
    @Test
    public void testValidNoComments() throws Exception {
        // arrange
        String file = "valid_no_comments.dot";

        // act
        IGraph g = new Graph(new File(dotFiles + file), mockLogger);

        // assert
        assertEquals(g.getNodeCount(), 4);
        assertEquals(g.getStartNodes().get(0).getLabel(), "a");


        for (INode node : g.getNodes()) {
            if (node.getLabel().equals("b")) {
                assertEquals(1, node.getParents().size());
                assertTrue(node.getParents().containsValue(11));

                assertFalse(node.getParents().containsValue(1));

                assertEquals(1, node.getChildren().size());
                assertTrue(node.getChildren().containsValue(2));

                assertFalse(node.getChildren().containsValue(3));
            }
        }
    }

    /**
     * Checks the interpreter correctly throws an error when a looped graph is loaded.
     */
    @Test(expected = DotFormatException.class)
    public void testLoopedGraph() throws Exception {
        // arrange
        String file = "invalid_looped.dot";

        // act
        IGraph g = new Graph(new File(dotFiles + file), mockLogger);

        // assert - should also throw exception
        assertTrue(mockLogger.getLoggedItems().isEmpty());
    }

    /**
     * Tests a graph with strange names (containing underscores, capitals and numbers as per spec)
     */
    @Test
    public void testValidSpecialNames() throws Exception {
        // arrange
        String file = "valid_special_names.dot";

        // act
        IGraph g = new Graph(new File(dotFiles + file), mockLogger);

        // assert
        assertEquals(g.getNodeCount(), 4);
        assertEquals(g.getStartNodes().get(0).getLabel(), "apache_node");


        for (INode node : g.getNodes()) {
            if (node.getLabel().equals("D322")) {
                assertEquals(2, node.getParents().size());
                assertTrue(node.getParents().containsValue(21));

                assertEquals(0, node.getChildren().size());
            }
        }
    }

    /**
     * Tests with extra params apart from Weight= for a node/edges.
     */
    @Test
    public void testValidExtraParams() throws Exception {
        // arrange
        String file = "valid_extra_params.dot";

        // act
        IGraph g = new Graph(new File(dotFiles + file), mockLogger);

        // assert
        assertEquals(g.getNodeCount(), 4);
        assertEquals(g.getStartNodes().get(0).getLabel(), "a");


        for (INode node : g.getNodes()) {
            if (node.getLabel().equals("b")) {
                assertEquals(1, node.getParents().size());
                assertTrue(node.getParents().containsValue(11));

                assertFalse(node.getParents().containsValue(1));

                assertEquals(1, node.getChildren().size());
                assertTrue(node.getChildren().containsValue(2));

                assertFalse(node.getChildren().containsValue(3));
            }
        }
    }
}
