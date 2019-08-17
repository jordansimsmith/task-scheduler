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
import java.util.Random;

public class SchedulingVisualizationAdapter {

    private static SchedulingVisualizationAdapter schedulingVisualizationAdapter = new SchedulingVisualizationAdapter();
    private final NumberAxis xAxis = new NumberAxis();
    private final CategoryAxis yAxis = new CategoryAxis();
    private final SchedulingVisualization<Number, String> chart = new SchedulingVisualization<>(xAxis, yAxis);

    private Map<Integer, XYChart.Series> seriesMap = new HashMap<>();
    private Map<INode, VisualNode> nodeMap = new HashMap<>();
    private INode currentSelectedNode;


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
                    setColor(node, nodeSchedule.y);
                }

                Platform.runLater(() -> {
                    SchedulingVisualization.DetailedInformation s = new SchedulingVisualization.DetailedInformation(nodeMap.get(node));
                    XYChart.Series series = seriesMap.get(nodeSchedule.y);
                    XYChart.Data data = new XYChart.Data(nodeSchedule.x, "P" + nodeSchedule.y, s);

                    series.getData().add(data);
                    data.getNode().setOnMouseClicked(event -> setSelectionListenerAction(graph, node, schedule, data));
                });

            }

        }

    }

    private void setColor(INode node, int pVal){

        //Checking if a node has been selected and if that selected node has a parent that was not put on the graph
        if (currentSelectedNode != null && currentSelectedNode.getChildren().get(node) != null){
            nodeMap.get(node).setChild(true);
        } else {
            String color = pickColour(pVal);
            nodeMap.get(node).setColour(color);
        }
    }

    private void setSelectionListenerAction(IGraph graph, INode node, ISchedule schedule, XYChart.Data data){
            currentSelectedNode = node;
            //Only one item can be selected
            clearPreviousSelection(graph, schedule);
            nodeMap.get(node).setSelected(true);
            changeParentAndChildNodeColour(graph, node, schedule, data);

            populateVisual(graph, schedule);

    }

    private void changeParentAndChildNodeColour(IGraph graph, INode node, ISchedule schedule, XYChart.Data data){
        //Getting all parent nodes and changing their color
        for(INode curNode : graph.getNodes()){
            Tuple<Integer, Integer> nodeSchedule = schedule.getNodeSchedule(curNode);
            if (nodeSchedule != null && nodeMap.get(curNode) != null) {
                if (node.getParents().keySet().contains(curNode)){
                    //Setting the colour for parent node
                    nodeMap.get(curNode).setParent(true);
                } else if (node.getChildren().keySet().contains(curNode)){
                    //Setting the colour for child node
                    nodeMap.get(curNode).setChild(true);
                }
            }
        }
    }

    private void clearPreviousSelection(IGraph graph, ISchedule schedule){
        for(INode curNode : graph.getNodes()) {
            Tuple<Integer, Integer> nodeSchedule = schedule.getNodeSchedule(curNode);
            if (nodeSchedule != null) {
                VisualNode n = nodeMap.get(curNode);
                if (n != null) {
                    n.setSelected(false);
                    n.setChild(false);
                    n.setParent(false);
                }

            }
        }
    }

    public String pickColour(int pVal){
        String[] colours = {"status-greenish", "status-blueish", "status-pinkish", "status-orangish" };
        //As there are 7 shades of each colour
        Random rand = new Random();
        int shade = rand.nextInt(7) + 1;
        return colours[pVal%4] + shade;
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
