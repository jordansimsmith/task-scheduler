package task.scheduler;

import java.io.File;

/**
 * ArgumentParser is a class for parsing the command line arguments for the task-scheduler program.
 */
public class ArgumentParser {

    // TODO: inject logger here
    public ArgumentParser() {
    }

    /**
     * parse is used to parse the command line arguments into a machine readable, configuration object.
     * @param args command line arguments from entry point
     * @return Instance of config singleton object
     * @throws IllegalArgumentException
     */
    public Config parse(String[] args) throws IllegalArgumentException {
        // check for required arguments
        if (args.length < 2) {
            throw new IllegalArgumentException("too few arguments were provided.");
        }

        // process input file
        String inputFilePath = args[0];
        File inputFile = new File(inputFilePath);
        if (!inputFile.exists()) {
            throw new IllegalArgumentException("file " + inputFile.getAbsolutePath() + " not found.");
        }

        // process number of cores
        int numberOfCores = 0;
        try {
            numberOfCores = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("number of cores " + args[1] + " is not a valid integer.");
        }

        // process optional arguments
        int numberOfThreads = 1;
        boolean visualise = false;
        File outputFile = new File(inputFilePath.replace(".dot", "-output.dot"));
        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "-p":
                    // number of threads to use in execution
                    if (args.length < i + 2) {
                        throw new IllegalArgumentException("argument -p must be followed by a valid integer.");
                    }
                    try {
                        // read next integer and increment i
                        numberOfThreads = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("argument -p must be followed by a valid integer.");
                    }
                    break;
                case "-v":
                    // turn on visual interface
                    visualise = true;
                    break;
                case "-o":
                    // output file path
                    outputFile = new File(args[++i]);
                    break;
            }
        }

        // get config instance
        Config config = Config.getInstance();

        // set config fields
        config.setInputFile(inputFile);
        config.setOutputFile(outputFile);
        config.setNumberOfCores(numberOfCores);
        config.setNumberOfThreads(numberOfThreads);
        config.setVisualise(visualise);

        // success
        return config;
    }

    /**
     * printHelp is used to print the help commands to the console.
     * This method should be called when the user enters an incorrect set of arguments.
     */
    public void printHelp() {
        System.out.println("java −jar scheduler.jar INPUT.dot P [OPTION]\r\n" +
                "INPUT.dot a task graph with integer weights in dot format\r\n" +
                "P number of processors to schedule the INPUT graph on\r\n");

        System.out.println("Optional: \r\n" +
                "−p N use N cores for execution in parallel (default is sequential )\r\n" +
                "−v visualise the search\r\n" +
                "−o OUTPUT.dot output file is named OUTPUT.dot (default is INPUT−output.dot)\r\n");
    }
}
