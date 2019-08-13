package task.scheduler.ui;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;

public class SchedulingVisualization<X, Y> extends XYChart<X, Y> {

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

    }

    @Override
    protected void seriesRemoved(Series<X, Y> series) {

    }

    @Override
    protected void layoutPlotChildren() {

    }

    private Node createNodeVisual(Data<X, Y> data){

        Node container = data.getNode();

        if (container == null){
            container = new StackPane();
            data.setNode(container);
        }

        return container;
    }
}
