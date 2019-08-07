package task.scheduler.common;

/**
 * An ILogger logs messages
 */
public interface ILogger {
    /**
     * Output interface for standard out
     * @param message
     */
    void log(String message);

    /**
     * Output interface for error output
     * @param error
     */
    void error(String error);
}
