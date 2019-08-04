package task.scheduler;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestFileWriter {

    private FileWriter fileWriter;
    @Spy  private ByteArrayOutputStream outputStream;
    @Mock private IGraph mockGraph;
    @Mock private ISchedule mockSchedule;

    public TestFileWriter(){
        outputStream = new ByteArrayOutputStream();
    }

    @Before
    public void setUp() {
        this.fileWriter = new FileWriter(outputStream);
    }

    @After
    public void tearDown() {
        Config config = Config.getInstance();

        // clear output stream between tests
        outputStream.reset();

        // reset singleton config object between tests
        config.setInputFile(null);
        config.setOutputFile(null);
        config.setNumberOfCores(0);
        config.setNumberOfThreads(0);
        config.setVisualise(false);
    }

    @Test
    public void testSingleNode() throws IOException {
        // arrange
        String expectedString = "digraph \"testSingleNode\" {\n" +
                "\ta\t[Weight=1 Start=2 Processor=3];\n" +
                "}";

        Node node = new Node(1, "a");
        List<INode> nodes = new ArrayList<>();
        nodes.add(node);

        when(mockGraph.getNodes()).thenReturn(nodes);
        when(mockSchedule.getNodeSchedule(node)).thenReturn(new Tuple<>(2,3));

        // act
        fileWriter.writeScheduledGraphToFile(mockGraph, mockSchedule);

        // remove carriage return if testing on windows
        String actualString = outputStream.toString().replaceAll("\r","");

        // assert
        assertEquals(expectedString, actualString);
        verify(outputStream, atLeastOnce()).write((byte[]) Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
    }
}

