package task.scheduler.schedule.astar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import task.scheduler.common.Config;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;
import task.scheduler.schedule.*;

import java.util.*;
import java.util.concurrent.*;

public class AStar implements IScheduler {
    private static final Logger logger = LoggerFactory.getLogger(AStar.class);
    private static SchedulerState state = SchedulerState.NOT_STARTED;
    private static SchedulerUtils schedulerUtils = new SchedulerUtils();

    private ISchedule currentSchedule;
    private int schedulesSearched;

    public AStar() {
    }

    @Override
    public ISchedule execute(IGraph graph) {
        // populate global state
        SchedulerCache.populateTotalNodeWeighting(graph);
        SchedulerCache.populateSortedNodes(graph);
        SchedulerCache.populateBottomLevelCache(graph);
        state = SchedulerState.RUNNING;

        Queue<Schedule> open = new PriorityQueue<>();
        Set<String> closed = new HashSet<>();

        open.add(new Schedule(graph.getStartNodes(), schedulerUtils.getParentCountMap(graph)));

        int numThreads = Config.getInstance().getNumberOfThreads();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        while (!open.isEmpty()) {
            Schedule s = open.peek();
            open.remove(s);

            if (s.getScheduledNodeCount() == graph.getNodeCount()) {
                state = SchedulerState.FINISHED;
                currentSchedule = s;
                logger.info("ASTAR searched " + this.schedulesSearched + " states");
                executor.shutdown();
                return s; // optimal schedule found
            }


            // submit jobs
            List<Future<Schedule>> futures = new ArrayList<>();
            for (INode node : s.getFree()) {
                for (int i = 1; i <= Config.getInstance().getNumberOfCores(); i++) {
                    final int p = i;

                    Future<Schedule> future = executor.submit(() -> s.expand(node, p));
                    futures.add(future);
                }
            }

            // collect jobs
            for (Future<Schedule> future : futures) {
                try {
                    Schedule child = future.get();

                    // do not add duplicate states to the priority queue
                    if (!closed.contains(child.getScheduleString())) {
                        open.add(child);
                        closed.add(child.getScheduleString());
                        this.schedulesSearched++;
                        this.currentSchedule = child;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }


        state = SchedulerState.STOPPED;
        executor.shutdown();
        return null;
    }

    @Override
    public SchedulerState getCurrentState() {
        return this.state;
    }

    @Override
    public ISchedule getCurrentSchedule() {
        return this.currentSchedule;
    }

    @Override
    public int getSchedulesSearched() {
        return this.schedulesSearched;
    }
}
