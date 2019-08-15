package task.scheduler.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Based on the example provided here https://stackoverflow.com/questions/27975898/gantt-chart-from-scratch
 * @param <X>
 * @param <Y>
 */
public class SchedulingVisualization<X, Y> extends XYChart<X, Y> {

    public static class DetailedInformation{
        private int length;
        private String styleSheet;
        private String label;

        public  DetailedInformation(int length, String styleSheet, String label){
            super();
            this.length = length;
            this.styleSheet = styleSheet;
            this.label = label;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public String getStyleSheet() {
            return styleSheet;
        }

        public void setStyleSheet(String styleSheet) {
            this.styleSheet = styleSheet;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }


    private double nodeHeight = 10;
    public SchedulingVisualization(Axis<X> axisX, Axis<Y> axisY, ObservableList<Series<X, Y>> nodes) {
        super(axisX, axisY);
        setData(nodes);
    }

    public SchedulingVisualization(Axis<X> axisX, Axis<Y> axisY) {
        this(axisX, axisY, FXCollections.<Series<X, Y>>observableArrayList());
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

            Iterator<Data<X,Y>> iterator = getDisplayedDataIterator(currentSeries);

            while(iterator.hasNext()){
                Data<X, Y> dataItem = iterator.next();
                double x = getXAxis().getDisplayPosition(dataItem.getXValue());
                double y = getYAxis().getDisplayPosition(dataItem.getYValue());
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                Rectangle shape;
                Node node = dataItem.getNode();

                if (node != null){
                    if (node instanceof StackPane){
                        StackPane rectangle = (StackPane)dataItem.getNode();

                        if (rectangle.getShape() instanceof Rectangle){
                            shape = (Rectangle) rectangle.getShape();
                        } else if (rectangle.getShape() == null){
                            shape = new Rectangle(getLength((dataItem.getExtraValue())),  nodeHeight);
                        } else {
                            return;
                        }

                        shape.setWidth((getLength(dataItem.getExtraValue())) * ((getXAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis)getXAxis()).getScale()) : 1));
                        shape.setHeight(nodeHeight * ((getYAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis)getYAxis()).getScale()) : 1));
                        y -= nodeHeight / 2.0;

                        Text text = new Text(getLabel(dataItem.getExtraValue()));

                        rectangle.setShape(shape);

                        rectangle.setAlignment(Pos.CENTER);
                        rectangle.getChildren().add(text);


                        rectangle.setScaleShape(false);

                        rectangle.setCacheShape(false);

                        node.translateXProperty().setValue((getLength(dataItem.getExtraValue())/2.0) * ((getXAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis)getXAxis()).getScale()) : 1));
                        node.translateYProperty().setValue((nodeHeight)/2.0 * ((getYAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis)getYAxis()).getScale()) : 1));
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
            if (axisX.isAutoRanging()){
                axisX.invalidateRange(dataX);
            }
            if (axisY.isAutoRanging()){
                axisY.invalidateRange(dataY);
            }

        }
    }

    private static int getLength(Object o){
        return ((DetailedInformation) o).getLength();
    }

    private static String getLabel(Object o){
        return ((DetailedInformation)o).getLabel();
    }

    private static String getStyleClass( Object obj) {
        return ((SchedulingVisualization.DetailedInformation) obj).getStyleSheet();
    }

    private Node createNodeVisual(Data<X, Y> data){

        Node container = data.getNode();

        if (container == null){
            container = new StackPane();
            data.setNode(container);
        }

        container.getStyleClass().add( getStyleClass( data.getExtraValue()));
        return container;
    }

    public void setBlockHeight( double blockHeight) {
        this.nodeHeight = blockHeight;
    }
}

