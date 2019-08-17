package task.scheduler.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;

/**
 * Encapsulates a JavaFX application that can be accessed after its creation
 */
public class PanelVisualisationFXApp extends Application {
    private static final CountDownLatch latch = new CountDownLatch(1);
    private static PanelVisualisationFXApp fxApp = null;

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

    public void launchFX()  {
        System.out.println("hi");
    }

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
                 primary stages and will not be embedded in the browser.
     */
    @Override
    public void start(Stage stage) throws Exception {
        BorderPane pane = new BorderPane();
        Scene scene = new Scene(pane, 720, 640);
        stage.setScene(scene);
        stage.show();
    }

}
