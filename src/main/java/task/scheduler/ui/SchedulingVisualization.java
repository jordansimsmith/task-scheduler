package task.scheduler.ui;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;

import javafx.scene.shape.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SchedulingVisualization<X, Y> extends XYChart<X, Y> {

    public static class DetailedInformation{
        public int length;
        public String styleClass;

        public DetailedInformation(String styleClass, int length){
            super();
            this.length = length;
            this.styleClass = styleClass;
        }

        public int getLength() {
            return length;
        }

        public String getStyleClass() {
            return styleClass;
        }
    }


    private double nodeHeight = 10;
    public SchedulingVisualization(Axis<X> axisX, Axis<Y> axisY, ObservableList<Series<X, Y>> nodes) {
        super(axisX, axisY);
        setData(nodes);
    }

    @Override
    protected void dataItemAdded(Series<X, Y> series, int i, Data<X, Y> data) {
        Node node  = createNodeVisual(data);
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
        for (Data<X, Y> data : series.getData()){
            Node node = createNodeVisual(data);
            getPlotChildren().add(node);
        }
    }

    @Override
    protected void seriesRemoved(Series<X, Y> series) {
        for (Data<X, Y> data : series.getData()){
            Node node = data.getNode();
            getPlotChildren().remove(node);
        }

        removeSeriesFromDisplay(series);
    }

    @Override
    protected void layoutPlotChildren() {

        for (Series<X, Y> currentSeries : getData()){

            for (Data<X, Y> dataItem : currentSeries.getData()){
                double x = getXAxis().getDisplayPosition(dataItem.getXValue());
                double y = getYAxis().getDisplayPosition(dataItem.getYValue());
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                Rectangle shape;
                Node node = dataItem.getNode();

                if (node != null){
                    if (node instanceof StackPane){
                        StackPane rectangle = (StackPane) dataItem.getNode();

                        if (rectangle.getShape() instanceof Rectangle){
                            shape = (Rectangle) rectangle.getShape();
                        } else if (rectangle.getShape() == null){
                            shape = new Rectangle(getLength((dataItem.getExtraValue())),  nodeHeight);
                        } else {
                            return;
                        }

                        shape.setWidth(getLength(dataItem.getExtraValue()) * ((getXAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis)getXAxis()).getScale()) : 1));
                        shape.setHeight(nodeHeight * ((getYAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis)getYAxis()).getScale()) : 1));
                        y -= nodeHeight / 2.0;

                        rectangle.setShape(null);
                        rectangle.setShape(shape);
                        rectangle.setScaleShape(false);
                        rectangle.setCenterShape(false);
                        rectangle.setCacheShape(false);

                        node.setLayoutX(x);
                        node.setLayoutY(y);

                    }
                }
            }


        }

    }

    @Override
    protected void updateAxisRange(){
        final Axis<Y> axisY = getYAxis();
        final Axis<X> axisX = getXAxis();
        List<X>  dataX = new ArrayList<>();
        List<Y>  dataY = new ArrayList<>();

        if(axisX.isAutoRanging() || axisY.isAutoRanging()){
            for (Series<X, Y> series : getData()){
                for (Data<X, Y> data : series.getData()){
                    if (axisX.isAutoRanging()){
                        dataX.add(data.getXValue());
                        dataX.add(axisX.toRealValue(axisX.toNumericValue(data.getXValue()) + getLength(data.getExtraValue())));
                    }

                    if (axisY.isAutoRanging()){
                        dataY.add(data.getYValue());
                    }
                }
            }
            axisX.invalidateRange(dataX);
            axisY.invalidateRange(dataY);
        }
    }

    private static int getLength(Object o){
        return ((DetailedInformation) o).getLength();
    }

    private Node createNodeVisual(Data<X, Y> data){

        Node container = data.getNode();

        if (container == null){
            container = new StackPane();
            data.setNode(container);
        }

        return container;
    }

    public void setBlockHeight( double blockHeight) {
        this.nodeHeight = blockHeight;
    }
}

