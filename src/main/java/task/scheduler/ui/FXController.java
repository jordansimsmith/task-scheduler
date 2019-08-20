package task.scheduler.ui;

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
import task.scheduler.App;

import java.net.URL;
import java.util.ResourceBundle;

public class FXController implements Initializable {

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // initialise schedule visualiser
        SchedulingVisualizationAdapter scheduleVisualiser = SchedulingVisualizationAdapter.getInstance();
        Chart outputGraph = scheduleVisualiser.getChart();
        this.outputGraphPane.getChildren().add(outputGraph);
        outputGraph.prefWidthProperty().bind(this.outputGraphPane.widthProperty());
        outputGraph.prefHeightProperty().bind(this.outputGraphPane.heightProperty());

        // initialise memory view
        NumberAxis memoryXAxis = new NumberAxis();
        memoryXAxis.setLabel("Time (s)");
        NumberAxis memoryYAxis = new NumberAxis();
        memoryYAxis.setLabel("Memory Usage (Mb)");
        LineChart<Number, Number> memoryChart = new LineChart<>(memoryXAxis, memoryYAxis);
        memoryChart.getData().add(memoryUsageSeries);
        this.memoryPane.getChildren().add(memoryChart);
        memoryChart.prefWidthProperty().bind(this.memoryPane.widthProperty());
        memoryChart.prefHeightProperty().bind(this.memoryPane.heightProperty());

        // initialise input graph visualisation
        InputGraphGenerator inputGraphGenerator = new InputGraphGenerator(App.input);
        ImageView inputGraph = inputGraphGenerator.getGraph();
        this.inputGraphPane.getChildren().add(inputGraph);
        inputGraph.fitWidthProperty().bind(this.inputGraphPane.widthProperty());
        inputGraph.fitHeightProperty().bind(this.inputGraphPane.heightProperty());
    }
}
