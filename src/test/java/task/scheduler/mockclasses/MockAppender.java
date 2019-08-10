package task.scheduler.mockclasses;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * A MockLogger stores logged items internally to be asserted.
 * Standard and error output are both treated the same
 */
public class MockAppender extends AppenderSkeleton {
    private final List<LoggingEvent> loggedItems;

    public MockAppender() {
        loggedItems = new ArrayList<>();
    }

    /**
     * Logs an item to be inspected later
     */
    @Override
    protected void append(LoggingEvent loggingEvent) {
        loggedItems.add(loggingEvent);
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    /**
     * @return the list of LoggingEvents logged by this appender
     */
    public List<LoggingEvent> getLoggedItems(){
        return loggedItems;
    }
}