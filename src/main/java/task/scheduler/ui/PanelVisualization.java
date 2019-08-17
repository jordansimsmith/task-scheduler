package task.scheduler.ui;

import task.scheduler.graph.IGraph;
import task.scheduler.schedule.ISchedule;

/**
 * Visualization of several panels as designed during planning
 * Takes a graph to visualize, and takes regular updates
 */
public class PanelVisualization implements  IVisualization {
    private IGraph graph;

    // Visualization panels
    private SchedulingVisualizationAdapter schedulingVisualizationAdapter;
    private InputGraphGenerator inputGraphGenerator;

    // Stage
    PanelVisualisationFXApp fxApp;

    /**
     * Graph to be visualised
     * @param graph
     */
    public PanelVisualization(IGraph graph) {
        new Thread()    {
            @Override
            public void run()   {
                javafx.application.Application.launch(PanelVisualisationFXApp.class);
            }
        }.start();

        PanelVisualisationFXApp app = PanelVisualisationFXApp.waitForInit();
        app.launchFX();

        this.graph = graph;
        this.schedulingVisualizationAdapter = SchedulingVisualizationAdapter.getInstance();

    }

    /**
     * Pushes a new schedule to be rendered
     *
     * @param schedule
     */
    @Override
    public void pushSchedule(ISchedule schedule) {
        schedulingVisualizationAdapter.populateVisual(graph, schedule);
    }
}
