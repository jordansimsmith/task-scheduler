package task.scheduler.ui;

import task.scheduler.graph.IGraph;
import task.scheduler.schedule.ISchedule;

/**
 * Visualization of several panels as designed during planning
 * Takes a graph to visualize, and takes regular updates
 */
public class PanelVisualization implements  IVisualization {
    private IGraph graph;

    // Encapsulated JavaFX app
    private PanelVisualisationFXApp fxApp;

    /**
     * Graph to be visualised
     * @param graph
     */
    public PanelVisualization(IGraph graph) {
        this.graph = graph;
        fxApp = null;

        new Thread()    {
            @Override
            public void run()   {
                javafx.application.Application.launch(PanelVisualisationFXApp.class);
            }
        }.start();

        fxApp = PanelVisualisationFXApp.waitForInit();
    }

    /**
     * Pushes a new schedule to be rendered
     *
     * @param schedule
     */
    @Override
    public void pushSchedule(ISchedule schedule) {
        if (fxApp != null && schedule != null)  {
            fxApp.pushSchedule(graph, schedule);
        }
    }
}
