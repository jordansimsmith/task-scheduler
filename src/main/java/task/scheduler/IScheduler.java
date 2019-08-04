package task.scheduler;


/**
 * Interface to define what each scheduler will need to have.
 * */
public interface IScheduler {

    /**
     * Each scheduler will need an execute method which will be responsible for returning the schedule
     *
     * @return Schedule which is built according to the scheduling algorithm
     * */
    ISchedule execute();
}
