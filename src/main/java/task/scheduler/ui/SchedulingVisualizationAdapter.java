package task.scheduler.ui;

import javafx.application.Platform;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import task.scheduler.common.Config;
import task.scheduler.common.Tuple;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;
import task.scheduler.schedule.ISchedule;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SchedulingVisualizationAdapter {

    private static SchedulingVisualizationAdapter schedulingVisualizationAdapter = new SchedulingVisualizationAdapter();
    private final NumberAxis xAxis = new NumberAxis();
    private final CategoryAxis yAxis = new CategoryAxis();
    private final SchedulingVisualization<Number, String> chart = new SchedulingVisualization<>(xAxis, yAxis);
    private Map<Integer, XYChart.Series> seriesMap = new HashMap<>();


    private SchedulingVisualizationAdapter() {
        setUpVisual();
    }

    public static SchedulingVisualizationAdapter getInstance() {
        return schedulingVisualizationAdapter;
    }

    public void populateVisual(IGraph graph, ISchedule schedule) {
        clearSeriesList();

        for (INode node : graph.getNodes()) {
            Tuple<Integer, Integer> nodeSchedule = schedule.getNodeSchedule(node);
            if (nodeSchedule != null) {
                Platform.runLater(() -> {
                    SchedulingVisualization.DetailedInformation s = new SchedulingVisualization.DetailedInformation(node.getProcessingCost(), "status-red", node.getLabel());
                    XYChart.Series k = new XYChart.Series();
                    k = seriesMap.get(nodeSchedule.y);
                    k.getData().add(new XYChart.Data(nodeSchedule.x, "P" + nodeSchedule.y, s));

                });
            }

        }

    }

    public Chart getChart() {
        return this.chart;
    }

    private void setUpVisual() {
        xAxis.setLabel("");
        xAxis.setTickLabelFill(Color.CHOCOLATE);
        xAxis.setMinorTickCount(4);

        yAxis.setLabel("");
        yAxis.setTickLabelFill(Color.CHOCOLATE);
        yAxis.setTickLabelGap(10);

        chart.setTitle("Scheduling");
        chart.setLegendVisible(false);
        chart.setBlockHeight(50);

        for (int p = 1; p <= Config.getInstance().getNumberOfCores(); p++) {
            final XYChart.Series series = new XYChart.Series();
            seriesMap.put(p, series);
        }

        for (XYChart.Series s : seriesMap.values()) {
            chart.getData().add(s);
        }

        chart.getStylesheets().add(getClass().getResource("/styles/gantt.css").toExternalForm());
    }


    private void clearSeriesList() {
        for (XYChart.Series s : seriesMap.values()) {
            Platform.runLater(() -> s.getData().clear());
        }
    }
}
