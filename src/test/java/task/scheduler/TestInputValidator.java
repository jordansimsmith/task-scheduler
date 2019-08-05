package task.scheduler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import task.scheduler.common.InputValidator;
import task.scheduler.common.Tuple;
import task.scheduler.exception.GraphException;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestInputValidator {

    private InputValidator validator;

    @Mock
    private IGraph mockGraph;

    @Before
    public void setUp() {
        this.validator = new InputValidator();
    }

    @Test
    public void testNullStartNode() {
        // arrange
        when(mockGraph.getStartNode()).thenReturn(null);

        // act
        try {
            validator.validateGraph(mockGraph);
            fail();
        } catch (GraphException e) {
            // assert
            String expected = "start node cannot be null";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testInvalidParent() {
        // arrange
        INode start = mock(INode.class);
        Tuple<INode, Integer> edgeA = new Tuple<>(mock(INode.class), 1);
        Tuple<INode, Integer> edgeB = new Tuple<>(mock(INode.class), 2);
        List<Tuple<INode, Integer>> edges = Arrays.asList(edgeA, edgeB);

        when(start.getParents()).thenReturn(edges);
        when(mockGraph.getStartNode()).thenReturn(start);

        // act
        try {
            validator.validateGraph(mockGraph);
            fail();
        } catch (GraphException e) {
            // assert
            String expected = "start node should not have any parents";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testNullNodeList() {
        // arrange
        INode start = mock(INode.class);
        when(start.getParents()).thenReturn(new ArrayList<Tuple<INode, Integer>>());
        when(mockGraph.getStartNode()).thenReturn(start);
        when(mockGraph.getNodes()).thenReturn(null);

        // act
        try {
            validator.validateGraph(mockGraph);
            fail();
        } catch (GraphException e) {
            // assert
            String expected = "node list should not be null";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testInvalidListSize() {
        // arrange
        INode start = mock(INode.class);
        when(start.getParents()).thenReturn(new ArrayList<Tuple<INode, Integer>>());
        when(mockGraph.getStartNode()).thenReturn(start);
        when(mockGraph.getNodes()).thenReturn(new ArrayList<INode>());
        when(mockGraph.getNodeCount()).thenReturn(1);

        // act
        try {
            validator.validateGraph(mockGraph);
            fail();
        } catch (GraphException e) {
            // assert
            String expected = "node count doesn't match node list size";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testNullNode() {
        // arrange
        INode start = mock(INode.class);
        List<INode> nodes = new ArrayList<>();
        nodes.add(null);
        when(start.getParents()).thenReturn(new ArrayList<Tuple<INode, Integer>>());
        when(mockGraph.getStartNode()).thenReturn(start);
        when(mockGraph.getNodes()).thenReturn(nodes);
        when(mockGraph.getNodeCount()).thenReturn(1);

        // act
        try {
            validator.validateGraph(mockGraph);
            fail();
        } catch (GraphException e) {
            // assert
            String expected = "node shouldn't be null";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testZeroProcessingCostNode() {
        // arrange
        INode start = mock(INode.class);
        INode node = mock(INode.class);
        List<INode> nodes = Arrays.asList(node);
        when(start.getParents()).thenReturn(new ArrayList<Tuple<INode, Integer>>());
        when(node.getProcessingCost()).thenReturn(0);
        when(mockGraph.getStartNode()).thenReturn(start);
        when(mockGraph.getNodes()).thenReturn(nodes);
        when(mockGraph.getNodeCount()).thenReturn(1);

        // act
        try {
            validator.validateGraph(mockGraph);
            fail();
        } catch (GraphException e) {
            // assert
            String expected = "node processing cost cannot be less than 1";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testMultipleStartNodes() {
        // arrange
        INode start = mock(INode.class);
        INode node = mock(INode.class);
        List<INode> nodes = Arrays.asList(node);
        when(start.getParents()).thenReturn(new ArrayList<Tuple<INode, Integer>>());
        when(node.getProcessingCost()).thenReturn(1);
        when(node.getParents()).thenReturn(new ArrayList<Tuple<INode, Integer>>());
        when(mockGraph.getStartNode()).thenReturn(start);
        when(mockGraph.getNodes()).thenReturn(nodes);
        when(mockGraph.getNodeCount()).thenReturn(1);

        // act
        try {
            validator.validateGraph(mockGraph);
            fail();
        } catch (GraphException e) {
            // assert
            String expected = "there should only be one start node";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testDuplicateNodeLabels() {
        // arrange
        INode start = mock(INode.class);
        INode node = mock(INode.class);
        List<INode> nodes = Arrays.asList(node, node);
        when(start.getParents()).thenReturn(new ArrayList<Tuple<INode, Integer>>());
        when(node.getProcessingCost()).thenReturn(1);
        when(node.getParents()).thenReturn(Arrays.asList(new Tuple<>(start, 3)));
        when(node.getLabel()).thenReturn("a");
        when(mockGraph.getStartNode()).thenReturn(start);
        when(mockGraph.getNodes()).thenReturn(nodes);
        when(mockGraph.getNodeCount()).thenReturn(2);

        // act
        try {
            validator.validateGraph(mockGraph);
            fail();
        } catch (GraphException e) {
            // assert
            String expected = "node labels must be unique";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testCycleInGraph() {
        // arrange
        INode start = mock(INode.class);
        INode node = mock(INode.class);
        List<INode> nodes = Arrays.asList(start, node);
        when(start.getParents()).thenReturn(new ArrayList<Tuple<INode, Integer>>());
        when(start.getLabel()).thenReturn("b");
        when(start.getProcessingCost()).thenReturn(2);
        when(node.getProcessingCost()).thenReturn(1);
        when(node.getParents()).thenReturn(Arrays.asList(new Tuple<>(start, 2)));
        when(node.getLabel()).thenReturn("a");
        when(mockGraph.getStartNode()).thenReturn(start);
        when(mockGraph.getNodes()).thenReturn(nodes);
        when(mockGraph.getNodeCount()).thenReturn(2);

        // introduce cycle
        when(start.getChildren()).thenReturn(Arrays.asList(new Tuple<>(node, 3)));
        when(node.getChildren()).thenReturn(Arrays.asList(new Tuple<>(start, 2)));

        // act
        try {
            validator.validateGraph(mockGraph);
            fail();
        } catch (GraphException e) {
            // assert
            String expected = "cycle detected in graph";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testValidGraph() throws GraphException {
        // arrange
        INode start = mock(INode.class);
        INode node = mock(INode.class);
        List<INode> nodes = Arrays.asList(start, node);
        when(start.getParents()).thenReturn(new ArrayList<Tuple<INode, Integer>>());
        when(start.getLabel()).thenReturn("b");
        when(start.getProcessingCost()).thenReturn(2);
        when(node.getProcessingCost()).thenReturn(1);
        when(node.getParents()).thenReturn(Arrays.asList(new Tuple<>(start, 2)));
        when(node.getLabel()).thenReturn("a");
        when(mockGraph.getStartNode()).thenReturn(start);
        when(mockGraph.getNodes()).thenReturn(nodes);
        when(mockGraph.getNodeCount()).thenReturn(2);

        // introduce cycle
        when(start.getChildren()).thenReturn(Arrays.asList(new Tuple<>(node, 3)));
        when(node.getChildren()).thenReturn(new ArrayList<Tuple<INode, Integer>>());

        // act
        validator.validateGraph(mockGraph);
    }

}
