package task.scheduler;

import task.scheduler.common.*;
import task.scheduler.exception.GraphException;
import task.scheduler.graph.Graph;
import task.scheduler.graph.IGraph;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;
import task.scheduler.schedule.SchedulerFactory;

import java.io.FileOutputStream;
import java.io.IOException;

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
            logger.error(e.getMessage());
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
        IGraph input;
        try {
            input = new Graph(config.getInputFile(), logger);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // validation
        InputValidator validator = new InputValidator();
        try {
            validator.validateGraph(input);
        } catch (GraphException e) {
            logger.error("Validation failure. Check your graph!");
            e.printStackTrace();
            return;
        }

        // produce schedule
        IScheduler scheduler = new SchedulerFactory().createScheduler(SchedulerFactory.SchedulerType.VALID);
        Long time = System.currentTimeMillis();
        System.out.println("Starting ...");
        ISchedule output = scheduler.execute(input);
        System.out.println(System.currentTimeMillis() - time + "ms");
        System.out.println(output.getTotalCost());

        // write to output file - construction is long because dependency injection is needed
        try (FileWriter fileWriter = new FileWriter(new FileOutputStream(config.getOutputFile()))) {
            fileWriter.writeScheduledGraphToFile(input, output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.log("Finished.");
    }
}
