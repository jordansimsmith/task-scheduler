package task.scheduler.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import task.scheduler.App;
import task.scheduler.schedule.IScheduler;

/**
 * The UI Orchestrator runs in its own thread, and is responsible for polling the algorithm execution thread(s) for
 * updated execution status and pushing that to the UI.
 * Stops in response to interruption
 */
public class UIOrchestrator implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private IScheduler watchedScheduler;
    private int interval;

    /**
     * @param watchedScheduler Scheduler to poll for information
     * @param interval Period (in ms) to poll
     */
    public UIOrchestrator(IScheduler watchedScheduler, int interval)   {
        this.watchedScheduler = watchedScheduler;
        this.interval = interval;
    }

    /**
     * Starts and maintains execution of the UIOrchestrator thread
     * @see Thread#run()
     */
    @Override
    public void run() {
        while (true)    {
            long start = System.currentTimeMillis();
            if (Thread.currentThread().isInterrupted()) {
                logger.info("UI Thread received interrupt, shutting down.");
                break;
            }

            businessLogic();

            // Sleep for interval, less time used in loop
            try {
                long sleep = interval - (System.currentTimeMillis() - start);
                Thread.sleep(sleep > 0 ? sleep : 1);
            } catch (InterruptedException e)    {
                logger.info("UI Thread received interrupt, breaking.");
                break;
            }

        }
    }

    /**
     * Polls execution thread(s) for information, and provides to UI
     */
    private void businessLogic() {
        logger.info(watchedScheduler.getCurrentState().toString());
    }
}
