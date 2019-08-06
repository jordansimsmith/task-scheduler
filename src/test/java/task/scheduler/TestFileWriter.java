package task.scheduler;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import task.scheduler.common.Config;
import task.scheduler.common.FileWriter;
import task.scheduler.common.Tuple;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;
import task.scheduler.graph.Node;
import task.scheduler.schedule.ISchedule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestFileWriter {

    private FileWriter fileWriter;
    private Config config;
    @Spy
    private ByteArrayOutputStream outputStream;
    @Mock
    private IGraph mockGraph;
    @Mock
    private ISchedule mockSchedule;

    public TestFileWriter() {
        this.outputStream = new ByteArrayOutputStream();
        this.config = Config.getInstance();
    }

    @Before
    public void setUp() {
        this.fileWriter = new FileWriter(outputStream);
    }

    @After
    public void tearDown() {
        // clear output stream between tests
        outputStream.reset();

        // reset singleton config object between tests
        config.setInputFile(null);
        config.setNumberOfCores(0);
        config.setNumberOfThreads(0);
        config.setVisualise(false);
    }

    @Test
    public void testSingleNode() throws IOException {
        // arrange
        config.setOutputFile(new File("src/testSingleNode.dot"));
        String expectedString = "digraph \"testSingleNode\" {\n" +
                "\ta\t[Weight=1 Start=2 Processor=3];\n" +
                "}";

        Node node = new Node(1, "a");
        List<INode> nodes = new ArrayList<>();
        nodes.add(node);

        when(mockGraph.getNodes()).thenReturn(nodes);
        when(mockSchedule.getNodeSchedule(node)).thenReturn(new Tuple<>(2, 3));

        // act
        fileWriter.writeScheduledGraphToFile(mockGraph, mockSchedule);

        // remove carriage return if testing on windows
        String actualString = outputStream.toString().replaceAll("\r", "");

        // assert
        assertEquals(expectedString, actualString);
        verify(outputStream, atLeastOnce()).write((byte[]) Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void testTreeOfDepthOne() throws IOException {
        // arrange
        config.setOutputFile(new File("src/testTree.dot"));
        StringBuilder stringBuilder = new StringBuilder("digraph \"testTree\" {\n");
        stringBuilder.append("\t0\t[Weight=1 Start=0 Processor=0];\n");

        // set up single parent node
        Node firstNode = new Node(1, "0");
        List<INode> nodes = new ArrayList<>();
        nodes.add(firstNode);
        when(mockSchedule.getNodeSchedule(firstNode)).thenReturn(new Tuple<>(0, 0));

        int[] array = new int[50];

        for (int i = 1; i < array.length; i++) {
            array[i] = i;
        }

        // attach children nodes
        for (int i : array) {
            Node node = new Node(i, String.valueOf(i));
            when(mockSchedule.getNodeSchedule(node)).thenReturn(new Tuple<>(i, i));
            stringBuilder.append(String.format("\t%s\t[Weight=%d Start=%d Processor=%d];\n", node.getLabel(), i, i, i));

            node.addDependency(firstNode, i);
            stringBuilder.append(String.format("\t%s -> %s\t[Weight=%d];\n", firstNode.getLabel(), node.getLabel(), i));

            nodes.add(node);
        }

        when(mockGraph.getNodes()).thenReturn(nodes);

        stringBuilder.append("}");
        String expectedString = stringBuilder.toString();


        // act
        fileWriter.writeScheduledGraphToFile(mockGraph, mockSchedule);

        // remove carriage return if testing on windows
        String actualString = outputStream.toString().replaceAll("\r", "");

        // assert
        assertEquals(expectedString, actualString);
        verify(outputStream, atLeastOnce()).write((byte[]) Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
    }
}

