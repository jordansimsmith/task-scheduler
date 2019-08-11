package task.scheduler.common;

/**
 * An ILogger logs messages
 */
public interface ILogger {
    /**
     * Output interface for standard out
     */
    void log(String message);

    /**
     * Output interface for error output
     */
    void error(String error);
}
