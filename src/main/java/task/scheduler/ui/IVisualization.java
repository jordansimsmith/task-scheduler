package task.scheduler.ui;

import task.scheduler.schedule.ISchedule;

/**
 * An interface for any visualisation system, that takes the graph, schedule objects and draws them
 */
public interface IVisualization {
    /**
     * Pushes a new schedule to be rendered
     * @param schedule
     */
    public void pushSchedule(ISchedule schedule);
}
