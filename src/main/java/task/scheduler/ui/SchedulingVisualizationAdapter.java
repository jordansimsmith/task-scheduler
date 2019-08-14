package task.scheduler.ui;

import javafx.collections.FXCollections;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import task.scheduler.common.Tuple;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;
import task.scheduler.schedule.ISchedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SchedulingVisualizationAdapter {

    private static SchedulingVisualizationAdapter schedulingVisualizationAdapter = new SchedulingVisualizationAdapter();
    Map<Integer, XYChart.Series> seriesMap = new HashMap<>();

    private SchedulingVisualizationAdapter(){};

    public static SchedulingVisualizationAdapter getInstance(){
        return schedulingVisualizationAdapter;
    }

    public  SchedulingVisualization populateVisual(IGraph graph, ISchedule schedule){
        clearSeriesList();

        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();

        final SchedulingVisualization<Number,String> chart = new SchedulingVisualization<>(xAxis,yAxis);
        xAxis.setLabel("");
        xAxis.setTickLabelFill(Color.CHOCOLATE);
        xAxis.setMinorTickCount(4);

        yAxis.setLabel("");
        yAxis.setTickLabelFill(Color.CHOCOLATE);
        yAxis.setTickLabelGap(10);

        chart.setTitle("Scheduling");
        chart.setLegendVisible(false);
        chart.setBlockHeight( 50);

        for(INode node: graph.getNodes()) {
            Tuple<Integer, Integer> nodeSchedule = schedule.getNodeSchedule(node);

            if (seriesMap.get(nodeSchedule.y) == null){
                seriesMap.put(nodeSchedule.y, new XYChart.Series());
            }

            seriesMap.get(nodeSchedule.y).getData().add(new XYChart.Data(nodeSchedule.x, "Core" +  nodeSchedule.y, new SchedulingVisualization.DetailedInformation(node.getProcessingCost(), "status-red")));
        }

        for (XYChart.Series s : seriesMap.values()){
            chart.getData().add(s);
        }
        chart.getStylesheets().add(getClass().getResource("ganttchart.css").toExternalForm());

        return chart;
    }

    private void clearSeriesList(){
        for (XYChart.Series s : seriesMap.values()){
            s.getData().clear();
        }
    }
}
