package task.scheduler.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import task.scheduler.graph.Graph;
import task.scheduler.graph.IGraph;
import task.scheduler.schedule.ISchedule;
import task.scheduler.common.Config;
import task.scheduler.schedule.IScheduler;

import java.util.concurrent.CountDownLatch;

/**
 * Encapsulates a JavaFX application that can be accessed after its creation
 */
public class PanelVisualisationFXApp extends Application {
    private static final CountDownLatch latch = new CountDownLatch(1);
    private static PanelVisualisationFXApp fxApp = null;

    private Stage stage;

    private IScheduler.SchedulerState schedulerState;

    // Views
    private Parent root;
    private SchedulingVisualizationAdapter scheduleVisualization;
    private InputGraphGenerator inputGraphGenerator;

    private XYChart.Series<Number, Number> ramUsage;
    private double startTime;

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
        schedulerState = IScheduler.SchedulerState.NOT_STARTED;
        setFxApp(this);
    }

    /**
     * Pushes an updated schedule to be rendered
     * Also renders graph the first time it is made available
     * @param graph
     * @param schedule
     */
    protected void pushSchedule(IGraph graph, ISchedule schedule)  {
        if (scheduleVisualization != null)  {
            scheduleVisualization.populateVisual(graph, schedule);
        }

        if (inputGraphGenerator == null)    {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    setUpInputView(root, graph);
                }
            });
        }
    }

    /**
     * Pushes the current state of the program to be displayed
     */
    protected void pushState(IScheduler.SchedulerState state)   {
        this.schedulerState = state;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.setTitle(getTitle());
            }
        });
    }

    /**
     * Pushes the current ram and cpu stats to be displayed
     */
    protected void pushStats(double ramUsageP, double cpuUsageP)   {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ramUsage.getData().add(new XYChart.Data<>(System.currentTimeMillis() - startTime, ramUsageP));
            }
        });
    }

    /**
     * FXML entry point
     */
    @Override
    public void start(Stage stage) throws Exception {
        root = FXMLLoader.load(getClass().getResource("/fxml/panel_view.fxml"));

        root.setStyle("-fx-background-color: white");

        Scene scene = new Scene(root, 1280, 720);
        stage.setMinWidth(480);
        stage.setMinHeight(360);
        stage.setScene(scene);

        setUpScheduleView(root);
        setUpProgressBar(root);
        setUpResourceViews(root);

        stage.setTitle(getTitle());
        stage.show();

        this.stage = stage;
        latch.countDown();
    }

    /**
     * Sets up CPU and RAM views
     */
    private void setUpResourceViews(Parent root) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time Ago");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(" % Utilisation ");

        LineChart<Number, Number> RAMChart = new LineChart<>(xAxis, yAxis);

        startTime = System.currentTimeMillis();
        ramUsage = new XYChart.Series<>();
        RAMChart.getData().add(ramUsage);

        AnchorPane ramWindow = (AnchorPane) root.lookup("#ram_usage");
        ramWindow.getChildren().add(RAMChart);

        RAMChart.prefWidthProperty().bind(ramWindow.widthProperty());
    }

    /**
     * Sets up progress bar
     * Bounds to appropriate size
     */
    private void setUpProgressBar(Parent root)  {
        ProgressBar progressBar = (ProgressBar) root.lookup("#progress_bar");

        progressBar.prefWidthProperty().bind(((BorderPane) progressBar.getParent()).widthProperty().multiply(0.8));
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

    /**
     * Generates and inserts input graph view
     */
    private void setUpInputView(Parent root, IGraph graph) {
        inputGraphGenerator = new InputGraphGenerator(graph);
        ImageView inputImage = inputGraphGenerator.getGraph();

        AnchorPane inputView = (AnchorPane) root.lookup("#input_preview");
        AnchorPane.setTopAnchor(inputImage, 0.0);
        AnchorPane.setBottomAnchor(inputImage, 0.0);
        AnchorPane.setLeftAnchor(inputImage, 0.0);
        AnchorPane.setRightAnchor(inputImage, 0.0);

        inputView.getChildren().add(inputImage);
        inputImage.fitHeightProperty().bind(inputView.heightProperty());
        inputImage.fitWidthProperty().bind(inputView.widthProperty());
    }

    /**
     * Gets appropriate title at any point in time
     * @return
     */
    private String getTitle()   {
        String title = "Visualisation of ";
        title += Config.getInstance().getInputFile().toString();
        title += " - ";

        switch (schedulerState) {
            case RUNNING:
                title += "Running";
                break;
            case NOT_STARTED:
                title += "Not Started";
                break;
            case FINISHED:
                title += "Finished";
                break;
            case STOPPED:
                title += "Stopped";
                break;
        }

        return title;
    }
}
