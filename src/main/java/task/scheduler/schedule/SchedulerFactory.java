package task.scheduler.schedule;

/**
 * The class SchedularFactory returns a scheduler object as specified by the SchedulerType input to createSchedular
 * method
 */
public class SchedulerFactory {

    public enum SchedulerType {
        VALID
    }

    public SchedulerFactory() {
    }

    /**
     * ISchedular takes in a SchedularType and returns a corresponding scheduler.
     *
     * @param type
     * @return IScheduler
     */
    public IScheduler createScheduler(SchedulerType type) {
        if (type == null) {
            throw new IllegalArgumentException("Null is not a valid argument");
        }
        switch (type) {
            case VALID:
                return new ValidScheduler();

            default:
                throw new RuntimeException("createSchedular case not implemented for " + type);

        }
    }

}


