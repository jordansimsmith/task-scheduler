package task.scheduler.schedule;


import task.scheduler.graph.IGraph;

/**
 * Interface to define what each scheduler will need to have.
 */
public interface IScheduler {
    /**
     * State a scheduler is in.
     *  NOT_STARTED is prior to an execute() call,
     *  RUNNING is when the scheduler is doing work,
     *  STOPPED is implementation optional - if the scheduler pauses/fails to finish during exection,
     *  FINISHED is when a schedule has been produced
     */
    public enum SchedulerState  {
        NOT_STARTED,
        RUNNING,
        STOPPED,
        FINISHED
    }

    /**
     * Each scheduler will need an execute method which will be responsible for returning the schedule
     *
     * @return Schedule which is built according to the scheduling algorithm
     */
    ISchedule execute(IGraph graph);

    /**
     *  Returns the current state of the scheduler, should be threadsafe
     *  Schedulers are responsible for reporting their state, and may delay reporting for speed and efficiency
     */
    SchedulerState getCurrentState();

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
