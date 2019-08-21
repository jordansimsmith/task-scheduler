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
import java.util.ResourceBundle;
import java.util.StringJoiner;

public class FXController implements IVisualization, Initializable {

    private static final int UPDATE_INTERVAL_MS = 1000;

    @FXML
    private Pane inputGraphPane;

    @FXML
    private Pane outputGraphPane;

    @FXML
    private Label currentCostLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Pane cpuPane;

    @FXML
    private Pane memoryPane;

    private XYChart.Series<Number, Number> memoryUsageSeries = new XYChart.Series<>();
    private SchedulingVisualizationAdapter scheduleVisualiser = SchedulingVisualizationAdapter.getInstance();
    private IScheduler.SchedulerState schedulerState;
    private double memoryStartTime;

    private IGraph graph;
    private IScheduler scheduler;

    public FXController(IGraph graph, IScheduler scheduler) {
        this.graph = graph;
        this.scheduler = scheduler;
    }

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

    @Override
    public void pushState(IScheduler.SchedulerState newState) {
        // update scheduler state and set title
        Platform.runLater(() -> {
            this.schedulerState = newState;
            Stage stage = (Stage) this.outputGraphPane.getScene().getWindow();
            stage.setTitle(getTitle());
        });
    }

    @Override
    public void pushStats(double ramUsage, double cpuUsage) {
        // update memory graph
        Platform.runLater(() -> {
            if (this.memoryStartTime < 1) {
                memoryStartTime = System.currentTimeMillis();
            }
            double timeElapsed = (System.currentTimeMillis() - this.memoryStartTime) / 1000;
            this.memoryUsageSeries.getData().add(new XYChart.Data<>(timeElapsed, ramUsage / (1024 * 1024)));
        });
    }

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // setup views
        initialiseOutputGraph();
        initialiseInputGraph();
        initialiseMemoryUsage();

        // set up orchestrator to poll model for updates
        new Thread(new UIOrchestrator(this.scheduler, this, UPDATE_INTERVAL_MS)).start();
    }
}
