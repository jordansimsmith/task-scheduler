package task.scheduler.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * SchedulingVisualization overrides XYChart to enable Gantt Chart type scheduling
 * Based on the example provided here https://stackoverflow.com/questions/27975898/gantt-chart-from-scratch
 *
 * @param <X>
 * @param <Y>
 */
public class SchedulingVisualization<X, Y> extends XYChart<X, Y> {
    private double nodeHeight = 5;

    public static class DetailedInformation {
        private VisualNode visualNode;

        public DetailedInformation(VisualNode visualNode) {
            super();
            this.visualNode = visualNode;
        }

        public int getLength() {
            return visualNode.getProcessingCost();
        }

        public String getStyleSheet() {
            return visualNode.getColour();
        }

        public void setStyleSheet(String styleSheet) {
            this.visualNode.setColour(styleSheet);
        }

        public String getLabel() {
            return visualNode.getLabel();
        }

        public VisualNode getVisualNode() {
            return this.visualNode;
        }
    }


    public SchedulingVisualization(Axis<X> axisX, Axis<Y> axisY, ObservableList<Series<X, Y>> nodes) {
        super(axisX, axisY);
        setData(nodes);
    }

    public SchedulingVisualization(Axis<X> axisX, Axis<Y> axisY) {
        this(axisX, axisY, FXCollections.<Series<X, Y>>observableArrayList());
    }

    @Override
    protected void dataItemAdded(Series<X, Y> series, int i, Data<X, Y> data) {
        Node node = createNodeVisual(data);
        getPlotChildren().add(node);
    }

    @Override
    protected void dataItemRemoved(Data<X, Y> data, Series<X, Y> series) {
        Node node = data.getNode();
        getPlotChildren().remove(node);
    }

    @Override
    protected void dataItemChanged(Data<X, Y> data) {
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int i) {
        for (Data<X, Y> data : series.getData()) {
            Node node = createNodeVisual(data);
            getPlotChildren().add(node);
        }
    }

    @Override
    protected void seriesRemoved(Series<X, Y> series) {
        for (Data<X, Y> data : series.getData()) {
            Node node = data.getNode();
            getPlotChildren().remove(node);
        }

        removeSeriesFromDisplay(series);
    }

