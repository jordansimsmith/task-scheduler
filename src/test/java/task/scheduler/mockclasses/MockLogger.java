package task.scheduler.mockclasses;

import task.scheduler.ILogger;
import java.util.ArrayList;
import java.util.List;

/**
 * A MockLogger stores logged items internally to be asserted
 */
public class MockLogger implements ILogger {
    private List<String> loggedItems;

    public MockLogger(){
        loggedItems = new ArrayList<>();
    }

    @Override
    public void log(String message) {
        loggedItems.add(message);
    }

    public List<String> getLoggedItems() {
        return loggedItems;
    }

    public void clearLoggedItems(){
        loggedItems.clear();
    }
}
