package task.scheduler;

/**
 * A ConsoleLogger writes messages to the console / standard output
 */
public class ConsoleLogger implements ILogger {

    @Override
    public void log(String message) {
        System.out.println(message);
    }
}
