package task.scheduler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class TestArgumentParser {

    private ArgumentParser parser;

    @Before
    public void setUp() {
        this.parser = new ArgumentParser();
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
    }

    @Test
    public void testMinumumArguments() {
        // arrange
        String[] args = {"src/test/resources/test_file.dot", "2"};

        // act
        Config config = parser.parse(args);

        // assert
        assertEquals(new File("src/test/resources/test_file.dot"), config.getInputFile());
        assertEquals(new File("src/test/resources/test_file-output.dot"), config.getOutputFile());
        assertEquals(2, config.getNumberOfCores());
    }

    @Test
    public void testOptionalArguments() {
        // arrange
        String[] args = {"src/test/resources/test_file.dot", "3", "-v", "-o", "output_file.dot", "-p", "8"};

        // act
        Config config = parser.parse(args);

        // assert
        assertEquals(new File("src/test/resources/test_file.dot"), config.getInputFile());
        assertEquals(new File("output_file.dot"), config.getOutputFile());
        assertEquals(3, config.getNumberOfCores());
        assertEquals(8, config.getNumberOfThreads());
        assertTrue(config.isVisualise());
    }

    @Test
    public void testHelp() {
        // should not throw an exception
        parser.printHelp();
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
        String[] args = {"src/test/resources/test_file.dot", "abc"};

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
        String[] args = {"src/test/resources/test_file.dot"};

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
        String[] args = {"src/test/resources/test_file.dot", "3", "-v", "-o", "output_file.dot", "-p"};

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
        String[] args = {"src/test/resources/test_file.dot", "3", "-v", "-o", "output_file.dot", "-p", "not an int"};

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
}

