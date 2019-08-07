package task.scheduler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import task.scheduler.common.Tuple;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.ValidScheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestValidScheduler {

    @Mock
    private IGraph mockGraph;

    @Mock
    private INode start;

    @Mock
    private INode node;

    @Before
    public void setUp() {
        List<INode> nodes = new LinkedList<>(Arrays.asList(this.start, this.node));
        when(this.start.getProcessingCost()).thenReturn(2);
        when(this.node.getProcessingCost()).thenReturn(1);
        when(this.node.getParents()).thenReturn(new LinkedList<>(Arrays.asList(new Tuple<>(this.start, 2))));
        when(this.mockGraph.getStartNode()).thenReturn(this.start);
        when(this.start.getChildren()).thenReturn(new LinkedList<>(Arrays.asList(new Tuple<>(this.node, 3))));
        when(this.node.getChildren()).thenReturn(new ArrayList<Tuple<INode, Integer>>());
    }

    @Test
    public void testValidGraph() {
        ValidScheduler scheduler = new ValidScheduler();
        ISchedule schedule = scheduler.execute(mockGraph);
        Tuple<Integer, Integer> s1 = schedule.getNodeSchedule(this.start);
        Tuple<Integer, Integer> s2 = schedule.getNodeSchedule(this.node);
        assertEquals(s1.y, Integer.valueOf(1));
        assertEquals(s1.x, Integer.valueOf(0));
        assertEquals(s2.y, Integer.valueOf(1));
        assertEquals(s2.x, Integer.valueOf(2));
    }
}
