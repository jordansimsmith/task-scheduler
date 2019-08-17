package task.scheduler.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;

public class PanelVisualisationFXApp extends Application {
    public static final CountDownLatch latch = new CountDownLatch(1);
    public static PanelVisualisationFXApp fxApp = null;

    public static PanelVisualisationFXApp waitForInit()    {
        try {
            latch.await();
        } catch (InterruptedException e)    {
            e.printStackTrace();
        }
        return fxApp;
    }

    public static void setFxApp(PanelVisualisationFXApp app)    {
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
