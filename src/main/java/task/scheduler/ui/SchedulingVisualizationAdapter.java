package task.scheduler.ui;

import javafx.application.Platform;
import javafx.scene.chart.*;
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
    private Map<INode, VisualNode> nodeMap = new HashMap<>();


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
                if (nodeMap.get(node) == null){
                    nodeMap.put(node, new VisualNode(node));
                    nodeMap.get(node).setColour("status-red");
                }

                Platform.runLater(() -> {
                    SchedulingVisualization.DetailedInformation s = new SchedulingVisualization.DetailedInformation(nodeMap.get(node));
                    XYChart.Series series = seriesMap.get(nodeSchedule.y);
                    XYChart.Data data = new XYChart.Data(nodeSchedule.x, "P" + nodeSchedule.y, s);
                    series.getData().add(data);
                    data.getNode().setOnMouseClicked(event ->  {
                        nodeMap.get(node).setColour( "status-blue");
                        populateVisual(graph, schedule);
                    });

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
        xAxis.setMinorTickCount(0);

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

        File file = new File("src/main/resources/styles/gantt.css");
        chart.getStylesheets().add("file:///" + file.getAbsolutePath());
    }


    private void clearSeriesList() {
        for (XYChart.Series s : seriesMap.values()) {
            Platform.runLater(() -> s.getData().clear());
        }
    }
}
