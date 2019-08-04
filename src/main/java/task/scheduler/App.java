package task.scheduler;

import java.io.*;

public class App {
    public static void main(String[] args) {
        System.out.println("Task Scheduler starting.");

        // parse input arguments
        ArgumentParser argumentParser = new ArgumentParser();
        Config config;
        try {
            config = argumentParser.parse(args);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            argumentParser.printHelp();
            return;
        }

        // display results
        System.out.println("Processing input file " + config.getInputFile().getPath());
        System.out.println("To generate an optimal schedule over " + config.getNumberOfCores() + " cores");
        System.out.println(config.getNumberOfThreads() + " threads will be used in execution");
        System.out.println(config.isVisualise() ? "The results will be visualised" : "The results will not be visualised");
        System.out.println("The results will be saved to " + config.getOutputFile().getPath());

        // parse input file
        try {
            new Graph(config.getInputFile());
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
