package task.scheduler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import task.scheduler.common.InputValidator;
import task.scheduler.exception.GraphException;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;

import java.util.*;

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
    public void testEmptyStartNodes() {
        // arrange
        when(mockGraph.getStartNodes()).thenReturn(new ArrayList<INode>());

        // act
        try {
            validator.validateGraph(mockGraph);
            fail();
        } catch (GraphException e) {
            // assert
            String expected = "start node cannot be empty";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testInvalidParent() {
        // arrange
        INode start = mock(INode.class);
        Map<INode, Integer> map = new HashMap<>();
        map.put(mock(INode.class), 1);
        map.put(mock(INode.class), 2);

        when(start.getParents()).thenReturn(map);
        when(mockGraph.getStartNodes()).thenReturn(Arrays.asList(start));

        // act
        try {
            validator.validateGraph(mockGraph);
            fail();
        } catch (GraphException e) {
            // assert
            String expected = "start nodes should not have any parents";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testNullNodeList() {
        // arrange
        INode start = mock(INode.class);
        when(start.getParents()).thenReturn(new HashMap<INode, Integer>());
        when(mockGraph.getStartNodes()).thenReturn(Arrays.asList(start));
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
        when(start.getParents()).thenReturn(new HashMap<INode, Integer>());
        when(mockGraph.getStartNodes()).thenReturn(Arrays.asList(start));
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
        when(start.getParents()).thenReturn(new HashMap<INode, Integer>());
        when(mockGraph.getStartNodes()).thenReturn(Arrays.asList(start));
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
        when(start.getParents()).thenReturn(new HashMap<INode, Integer>());
        when(node.getProcessingCost()).thenReturn(0);
        when(mockGraph.getStartNodes()).thenReturn(Arrays.asList(start));
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
    public void testDuplicateNodeLabels() {
        // arrange
        INode start = mock(INode.class);
        INode node = mock(INode.class);
        Map<INode, Integer> map = new HashMap<>();
        map.put(start, 3);
        List<INode> nodes = Arrays.asList(node, node);
        when(start.getParents()).thenReturn(new HashMap<INode, Integer>());
        when(node.getProcessingCost()).thenReturn(1);
        when(node.getLabel()).thenReturn("a");
        when(mockGraph.getStartNodes()).thenReturn(Arrays.asList(start));
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
        Map<INode, Integer> map = new HashMap<>();
        map.put(node, 3);
        Map<INode, Integer> map1 = new HashMap<>();
        map1.put(start, 2);
        List<INode> nodes = Arrays.asList(start, node);
        when(start.getParents()).thenReturn(new HashMap<INode, Integer>());
        when(start.getLabel()).thenReturn("b");
        when(start.getProcessingCost()).thenReturn(2);
        when(node.getProcessingCost()).thenReturn(1);
        when(node.getLabel()).thenReturn("a");
        when(mockGraph.getStartNodes()).thenReturn(Arrays.asList(start));
        when(mockGraph.getNodes()).thenReturn(nodes);
        when(mockGraph.getNodeCount()).thenReturn(2);

        // introduce cycle
        when(start.getChildren()).thenReturn(map);
        when(node.getChildren()).thenReturn(map1);

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
        Map<INode, Integer> map = new HashMap<>();
        INode start = mock(INode.class);
        map.put(start, 2);
        INode node = mock(INode.class);
        List<INode> nodes = Arrays.asList(start, node);
        when(start.getParents()).thenReturn(new HashMap<INode, Integer>());
        when(start.getLabel()).thenReturn("b");
        when(start.getProcessingCost()).thenReturn(2);
        when(node.getProcessingCost()).thenReturn(1);
        when(node.getLabel()).thenReturn("a");
        when(mockGraph.getStartNodes()).thenReturn(Arrays.asList(start));
        when(mockGraph.getNodes()).thenReturn(nodes);
        when(mockGraph.getNodeCount()).thenReturn(2);

        Map<INode, Integer> map1 = new HashMap<>();
        map1.put(node, 3);
        // introduce cycle
        when(start.getChildren()).thenReturn(map1);
        when(node.getChildren()).thenReturn(new HashMap<INode, Integer>());

        // act
        validator.validateGraph(mockGraph);
    }

}
