package task.scheduler.ui;

import javafx.application.Application;
import task.scheduler.graph.IGraph;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;

/**
 * Visualization of several panels as designed during planning
 * Takes a graph to visualize, and takes regular updates
 */
public class PanelVisualization implements  IVisualization {
    private IGraph graph;

    // Encapsulated JavaFX app
    PanelVisualisationFXApp fxApp;

    /**
     * Graph to be visualised
     * @param graph
     */
    public PanelVisualization(IGraph graph) {
        this.graph = graph;
        fxApp = null;

        new Thread(() -> Application.launch(PanelVisualisationFXApp.class)).start();

        fxApp = PanelVisualisationFXApp.waitForInit();
    }

    /**
     * Pushes a new schedule to be rendered
     *
     * @param schedule
     * @param schedulesSearched
     */
    @Override
    public void pushSchedule(ISchedule schedule, int schedulesSearched) {
        if (fxApp != null && schedule != null)  {
            fxApp.pushSchedule(graph, schedule, schedulesSearched);
        }
    }

    /**
     * Pushes a new state to be rendered
     */
    @Override
    public void pushState(IScheduler.SchedulerState newState) {
        if (fxApp != null) {
            fxApp.pushState(newState);
        }
    }

    /**
     * Pushes CPU/RAM usage information
     */
    @Override
    public void pushStats(double ram, double cpu)   {
        if (fxApp != null) {
            fxApp.pushStats(ram, cpu);
        }
    }

}
