package task.scheduler.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
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
import java.util.ResourceBundle;
import java.util.StringJoiner;

public class ModernUI implements IVisualization, Initializable {
    private static final int UPDATE_INTERVAL_MS = 1000;

    @FXML
    private VBox inputGraphVBox;

    @FXML
    private VBox outputGraphVBox;

    @FXML
    private Label currentCostLabel;

    @FXML
    private PieChart dataChart;

    @FXML
    private VBox cpuVBox;

    @FXML
    private VBox memoryVbox;

    @FXML
    private Label progressBarInfo;

    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();;

    private XYChart.Series<Number, Number> memoryUsageSeries = new XYChart.Series<>();
    private SchedulingVisualizationAdapter scheduleVisualiser = SchedulingVisualizationAdapter.getInstance();
    private IScheduler.SchedulerState schedulerState;
    private double memoryStartTime;
    private BigDecimal schedulesUpperBound;
    private double schedulesUpperBoundLog;

    private IGraph graph;
    private IScheduler scheduler;

    public ModernUI(IGraph graph, IScheduler scheduler) {
        this.graph = graph;
        this.scheduler = scheduler;
    }

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

    @Override
    public void pushState(IScheduler.SchedulerState newState) {
        // update scheduler state and set title
        this.schedulerState = newState;
        Platform.runLater(() -> {

            Stage stage = (Stage) this.outputGraphVBox.getScene().getWindow();
            stage.setTitle(getTitle());
        });
    }

    @Override
    public void pushStats(double ramUsage, double cpuUsage) {
        // update memory graph
        if (this.memoryStartTime < 1) {
            memoryStartTime = System.currentTimeMillis();
        }
        double timeElapsed = (System.currentTimeMillis() - this.memoryStartTime) / 1000;

        Platform.runLater(() -> {
            this.memoryUsageSeries.getData().add(new XYChart.Data<>(timeElapsed, ramUsage / (1024 * 1024)));
        });
    }

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

    private void initialiseInputGraph() {
        // initialise input graph visualisation
        InputGraphGenerator inputGraphGenerator = new InputGraphGenerator(this.graph);
        ImageView inputGraph = inputGraphGenerator.getGraph();
        inputGraph.setFitWidth(350);
        inputGraph.preserveRatioProperty();
        this.inputGraphVBox.getChildren().add(inputGraph);
        //inputGraph.fitWidthProperty().bind(this.inputGraphVBox.widthProperty());
        //inputGraph.setPreserveRatio(true);
        //inputGraph.fitHeightProperty().bind(this.inputGraphVBox.heightProperty());
    }

    private void initialiseChartData(){
       pieChartData.add(new PieChart.Data("Log States", 0) );
        pieChartData.add(new PieChart.Data("All States", 0) );
       dataChart.setData(pieChartData);

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
        initialiseChartData();
        this.schedulesUpperBound = this.graph.getSchedulesUpperBound();
        this.schedulesUpperBoundLog = this.graph.getSchedulesUpperBoundLog();

        // set up orchestrator to poll model for updates
        new Thread(new UIOrchestrator(this.scheduler, this, UPDATE_INTERVAL_MS)).start();
    }
}


