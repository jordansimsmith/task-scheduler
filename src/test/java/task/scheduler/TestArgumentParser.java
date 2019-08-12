package task.scheduler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Logger;

import task.scheduler.common.ArgumentParser;
import task.scheduler.common.Config;
import task.scheduler.mockclasses.MockAppender;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class TestArgumentParser {

    private ArgumentParser parser;
    private MockAppender appender;
    private Logger logger;

    public TestArgumentParser(){
        parser = new ArgumentParser();
        appender = new MockAppender();
    }

    @Before
    public void setUp() {
        logger = Logger.getRootLogger();
        logger.addAppender(appender);
    }

    @After
    public void tearDown() {
        Config config = Config.getInstance();

        // reset singleton config object between tests
        config.setInputFile(null);
        config.setOutputFile(null);
        config.setNumberOfCores(0);
        config.setNumberOfThreads(0);
        config.setVisualise(false);

        // reset the appender between tests
        logger.removeAppender(appender);
    }

    @Test
    public void testMinumumArguments() {
        // arrange
        String[] args = {"src/test/resources/dot_files/test_file.dot", "2"};

        // act
        Config config = parser.parse(args);

        // assert
        assertEquals(new File("src/test/resources/dot_files/test_file.dot"), config.getInputFile());
        assertEquals(new File("src/test/resources/dot_files/test_file-output.dot"), config.getOutputFile());
        assertEquals(2, config.getNumberOfCores());
    }

    @Test
    public void testOptionalArguments() {
        // arrange
        String[] args = {"src/test/resources/dot_files/test_file.dot", "3", "-v", "-o", "output_file.dot", "-p", "8"};

        // act
        Config config = parser.parse(args);

        // assert
        assertEquals(new File("src/test/resources/dot_files/test_file.dot"), config.getInputFile());
        assertEquals(new File("output_file.dot"), config.getOutputFile());
        assertEquals(3, config.getNumberOfCores());
        assertEquals(8, config.getNumberOfThreads());
        assertTrue(config.isVisualise());
    }

    @Test
    public void testHelp() {
        // arrange
        String expectedString1 = "java −jar scheduler.jar INPUT.dot P [OPTION]\r\n" +
                "INPUT.dot a task graph with integer weights in dot format\r\n" +
                "P number of processors to schedule the INPUT graph on\r\n";

        String expectedString2 = "Optional: \r\n" +
                "−p N use N cores for execution in parallel (default is sequential )\r\n" +
                "−v visualise the search\r\n" +
                "−o OUTPUT.dot output file is named OUTPUT.dot (default is INPUT−output.dot)\r\n";

        // act
        parser.printHelp();

        // assert
        final List<LoggingEvent> log = appender.getLoggedItems();

        final LoggingEvent firstLogEntry = log.get(0);
        assertEquals(firstLogEntry.getLevel(), (Level.INFO));
        assertEquals(firstLogEntry.getMessage(), expectedString1);

        final LoggingEvent secondLogEntry = log.get(1);
        assertEquals(secondLogEntry.getLevel(), (Level.INFO));
        assertEquals(secondLogEntry.getMessage(), expectedString2);
    }

    @Test
    public void testInputFileNotFound() {
        // arrange
        String[] args = {"non_existent_file", "2"};

        // act
        try {
            parser.parse(args);
            fail();
        } catch (IllegalArgumentException e) {
            // do nothing
        }
    }

    @Test
    public void testInvalidCoreArgument() {
        // arrange
        String[] args = {"src/test/resources/dot_files/test_file.dot", "abc"};

        // act
        try {
            parser.parse(args);
            fail();
        } catch (IllegalArgumentException e) {
            // assert
            String expected = "number of cores abc is not a valid integer.";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testNoArguments() {
        // arrange
        String[] args = {};

        // act
        try {
            parser.parse(args);
            fail();
        } catch (IllegalArgumentException e) {
            // assert
            String expected = "too few arguments were provided.";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testSingleArgument() {
        // arrange
        String[] args = {"src/test/resources/dot_files/test_file.dot"};

        // act
        try {
            parser.parse(args);
            fail();
        } catch (IllegalArgumentException e) {
            // assert
            String expected = "too few arguments were provided.";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testIncompleteThreadArgument() {
        // arrange
        String[] args = {"src/test/resources/dot_files/test_file.dot", "3", "-v", "-o", "output_file.dot", "-p"};

        // act
        try {
            parser.parse(args);
            fail();
        } catch (IllegalArgumentException e) {
            // assert
            String expected = "argument -p must be followed by a valid integer.";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testInvalidThreadArgument() {
        // arrange
        String[] args = {"src/test/resources/dot_files/test_file.dot", "3", "-v", "-o", "output_file.dot", "-p", "not an int"};

        // act
        try {
            parser.parse(args);
            fail();
        } catch (IllegalArgumentException e) {
            // assert
            String expected = "argument -p must be followed by a valid integer.";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testInvalidOptionalArgument() {
        // arrange
        String[] args = {"src/test/resources/dot_files/test_file.dot", "3", "-x"};

        // act
        try {
            parser.parse(args);
            fail();
        } catch (IllegalArgumentException e) {
            // assert
            String expected = "argument -x was not recognised as a valid command.";
            assertEquals(expected, e.getMessage());
        }
    }
}

