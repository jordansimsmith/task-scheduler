package task.scheduler.schedule;


import task.scheduler.graph.IGraph;

/**
 * Interface to define what each scheduler will need to have.
 */
public interface IScheduler {

    /**
     * Each scheduler will need an execute method which will be responsible for returning the schedule
     *
     * @return Schedule which is built according to the scheduling algorithm
     */
    ISchedule execute(IGraph graph);

    /**
     * Schedulers search the search space, schedule by schedule. getCurrentSchedule gets the current
     * schedule being considered. This method should be safe to invoke from another thread.
     *
     * @return the current partial schedule.
     */
    ISchedule getCurrentSchedule();

    /**
     * Returns the number of states/schedules searched. This method should be safe to invoke from another thread.
     *
     * @return the number of schedules searched so far.
     */
    int getSchedulesSearched();
}
