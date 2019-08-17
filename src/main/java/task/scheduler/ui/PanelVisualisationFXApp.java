package task.scheduler.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
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
        latch.countDown();
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
        scheduleVisualization = SchedulingVisualizationAdapter.getInstance();
        Chart chart = scheduleVisualization.getChart();

        Scene scene = new Scene(chart, 720, 640);
        stage.setScene(scene);
        stage.show();
    }

}
