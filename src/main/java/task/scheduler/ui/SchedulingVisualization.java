package task.scheduler.ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
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

        public VisualNode getVisualNode(){
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

                        VBox labels = setUpLabels(dataItem);

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


    private VBox setUpLabels(Data<X, Y> dataItem){
        Text text = new Text(getLabel(dataItem.getExtraValue()));
        text.setTextAlignment(TextAlignment.CENTER);
        VBox vBox = new VBox(text);
        VisualNode visualNode = getVisualNode(dataItem.getExtraValue());

        if (visualNode.isParent()){
            Text identifier = new Text("Parent");
            identifier.setTextAlignment(TextAlignment.CENTER);
            vBox.getChildren().add(identifier);
        } else if (visualNode.isChild()){
            Text identifier = new Text("Child");
            identifier.setTextAlignment(TextAlignment.CENTER);
            vBox.getChildren().add(identifier);
        } else if (visualNode.isSelected()){
            Text identifier = new Text("Selected");
            identifier.setTextAlignment(TextAlignment.CENTER);
            vBox.getChildren().add(identifier);
        }
        vBox.setAlignment(Pos.CENTER);
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

    private static int getLength(Object o) {
        return ((DetailedInformation) o).getLength();
    }

    private static String getLabel(Object o) {
        return ((DetailedInformation) o).getLabel();
    }

    private static String getStyleClass(Object obj) {
        return ((SchedulingVisualization.DetailedInformation) obj).getStyleSheet();
    }

    private static VisualNode getVisualNode(Object obj) {
        return ((SchedulingVisualization.DetailedInformation) obj).getVisualNode();
    }

    private static void setStyleClass(Object obj, String styleClass) {
         ((SchedulingVisualization.DetailedInformation) obj).setStyleSheet(styleClass);
    }

    private Node createNodeVisual(Data<X, Y> data) {

        Node container = data.getNode();

        if (container == null) {
            container = new StackPane();
            data.setNode(container);
        }

        container.getStyleClass().add(getStyleClass(data.getExtraValue()));
        return container;
    }

    public void setBlockHeight(double blockHeight) {
        this.nodeHeight = blockHeight;
    }
}

