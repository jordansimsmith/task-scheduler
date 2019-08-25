package task.scheduler.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runs in its own thread to monitor average CPU usage using MPStat
 * Sends back to visualisation
 */
public class UIOrchestratorCPUMonitor implements Runnable {
    private IVisualization visualization;
    private static final Logger logger = LoggerFactory.getLogger(UIOrchestratorCPUMonitor.class);

    public UIOrchestratorCPUMonitor(IVisualization visualization) {
        this.visualization = visualization;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            List<Double> cpuLoad = new LinkedList<>();
            // Ask mpstat for a 1 second average
            ProcessBuilder cpuPB = new ProcessBuilder("/bin/bash", "-c", "mpstat -P ALL 1 1");
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
                Pattern cpuLineMatcher = Pattern.compile("^Average:\\W*[0-9]*(\\W*[0-9]*\\.[0-9]*]*)*([0-9]*)$");
                while (line != null)  {
                    Matcher m = cpuLineMatcher.matcher(line);
                    if (m.matches())    {
                        cpuLoad.add((100 - Double.parseDouble(m.group(1)))/100);
                    }
                    line = reader.readLine();
                }

                try {
                    this.visualization.pushCPUUsage(cpuLoad);
                } catch (NullPointerException e) {
                    return; // If visualisation has closed, app is closed and we should just shut down gracefully
                }
            } catch (IOException e) {
                logger.error("Error while polling CPU statistics");
                e.printStackTrace();
            } catch (InterruptedException e)    {
                logger.info("UI CPU Monitor shutting down.");
                return;
            }
        }
    }
}
