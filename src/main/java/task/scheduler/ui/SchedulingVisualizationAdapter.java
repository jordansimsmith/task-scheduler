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

/**
 * Class SchedulingVisualizationAdapter is used to create, generate and update a Gantt Chart that is used to job visualization
 * on CPU cores.
 */
public class SchedulingVisualizationAdapter {

    private static SchedulingVisualizationAdapter schedulingVisualizationAdapter = new SchedulingVisualizationAdapter();
    private final NumberAxis xAxis = new NumberAxis();
    private final CategoryAxis yAxis = new CategoryAxis();
    private final SchedulingVisualization<Number, String> chart = new SchedulingVisualization<>(xAxis, yAxis);

    private Map<Integer, XYChart.Series> seriesMap = new HashMap<>();
    private Map<INode, VisualNode> nodeMap = new HashMap<>();
    private INode currentSelectedNode;
    private int[] shadePicker = {0, 0, 0, 0};


    private SchedulingVisualizationAdapter() {
        setUpVisual();
    }

    public static SchedulingVisualizationAdapter getInstance() {
        return schedulingVisualizationAdapter;
    }

    /**
     * PopulateVisual is called when the Gantt Chart created by the SchedulingVIsualizationAdapter needs to be updated
     * with new scheduling data
     *
     * @param graph    IGraph input that is used to retrieve all the nodes in the generated graph
     * @param schedule ISchedule used to get the scheduled processor for a node
     */
    public void populateVisual(IGraph graph, ISchedule schedule) {
        clearSeriesList();

        for (INode node : graph.getNodes()) {
            Tuple<Integer, Integer> nodeSchedule = schedule.getNodeSchedule(node);
            if (nodeSchedule != null) {

                if (nodeMap.get(node) == null) {
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

    /**
     * Sets the default colour of a new node and checks if the new node is the parent of the selected node. If it is then
     * the new VisualNodes isParent parameter is set to true.
     */
    private void setColor(INode node, int pVal) {

        //Checking if a node has been selected and if that selected node has a parent that was not put on the graph
        if (currentSelectedNode != null && currentSelectedNode.getChildren().get(node) != null) {
            nodeMap.get(node).setChild(true);
        }

        String color = pickColour(pVal);
        nodeMap.get(node).setColour(color);

    }

    /**
     * Sets onClickListener for when a scheduled box is clicked.
     */
    private void setSelectionListenerAction(IGraph graph, INode node, ISchedule schedule, XYChart.Data data) {
        currentSelectedNode = node;
        //Only one item can be selected
        clearPreviousSelection(graph, schedule);
        nodeMap.get(node).setSelected(true);
        changeParentAndChildNodeColour(graph, node, schedule, data);

        populateVisual(graph, schedule);

    }

    /**
     * Helper method for onClickListener that changed the state of a visual node by stating if it is a parent or child
     */
    private void changeParentAndChildNodeColour(IGraph graph, INode node, ISchedule schedule, XYChart.Data data) {
        //Getting all parent nodes and changing their color
        for (INode curNode : graph.getNodes()) {
            Tuple<Integer, Integer> nodeSchedule = schedule.getNodeSchedule(curNode);
            if (nodeSchedule != null && nodeMap.get(curNode) != null) {
                if (node.getParents().keySet().contains(curNode)) {
                    //Setting the colour for parent node
                    nodeMap.get(curNode).setParent(true);
                } else if (node.getChildren().keySet().contains(curNode)) {
                    //Setting the colour for child node
                    nodeMap.get(curNode).setChild(true);
                }
            }
        }
    }

    /**
     * Clears all parent, child or selected states from all nodes that are shcenduled
     */
    private void clearPreviousSelection(IGraph graph, ISchedule schedule) {
        for (INode curNode : graph.getNodes()) {
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

    /**
     * Helper method helps pink a random colour specified in the css stylesheet file
     */
    public String pickColour(int pVal) {
        String[] colours = {"status-greenish", "status-blueish", "status-pinkish", "status-orangish"};
        //As there are 6 shades of each colour
        int shade = (shadePicker[pVal % 4] % 6) + 1;
        shadePicker[pVal % 4]++;
        return colours[pVal % 4] + shade;
    }

    /**
     * Returns the chart generated
     *
     * @return
     */
    public Chart getChart() {
        return this.chart;
    }

    /**
     * Sets up the chart that contains the Gantt Chart schedule at instantiation
     */
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

        chart.getStylesheets().add(getClass().getResource("/styles/gantt.css").toExternalForm());
    }

    /**
     * Clears all the nodes from the series. Called every time the series is populated.
     */
    private void clearSeriesList() {
        for (XYChart.Series s : seriesMap.values()) {
            Platform.runLater(() -> s.getData().clear());
        }
    }
}
