package task.scheduler.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import task.scheduler.App;
import task.scheduler.graph.IGraph;
import task.scheduler.schedule.ISchedule;
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
    private int interval;

    private IVisualization visualization;

    /**
     * @param watchedScheduler Scheduler to poll for information
     * @param interval Period (in ms) to poll
     */
    public UIOrchestrator(IScheduler watchedScheduler, IVisualization visualization, int interval)   {
        this.watchedScheduler = watchedScheduler;
        this.interval = interval;
        this.visualization = visualization;
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
                logger.info("UI Thread received interrupt, will push once and shut down.");
                this.businessLogic();
                break;
            }

            businessLogic();

            // Sleep for interval, less time used in loop
            try {
                long sleep = interval - (System.currentTimeMillis() - start);
                Thread.sleep(sleep > 0 ? sleep : 1);
            } catch (InterruptedException e)    {
                this.businessLogic();
                logger.info("UI Thread received interrupt, will push once and shut down.");
                this.businessLogic();
                break;
            }

            IScheduler.SchedulerState currentState = this.watchedScheduler.getCurrentState();
            if (currentState == IScheduler.SchedulerState.STOPPED || currentState == IScheduler.SchedulerState.FINISHED) {
                break;
            }
        }
    }

    /**
     * Polls execution thread(s) for information, and provides to UI
     */
    private void businessLogic() {
        visualization.pushState(watchedScheduler.getCurrentState());
        visualization.pushSchedule(watchedScheduler.getCurrentSchedule(), watchedScheduler.getSchedulesSearched());

        List<Double> cpuLoad = new LinkedList<>();
        ProcessBuilder cpuPB = new ProcessBuilder("/bin/bash", "-c", "mpstat -A");
        try {
            Process cpuP = cpuPB.start();
            cpuP.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader((cpuP.getInputStream())));

            // Skip mpstat head
            reader.readLine();
            reader.readLine();
            reader.readLine();
            // Skip all summary
            reader.readLine();

            // Read CPU lines
            String line = reader.readLine();
            Pattern cpuLineMatcher = Pattern.compile("^[0-9]*:[0-9]*:[0-9]*\\W*[0-9]*(\\W*[0-9]*.[0-9]*]*)*([0-9]*)$");
            while (line != null)  {
                Matcher m = cpuLineMatcher.matcher(line);
                if (m.matches())    {
                    cpuLoad.add((100 - Double.parseDouble(m.group(1)))/100);
                } else {
                    break;
                }

                line = reader.readLine();
            }

        } catch (IOException | InterruptedException e) {
            logger.error("Error while polling CPU statistics");
            e.printStackTrace();
        }

        Runtime runtime = Runtime.getRuntime();
        visualization.pushStats(runtime.totalMemory() - runtime.freeMemory(), cpuLoad);
    }
}
