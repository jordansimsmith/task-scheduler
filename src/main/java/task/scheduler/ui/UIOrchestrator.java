package task.scheduler.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import task.scheduler.schedule.IScheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The UI Orchestrator runs in its own thread, and is responsible for polling the algorithm execution thread(s) for
 * updated execution status and pushing that to the UI.
 * Stops in response to interruption
 */
public class UIOrchestrator implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(UIOrchestrator.class);

    private IScheduler watchedScheduler;
    private Thread cpuMonitor;
    private int interval;

    private boolean schedulerRunning;

    private IVisualization visualization;

    /**
     * @param watchedScheduler Scheduler to poll for information
     * @param interval Period (in ms) to poll
     */
    public UIOrchestrator(IScheduler watchedScheduler, IVisualization visualization, int interval)   {
        this.watchedScheduler = watchedScheduler;
        this.interval = interval;
        this.visualization = visualization;
        this.schedulerRunning = true;

        // Launch CPU Monitor in seperate thread
        this.cpuMonitor = new Thread(new UIOrchestratorCPUMonitor(this.visualization));
        this.cpuMonitor.start();
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
                logger.info("UI Thread received interrupt, will shut down.");
                cpuMonitor.interrupt();
                break;
            }

            businessLogic();

            // Sleep for interval, less time used in loop
            try {
                long sleep = interval - (System.currentTimeMillis() - start);
                Thread.sleep(sleep > 0 ? sleep : 1);
            } catch (InterruptedException e)    {
                logger.info("UI Thread received interrupt, will shut down.");
                cpuMonitor.interrupt();
                break;
            }

            IScheduler.SchedulerState currentState = this.watchedScheduler.getCurrentState();
            if (currentState == IScheduler.SchedulerState.STOPPED || currentState == IScheduler.SchedulerState.FINISHED) {
                this.schedulerRunning = false;
            }
        }
    }

    /**
     * Polls execution thread(s) for information, and provides to UI
     */
    private void businessLogic() {
        if (schedulerRunning) {
            visualization.pushSchedule(watchedScheduler.getCurrentSchedule(), watchedScheduler.getSchedulesSearched());
        }
        visualization.pushState(watchedScheduler.getCurrentState());
        Runtime runtime = Runtime.getRuntime();
        visualization.pushMemoryUsage(runtime.totalMemory() - runtime.freeMemory());
    }
}