    @Override
    protected void layoutPlotChildren() {

        for (Series<X, Y> currentSeries : getData()) {


            Iterator<Data<X, Y>> iterator = getDisplayedDataIterator(currentSeries);

            while (iterator.hasNext()) {
                Data<X, Y> dataItem = iterator.next();
                double x = getXAxis().getDisplayPosition(dataItem.getXValue());
                double y = getYAxis().getDisplayPosition(dataItem.getYValue());
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                Rectangle rectangle;
                Node node = dataItem.getNode();

                if (node != null) {
                    if (node instanceof StackPane) {
                        StackPane shape = (StackPane) dataItem.getNode();

                        if (shape.getShape() instanceof Rectangle) {
                            rectangle = (Rectangle) shape.getShape();
                        } else if (shape.getShape() == null) {
                            rectangle = new Rectangle(getLength((dataItem.getExtraValue())), nodeHeight);
                        } else {
                            return;
                        }

                        rectangle.setWidth((getLength(dataItem.getExtraValue())) * ((getXAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis) getXAxis()).getScale()) : 1));
                        rectangle.setHeight(nodeHeight * ((getYAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis) getYAxis()).getScale()) : 1));
                        y -= nodeHeight / 2.0;

                        VBox labels = setUpLabels(dataItem, shape);

                        shape.setShape(rectangle);

                        shape.setAlignment(Pos.CENTER);
                        shape.getChildren().add(labels);
                        shape.setScaleShape(false);
                        shape.setCacheShape(false);

                        node.translateXProperty().setValue((getLength(dataItem.getExtraValue()) / 2.0) * ((getXAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis) getXAxis()).getScale()) : 1));
                        node.translateYProperty().setValue((nodeHeight) / 2.0 * ((getYAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis) getYAxis()).getScale()) : 1));
                        node.setLayoutX(x);
                        node.setLayoutY(y);

                    }
                }
            }
        }
    }


    private VBox setUpLabels(Data<X, Y> dataItem, StackPane pane) {
        Text text = new Text(getLabel(dataItem.getExtraValue()));
        text.setTextAlignment(TextAlignment.CENTER);
        text.setFontSmoothingType(FontSmoothingType.LCD);
        VBox vBox = new VBox(text);
        VisualNode visualNode = getVisualNode(dataItem.getExtraValue());

        if (visualNode.isParent()) {
            Text identifier = new Text("Parent");
            setTextProperties(identifier, vBox, pane, text);
        } else if (visualNode.isChild()) {
            Text identifier = new Text("Child");
            setTextProperties(identifier, vBox, pane, text);

        } else if (visualNode.isSelected()) {
            Text identifier = new Text("Selected");
            setTextProperties(identifier, vBox, pane, text);

        }
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }

    private VBox setTextProperties(Text identifier, VBox vBox, StackPane pane, Text text) {
        identifier.setTextAlignment(TextAlignment.CENTER);
        text.setFill(Color.WHITE);
        identifier.setFill(Color.WHITE);
        identifier.setFontSmoothingType(FontSmoothingType.LCD);
        vBox.getChildren().add(identifier);
        pane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.DOTTED, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        return vBox;
    }

    @Override
    protected void updateAxisRange() {
        final Axis<Y> axisY = getYAxis();
        final Axis<X> axisX = getXAxis();
        List<X> dataX = new ArrayList<>();
        List<Y> dataY = new ArrayList<>();

        if (axisX.isAutoRanging() || axisY.isAutoRanging()) {
            for (Series<X, Y> series : getData()) {
                for (Data<X, Y> data : series.getData()) {
                    if (axisX.isAutoRanging()) {
                        dataX.add(data.getXValue());
                        dataX.add(axisX.toRealValue(axisX.toNumericValue(data.getXValue()) + getLength(data.getExtraValue())));
                    }

                    if (axisY.isAutoRanging()) {
                        dataY.add(data.getYValue());
                    }
                }
            }
            if (axisX.isAutoRanging()) {
                axisX.invalidateRange(dataX);
            }
            if (axisY.isAutoRanging()) {
                axisY.invalidateRange(dataY);
            }

        }
    }

    /**
     * Method takes an ExtraData object from chart and returns the length information for a scheduled node
     */
    private static int getLength(Object o) {
        return ((DetailedInformation) o).getLength();
    }

    /**
     * Method takes an ExtraData object from chart and returns the label information for a scheduled node
     */
    private static String getLabel(Object o) {
        return ((DetailedInformation) o).getLabel();
    }

    /**
     * Method takes an ExtraData object from chart and returns the style information for a scheduled node
     */
    private static String getStyleClass(Object obj) {
        return ((SchedulingVisualization.DetailedInformation) obj).getStyleSheet();
    }

    /**
     * Method takes an ExtraData object from chart and returns the visual node for a scheduled node
     */
    private static VisualNode getVisualNode(Object obj) {
        return ((SchedulingVisualization.DetailedInformation) obj).getVisualNode();
    }

    private static void setStyleClass(Object obj, String styleClass) {
        ((SchedulingVisualization.DetailedInformation) obj).setStyleSheet(styleClass);
    }

    /**
     * Creates a new node that gets added to the chart getting generated
     */
    private Node createNodeVisual(Data<X, Y> data) {

        Node container = data.getNode();

        if (container == null) {
            container = new StackPane();
            data.setNode(container);
        }

        container.getStyleClass().add(getStyleClass(data.getExtraValue()));
        return container;
    }

    /**
     * Sets the height of the node block that gets displayed
     *
     * @param blockHeight
     */
    public void setBlockHeight(double blockHeight) {
        this.nodeHeight = blockHeight;
    }
}

