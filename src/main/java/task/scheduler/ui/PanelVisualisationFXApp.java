package task.scheduler.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import task.scheduler.graph.IGraph;
import task.scheduler.schedule.ISchedule;

import java.util.concurrent.CountDownLatch;

/**
 * Encapsulates a JavaFX application that can be accessed after its creation
 */
public class PanelVisualisationFXApp extends Application {
    private static final CountDownLatch latch = new CountDownLatch(1);
    private static PanelVisualisationFXApp fxApp = null;

    // Views
    private SchedulingVisualizationAdapter scheduleVisualization;

    /**
     * Waits until the JavaFX application is ready, then returns the app
     * WARNING: Does not init app, will hang permanently if app not initialized
     * @return
     */
    public static PanelVisualisationFXApp waitForInit()    {
        try {
            latch.await();
        } catch (InterruptedException e)    {
            e.printStackTrace();
        }
        return fxApp;
    }

    private static void setFxApp(PanelVisualisationFXApp app)    {
        fxApp = app;
    }

    public PanelVisualisationFXApp()  {
        setFxApp(this);
    }

    /**
     * Pushes an updated schedule to be rendered by FXML
     * @param graph
     * @param schedule
     */
    protected void pushSchedule(IGraph graph, ISchedule schedule)  {
        if (scheduleVisualization != null)  {
            scheduleVisualization.populateVisual(graph, schedule);
        }
    }

    /**
     * FXML entry point
     */
    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/panel_view.fxml"));

        Scene scene = new Scene(root, 1280, 720);
        stage.setMinWidth(480);
        stage.setMinHeight(360);
        stage.setScene(scene);

        setUpScheduleView(root);

        stage.show();

        latch.countDown();
    }

    /**
     * Inserts schedule in #schedule_preview anchor view
     * @param root
     */
    private void setUpScheduleView(Parent root)    {
        scheduleVisualization = SchedulingVisualizationAdapter.getInstance();
        Chart chart = scheduleVisualization.getChart();

        AnchorPane scheduleView = (AnchorPane) root.lookup("#schedule_preview");
        AnchorPane.setTopAnchor(chart, 0.0);
        AnchorPane.setBottomAnchor(chart, 0.0);
        AnchorPane.setLeftAnchor(chart, 0.0);
        AnchorPane.setRightAnchor(chart, 0.0);

        scheduleView.getChildren().add(chart);
    }

}
