package task.scheduler.mockclasses;

import task.scheduler.common.ILogger;

import java.util.ArrayList;
import java.util.List;

/**
 * A MockLogger stores logged items internally to be asserted.
 * Standard and error output are both treated the same
 */
public class MockLogger implements ILogger {
    private List<String> loggedItems;

    public MockLogger() {
        loggedItems = new ArrayList<>();
    }

    /**
     * Logs an item to be inspected later
     */
    @Override
    public void log(String message) {
        loggedItems.add(message);
    }

    /**
     * Same as log() in this implementation
     */
    @Override
    public void error(String error) {
        loggedItems.add(error);
    }

    /**
     * Returns list of logged items (stdout and stderr).
     * Does not clear the list (see clearLoggedItems())
     */
    public List<String> getLoggedItems() {
        return loggedItems;
    }

    public void clearLoggedItems() {
        loggedItems.clear();
    }
}
