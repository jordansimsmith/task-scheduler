package task.scheduler;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.StringJoiner;

import static org.junit.Assert.fail;

public class ITInputOutput {

    private static final String JAR = "target/task-scheduler-1.0-SNAPSHOT.jar";
    private static final String INPUT = "src/test/resources/dot_files/valid_no_comments.dot";

    private File outputFile;

    @Before
    public void setUp() throws IOException {
        // construct temporary output file
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

        // TODO: verify output file
    }
}
