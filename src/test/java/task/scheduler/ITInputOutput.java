package task.scheduler;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static org.junit.Assert.*;

public class ITInputOutput {

    private static final String JAR = "target/task-scheduler-1.0-SNAPSHOT.jar";
    private static final String INPUT = "src/test/resources/dot_files/integration_test_input.dot";

    private File outputFile;

    @Before
    public void setUp() throws IOException {
        // construct temporary output file which is deleted on JVM exit
        this.outputFile = File.createTempFile("output", ".dot");
        this.outputFile.deleteOnExit();
    }

    @Test
    public void testInputOutput() throws IOException, InterruptedException {

        // structure command
        StringJoiner command = new StringJoiner(" ");
        command.add("java -jar");
        command.add(JAR);
        command.add(INPUT);
        command.add("2");
        command.add("-o");
        command.add(outputFile.getPath());

        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", command.toString());

        // execute program from the command line
        int status = builder.start().waitFor();
        if (status != 0) {
            fail("process did not return with status code 0");
        }

        // read lines to string list
        List<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(outputFile));
        while (reader.ready()) {
            lines.add(reader.readLine());
        }
        reader.close();

        // verify file contents
        String actual, expected;

        // verify first line
        actual = lines.get(0);
        expected = "digraph \"" + outputFile.getName().replaceFirst(".dot", "") + "\" {";
        assertEquals(expected, actual);

        // verify last line
        actual = lines.get(lines.size() - 1);
        expected = "}";
        assertEquals(expected, actual);

        // verify edges
        String[] edges = {"a -> b", "a -> c", "c -> d", "b -> d"};
        String[] weights = {"[Weight=11]", "[Weight=2]", "[Weight=1", "[Weight=2]"};
        for (int i = 0; i < edges.length; i++) {
            boolean contains = false;
            for (String line : lines) {
                if (line.contains(edges[i]) && line.contains(weights[i])) {
                    contains = true;
                    break;
                }
            }
            String message = "edge \"" + edges[i] + " " + weights[i] + "\" not contained in output";
            assertTrue(message, contains);
        }

        // verify nodes
        String[] nodes = {"a", "b", "c", "d"};
        for (int i = 0; i < nodes.length; i++) {
            boolean contains = false;
            for (String line : lines) {
                if (line.contains(nodes[i]) && line.contains("Start=") && line.contains("Processor=") && line.contains("Weight=")) {
                    contains = true;
                    break;
                }
            }
            String message = "node \"" + nodes[i] + "\" with its weight, start and processor attributes was not in the output";
            assertTrue(message, contains);
        }
    }
}
