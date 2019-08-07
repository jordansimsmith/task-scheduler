package task.scheduler.common;

import task.scheduler.common.ILogger;

/**
 * A ConsoleLogger writes messages to the console / standard output and standard error
 */
public class ConsoleLogger implements ILogger {

    @Override
    public void log(String message) {
        System.out.println(message);
    }

    @Override
    public void error(String error) {
        System.err.println(error);
    }
}
