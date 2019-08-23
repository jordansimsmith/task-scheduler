package task.scheduler.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import task.scheduler.common.Config;
import task.scheduler.graph.IGraph;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringJoiner;

/**
 * FX Controller for visualisation
 */
public class FXController implements IVisualization, Initializable {

    private static final int UPDATE_INTERVAL_MS = 1000;

    /**
     * FXML linked elements
     */
    @FXML
    private Pane inputGraphPane;

    @FXML
    private Pane outputGraphPane;

    @FXML
    private Label currentCostLabel;

    @FXML
    private ProgressBar progressBar;

    // For cpu visualisation
    @FXML
    private Pane cpuPane;
    LineChart<Number, Number> cpuChart;
    private List<XYChart.Series<Number, Number>> cpuUsageSeries = new ArrayList<>();

    // For memory visualisation
    @FXML
    private Pane memoryPane;

    private XYChart.Series<Number, Number> memoryUsageSeries = new XYChart.Series<>();
    private SchedulingVisualizationAdapter scheduleVisualiser = SchedulingVisualizationAdapter.getInstance();
    private IScheduler.SchedulerState schedulerState;
    private double visualisationStartTime;

    // Internal elements used by scheduler
    private IGraph graph;
    private IScheduler scheduler;

    public FXController(IGraph graph, IScheduler scheduler) {
        this.graph = graph;
        this.scheduler = scheduler;
    }

    /**
     * Pushes a schedule to be rendered, also includes info about the number of schedules searched
     */
    @Override
    public void pushSchedule(ISchedule schedule, int schedulesSearched) {
        Platform.runLater(() -> {
            if (schedule != null) {
                // update partial schedule displayed
                this.scheduleVisualiser.populateVisual(this.graph, schedule);

                // update current cost label
                this.currentCostLabel.setText(String.valueOf(schedule.getTotalCost()));
            }

            // update progress bar
            this.progressBar.setProgress(Math.log(schedulesSearched) / this.graph.getSchedulesUpperBoundLog());

        });
    }

    /**
     * Pushes scheduler state used to update GUI elements about current status
     */
    @Override
    public void pushState(IScheduler.SchedulerState newState) {
        // update scheduler state and set title
        Platform.runLater(() -> {
            this.schedulerState = newState;
            Stage stage = (Stage) this.outputGraphPane.getScene().getWindow();
            stage.setTitle(getTitle());
        });
    }

    /**
     * Pushes RAM and CPU usage stats
     * @param ramUsage RAM usage in bytes
     * @param cpuUsage List of doubles, each double representing cpu usage on a core, range 0-1 (1 being 100%)
     */
    @Override
    public void pushStats(double ramUsage, List<Double> cpuUsage) {
        // update memory and cpu graph
        Platform.runLater(() -> {
            if (this.visualisationStartTime < 1) {
                visualisationStartTime = System.currentTimeMillis();
            }
            double timeElapsed = (System.currentTimeMillis() - this.visualisationStartTime) / 1000;
            this.memoryUsageSeries.getData().add(new XYChart.Data<>(timeElapsed, ramUsage / (1024 * 1024)));

            for (int i = 0; i <  cpuUsage.size(); i++) {
                if (this.cpuUsageSeries.size() <= i)   {
                    XYChart.Series<Number,Number> trend = new XYChart.Series<>();
                    this.cpuChart.getData().add(trend);
                    this.cpuUsageSeries.add(trend);
                }

                this.cpuUsageSeries.get(i).getData().add(new XYChart.Data<>(timeElapsed, cpuUsage.get(i)));
            }
        });
    }

    // Initialisation functions for several elements

    private void initialiseOutputGraph() {
        // initialise schedule visualiser
        Chart outputGraph = scheduleVisualiser.getChart();
        this.outputGraphPane.getChildren().add(outputGraph);
        outputGraph.prefWidthProperty().bind(this.outputGraphPane.widthProperty());
        outputGraph.prefHeightProperty().bind(this.outputGraphPane.heightProperty());
    }

    private void initialiseMemoryUsage() {
        // initialise memory view
        NumberAxis memoryXAxis = new NumberAxis();
        memoryXAxis.setLabel("Time (s)");
        NumberAxis memoryYAxis = new NumberAxis();
        memoryYAxis.setLabel("Memory Usage (Mb)");
        LineChart<Number, Number> memoryChart = new LineChart<>(memoryXAxis, memoryYAxis);

        memoryChart.getData().add(memoryUsageSeries);
        memoryChart.setLegendVisible(false);
        this.memoryPane.getChildren().add(memoryChart);
        memoryChart.prefWidthProperty().bind(this.memoryPane.widthProperty());
        memoryChart.prefHeightProperty().bind(this.memoryPane.heightProperty());
    }

    private void initialiseCPUUsage()   {
        NumberAxis cpuXAxis = new NumberAxis();
        cpuXAxis.setLabel("Time (s)");
        NumberAxis cpuYAxis = new NumberAxis();
        cpuYAxis.setLabel("CPU Usage %");

        cpuChart = new LineChart<>(cpuXAxis, cpuYAxis);
        cpuChart.setLegendVisible(false);
        this.cpuPane.getChildren().add(cpuChart);
        cpuChart.prefWidthProperty().bind(this.memoryPane.widthProperty());
        cpuChart.prefHeightProperty().bind(this.memoryPane.heightProperty());
    }

    private void initialiseInputGraph() {
        // initialise input graph visualisation
        InputGraphGenerator inputGraphGenerator = new InputGraphGenerator(this.graph);
        ImageView inputGraph = inputGraphGenerator.getGraph();
        this.inputGraphPane.getChildren().add(inputGraph);
        inputGraph.fitWidthProperty().bind(this.inputGraphPane.widthProperty());
        inputGraph.fitHeightProperty().bind(this.inputGraphPane.heightProperty());
    }

    private String getTitle() {
        StringJoiner joiner = new StringJoiner(" ");
        joiner.add("Task Scheduler");
        joiner.add("-");
        joiner.add(Config.getInstance().getInputFile().getName());
        joiner.add("-");
        joiner.add(this.schedulerState.toString());

        return joiner.toString();
    }

    // Initializes the whole GUI
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // setup views
        initialiseOutputGraph();
        initialiseInputGraph();
        initialiseMemoryUsage();
        initialiseCPUUsage();

        // set up orchestrator to poll model for updates
        new Thread(new UIOrchestrator(this.scheduler, this, UPDATE_INTERVAL_MS)).start();
    }
}
