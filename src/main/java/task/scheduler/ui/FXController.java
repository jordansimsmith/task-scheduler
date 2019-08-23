package task.scheduler.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import task.scheduler.common.Config;
import task.scheduler.graph.IGraph;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private VBox inputGraphVBox;

    @FXML
    private VBox outputGraphVBox;

    @FXML
    private Label currentCostLabel;
    @FXML
    private PieChart dataChart;

    @FXML
    private Label timeElapsedLabel;

    @FXML
    private Label progressBarInfo;

    // For cpu visualisation
    @FXML
    private VBox cpuVBox;
    LineChart<Number, Number> cpuChart;
    private List<XYChart.Series<Number, Number>> cpuUsageSeries = new ArrayList<>();

    // For memory visualisation
    @FXML
    private VBox memoryVbox;
    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();;

    private XYChart.Series<Number, Number> memoryUsageSeries = new XYChart.Series<>();
    private SchedulingVisualizationAdapter scheduleVisualiser = SchedulingVisualizationAdapter.getInstance();
    private IScheduler.SchedulerState schedulerState;
    private double visualisationStartTime;

    // Schedules searched
    private BigDecimal schedulesUpperBound;
    private double schedulesUpperBoundLog;

    @FXML
    private Label programStatusLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ProgressIndicator progressIndicator;

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
        // update partial schedule displayed
        this.scheduleVisualiser.populateVisual(this.graph, schedule);
        // Update progress bar status
        String percentageScheduled = String.valueOf(BigDecimal.valueOf(schedulesSearched * 100).divide(schedulesUpperBound,30, RoundingMode.HALF_UP));
        String percentageToPrint = "Total search space searched: " + percentageScheduled.substring(0, 4);
        if(percentageScheduled.contains("E")){
            percentageToPrint = percentageToPrint + percentageScheduled.substring(percentageScheduled.indexOf("E")) + " %";
        } else {
            percentageToPrint = percentageToPrint +  " %";
        }

        String finalPercentageToPrint = percentageToPrint;
        Platform.runLater(() -> {
            // First element is log value and second element is the total value
            pieChartData.get(0).setPieValue(Math.log(schedulesSearched));
            pieChartData.get(1).setPieValue(this.schedulesUpperBoundLog);

            //The first 3 number values and last 4 values are concatenated to give the decimal representation
            this.progressBarInfo.setText(finalPercentageToPrint);

        });
    }

    /**
     * Pushes scheduler state used to update GUI elements about current status
     */
    @Override
    public void pushState(IScheduler.SchedulerState newState) {
        // update scheduler state and set title
        this.schedulerState = newState;
        if (newState == IScheduler.SchedulerState.FINISHED){
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                programStatusLabel.setVisible(true);
            });


        }
        Platform.runLater(() -> {

            Stage stage = (Stage) this.outputGraphVBox.getScene().getWindow();
            stage.setTitle(getTitle());
        });
    }

    /**
     * Pushes RAM usage stats
     * @param ramUsage RAM usage in bytes
     */
    @Override
    public void pushMemoryUsage(double ramUsage) {
        // update memory graph
        if (this.visualisationStartTime < 1) {
            visualisationStartTime = System.currentTimeMillis();
        }
        double timeElapsed = (System.currentTimeMillis() - this.visualisationStartTime) / 1000;

        Platform.runLater(() -> {
            this.memoryUsageSeries.getData().add(new XYChart.Data<>(timeElapsed, ramUsage / (1024 * 1024)));
            //Updating time elapsed label
            timeElapsedLabel.setText("Time Elapsed: " + Math.ceil(timeElapsed));
        });
    }

    /**
     * @param perCoreUsage List of doubles, each double representing cpu usage on a core, range 0-1 (1 being 100%)
     */
    @Override
    public void pushCPUUsage(List<Double> perCoreUsage) {
        Platform.runLater(() -> {
            for (int i = 0; i <  perCoreUsage.size(); i++) {
                double timeElapsed = (System.currentTimeMillis() - this.visualisationStartTime) / 1000;

                if (this.cpuUsageSeries.size() <= i)   {
                    XYChart.Series<Number,Number> trend = new XYChart.Series<>();
                    this.cpuChart.getData().add(trend);
                    this.cpuUsageSeries.add(trend);
                }

                this.cpuUsageSeries.get(i).getData().add(new XYChart.Data<>(timeElapsed, perCoreUsage.get(i) * 100));
            }
        });
    }

    // Initialisation functions for several elements

    private void initialiseOutputGraph() {
        // initialise schedule visualiser
        Chart outputGraph = scheduleVisualiser.getChart();
        outputGraph.prefWidthProperty().bind(this.outputGraphVBox.widthProperty());
        outputGraph.prefHeightProperty().bind(this.outputGraphVBox.heightProperty());
        this.outputGraphVBox.getChildren().add(outputGraph);
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
        this.memoryVbox.getChildren().add(memoryChart);
        memoryChart.prefWidthProperty().bind(this.memoryVbox.widthProperty());
        memoryChart.prefHeightProperty().bind(this.memoryVbox.heightProperty());
    }

    private void initialiseCPUUsage()   {
        NumberAxis cpuXAxis = new NumberAxis();
        cpuXAxis.setLabel("Time (s)");
        NumberAxis cpuYAxis = new NumberAxis();
        cpuYAxis.setLabel("CPU Usage %");
        cpuYAxis.setForceZeroInRange(true);
        cpuYAxis.setAutoRanging(false);
        cpuYAxis.setUpperBound(100);
        cpuYAxis.setLowerBound(0);

        cpuChart = new LineChart<>(cpuXAxis, cpuYAxis);
        cpuChart.setLegendVisible(false);
        this.cpuVBox.getChildren().add(cpuChart);
        cpuChart.prefWidthProperty().bind(this.cpuVBox.widthProperty());
        cpuChart.prefHeightProperty().bind(this.cpuVBox.heightProperty());
    }

    /**
     *  initialise input graph visualisation
     */
    private void initialiseInputGraph() {
        new Thread(() -> {
            InputGraphGenerator inputGraphGenerator = new InputGraphGenerator(this.graph);
            ImageView inputGraph = inputGraphGenerator.getGraph();
            inputGraph.setFitWidth(350);
            inputGraph.preserveRatioProperty();

            Platform.runLater(() -> {
                this.inputGraphVBox.getChildren().removeAll();
                this.inputGraphVBox.getChildren().add(inputGraph);
            });
        }).start();
    }

    private void initialiseChartData(){
        //The order that this gets added into the observable matters as it gets accessed using index values later
        pieChartData.add(new PieChart.Data("Log Searched States", 0) );
        pieChartData.add(new PieChart.Data("Log All States", 0) );
        dataChart.setData(pieChartData);
        dataChart.setLegendVisible(false);

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
        initialiseChartData();
        //Spinning indicator
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        // Hides the button for displaying when a process has finished
        programStatusLabel.setVisible(false);
        this.schedulesUpperBound = this.graph.getSchedulesUpperBound();
        this.schedulesUpperBoundLog = this.graph.getSchedulesUpperBoundLog();

        // set up orchestrator to poll model for updates
        new Thread(new UIOrchestrator(this.scheduler, this, UPDATE_INTERVAL_MS)).start();
    }
}
