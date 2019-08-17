package task.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import task.scheduler.common.*;
import task.scheduler.exception.DotFormatException;
import task.scheduler.exception.GraphException;
import task.scheduler.graph.Graph;
import task.scheduler.graph.IGraph;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;
import task.scheduler.schedule.SchedulerFactory;
import task.scheduler.schedule.astar.AStar;
import task.scheduler.ui.UIOrchestrator;

import java.io.FileOutputStream;
import java.io.IOException;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("Task Scheduler starting.");

        // parse input arguments
        ArgumentParser argumentParser = new ArgumentParser();
        Config config;
        try {
            config = argumentParser.parse(args);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            argumentParser.printHelp();
            return;
        }

        // display results
        logger.info("Processing input file " + config.getInputFile().getPath());
        logger.info("To generate an optimal schedule over " + config.getNumberOfCores() + " cores");
        logger.info(config.getNumberOfThreads() + " threads will be used in execution");
        logger.info(config.isVisualise() ? "The results will be visualised" : "The results will not be visualised");
        logger.info("The results will be saved to " + config.getOutputFile().getPath());

        // parse input file
        IGraph input;
        try {
            input = new Graph(config.getInputFile());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (DotFormatException e) {
            logger.error("There was an error in the input dot file");
            logger.error(e.getMessage());
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

        // Get scheduler
        SchedulerFactory factory = new SchedulerFactory();
        IScheduler scheduler = factory.createScheduler(SchedulerFactory.SchedulerType.ASTAR);


        // Start visuals
        Thread ui = null;
        if (config.isVisualise())   {
            ui = new Thread(new UIOrchestrator(scheduler, 1000));
            ui.start();
        }

        // Execute
        logger.info("Starting ...");
        long time = System.currentTimeMillis();
        ISchedule output = scheduler.execute(input);
        long deltaTime = System.currentTimeMillis() - time;
        logger.info("... Finished");
        logger.info("In " + deltaTime + "ms");
        logger.info("Schedule cost: " + output.getTotalCost());

        if (ui != null) {
            ui.interrupt();
        }

        // write to output file - construction is long because dependency injection is needed
        try (FileWriter fileWriter = new FileWriter(new FileOutputStream(config.getOutputFile()))) {
            fileWriter.writeScheduledGraphToFile(input, output);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        logger.info("Schedule written to output file " + Config.getInstance().getOutputFile().getPath());
    }
}
