package task.scheduler.common;

import task.scheduler.common.ILogger;

/**
 * A ConsoleLogger writes messages to the console / standard output
 */
public class ConsoleLogger implements ILogger {

    @Override
    public void log(String message) {
        System.out.println(message);
    }
}
