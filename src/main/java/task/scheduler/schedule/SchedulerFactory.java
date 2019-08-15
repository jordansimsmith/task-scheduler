package task.scheduler.schedule;

import task.scheduler.schedule.astar.AStar;
import task.scheduler.schedule.bnb.BNB;
import task.scheduler.schedule.valid.ValidScheduler;

/**
 * The class SchedularFactory returns a scheduler object as specified by the SchedulerType input to createSchedular
 * method
 */
public class SchedulerFactory {

    public enum SchedulerType {
        VALID, ASTAR, BNB
    }

    public SchedulerFactory() {
    }

    /**
     * ISchedular takes in a SchedularType and returns a corresponding scheduler.
     */
    public IScheduler createScheduler(SchedulerType type) {
        if (type == null) {
            throw new IllegalArgumentException("Null is not a valid argument");
        }
        switch (type) {
            case VALID:
                return new ValidScheduler();
            case ASTAR:
                return new AStar();
            case BNB:
                return new BNB();
            default:
                throw new RuntimeException("createSchedular case not implemented for " + type);
        }
    }

}


