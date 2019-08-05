package task.scheduler;

import java.io.*;

public class App {
    public static void main(String[] args) {
        final ILogger logger = new ConsoleLogger();
        logger.log("Task Scheduler starting.");

        // parse input arguments
        ArgumentParser argumentParser = new ArgumentParser(logger);
        Config config;
        try {
            config = argumentParser.parse(args);
        } catch (IllegalArgumentException e) {
            logger.log(e.getMessage());
            argumentParser.printHelp();
            return;
        }

        // display results
        logger.log("Processing input file " + config.getInputFile().getPath());
        logger.log("To generate an optimal schedule over " + config.getNumberOfCores() + " cores");
        logger.log(config.getNumberOfThreads() + " threads will be used in execution");
        logger.log(config.isVisualise() ? "The results will be visualised" : "The results will not be visualised");
        logger.log("The results will be saved to " + config.getOutputFile().getPath());

        // parse input file
        try {
            new Graph(config.getInputFile(), logger);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // write to output file - construction is long because dependency injection is needed
        // TODO: move/change this invocation once algorithms have been implemented
        try (FileWriter fileWriter = new FileWriter(new FileOutputStream(config.getOutputFile()))){
            fileWriter.writeScheduledGraphToFile(null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
