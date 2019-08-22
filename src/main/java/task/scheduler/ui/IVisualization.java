package task.scheduler.ui;

import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;

import java.util.List;

/**
 * An interface for any visualisation system, that takes the graph, schedule objects and draws them
 */
public interface IVisualization {
    /**
     * Pushes a new schedule to be rendered
     * @param schedule
     * @param schedulesSearched
     */
    void pushSchedule(ISchedule schedule, int schedulesSearched);

    /**
     * Pushes a new state to be rendered
     */
    void pushState(IScheduler.SchedulerState newState);

    /**
     * Pushes updated system resource usage information for graphs
     */
    void pushStats(double ramUsage, List<Double> cpuUsage);
}
