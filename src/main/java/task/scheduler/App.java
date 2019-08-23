package task.scheduler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import task.scheduler.common.ArgumentParser;
import task.scheduler.common.Config;
import task.scheduler.common.FileWriter;
import task.scheduler.common.InputValidator;
import task.scheduler.exception.DotFormatException;
import task.scheduler.exception.GraphException;
import task.scheduler.graph.Graph;
import task.scheduler.graph.IGraph;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;
import task.scheduler.schedule.SchedulerFactory;
import task.scheduler.ui.FXController;
import task.scheduler.ui.ModernUI;

import java.io.FileOutputStream;
import java.io.IOException;

public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static IGraph graph;
    private static IScheduler scheduler;

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
        try {
            graph = new Graph(config.getInputFile());
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
            validator.validateGraph(graph);
        } catch (GraphException e) {
            logger.error("Validation failure. Check your graph!");
            e.printStackTrace();
            return;
        }

        // Get scheduler
        SchedulerFactory factory = new SchedulerFactory();
        scheduler = factory.createScheduler(SchedulerFactory.SchedulerType.ASTAR);

        // Start visuals
        if (config.isVisualise()) {
            new Thread(() -> launch(args)).start();
        }

        // Execute
        logger.info("Starting ...");
        long time = System.currentTimeMillis();
        ISchedule output = scheduler.execute(graph);
        long deltaTime = System.currentTimeMillis() - time;
        logger.info("... Finished");
        logger.info("In " + deltaTime + "ms");
        logger.info("Schedule cost: " + output.getTotalCost());

        // write to output file - construction is long because dependency injection is needed
        try (FileWriter fileWriter = new FileWriter(new FileOutputStream(config.getOutputFile()))) {
            fileWriter.writeScheduledGraphToFile(graph, output);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        logger.info("Schedule written to output file " + Config.getInstance().getOutputFile().getPath());
        if (!Config.getInstance().isVisualise()) {
            System.exit(0);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        Font.loadFont(getClass().getResource("/fonts/Roboto-Medium.ttf").toExternalForm(), 10);
        Font.loadFont(getClass().getResource("/fonts/BebasNeue-Regular.ttf").toExternalForm(), 10);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModernUI.fxml"));
        ModernUI controller = new ModernUI(graph, scheduler);
        loader.setController(controller);
        stage.setTitle("Task Scheduler");
        stage.setScene(new Scene(loader.load(), 1280, 960)); // use -10 to fix the margin on width
        stage.setResizable(false);
        stage.show();
    }
}
