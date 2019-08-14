package task.scheduler.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import javax.naming.directory.SearchResult;
import java.util.HashMap;
import java.util.Map;

public class SchedulingVisualizationAdapter {

    private static SchedulingVisualizationAdapter schedulingVisualizationAdapter = new SchedulingVisualizationAdapter();
    final NumberAxis xAxis = new NumberAxis();
    final CategoryAxis yAxis = new CategoryAxis();
    final SchedulingVisualization<Number,String> chart = new SchedulingVisualization<>(xAxis,yAxis);
    private Map<Integer, XYChart.Series> seriesMap = new HashMap<>();


    private SchedulingVisualizationAdapter(){
        setUpVisual();
    };

    public static SchedulingVisualizationAdapter getInstance(){
        return schedulingVisualizationAdapter;
    }

    public void populateVisual(IGraph graph, ISchedule schedule){
        clearSeriesList();

        for(INode node: graph.getNodes()) {
            Tuple<Integer, Integer> nodeSchedule = schedule.getNodeSchedule(node);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    SchedulingVisualization.DetailedInformation s = new SchedulingVisualization.DetailedInformation(node.getProcessingCost(), "status-red");
                    XYChart.Series k = new XYChart.Series();
                    try {
                         k = seriesMap.get(nodeSchedule.y);
                        k.getData().add(new XYChart.Data(nodeSchedule.x, "Core" +  nodeSchedule.y, s));
                    } catch (NullPointerException e){
                        System.out.println("This is stupid");
                    }

                }
            });

        }

    }

    public Chart getChart(){
        return this.chart;
    }

    public void setUpVisual(){
        xAxis.setLabel("");
        xAxis.setTickLabelFill(Color.CHOCOLATE);
        xAxis.setMinorTickCount(4);

        yAxis.setLabel("");
        yAxis.setTickLabelFill(Color.CHOCOLATE);
        yAxis.setTickLabelGap(10);

        chart.setTitle("Scheduling");
        chart.setLegendVisible(false);
        chart.setBlockHeight( 50);

        int threads = Config.getInstance().getNumberOfThreads();

        for (int i = 1; i <= threads; i++){
            final XYChart.Series series = new XYChart.Series();
            seriesMap.put(i, series);
        }

        for (XYChart.Series s : seriesMap.values()){
            chart.getData().add(s);
        }

        chart.getStylesheets().add(getClass().getResource("ganttchart.css").toExternalForm());
    }


    public void clearSeriesList(){
        for (XYChart.Series s : seriesMap.values()){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    s.getData().clear();
                }
            });
        }
    }
}
