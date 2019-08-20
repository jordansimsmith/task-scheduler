package task.scheduler.schedule.bnb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import task.scheduler.common.Config;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;
import task.scheduler.schedule.Schedule;
import task.scheduler.schedule.SchedulerCache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class BNB implements IScheduler {
    private static final Logger logger = LoggerFactory.getLogger(BNB.class);

    private AtomicInteger upperBound = new AtomicInteger(Integer.MAX_VALUE);
    private AtomicReference<Schedule> bestSchedule = new AtomicReference<>();
    private Set<String> seenSchedules = ConcurrentHashMap.newKeySet();
    private AtomicInteger threadCount = new AtomicInteger(1);
    private Set<Thread> threads = ConcurrentHashMap.newKeySet();

    private ISchedule currentSchedule;
    private int schedulesSearched;

    public BNB() {
    }

    @Override
    public ISchedule execute(IGraph graph) {
        // populate global state
        SchedulerCache.populateTotalNodeWeighting(graph);
        SchedulerCache.populateSortedNodes(graph);
        SchedulerCache.populateBottomLevelCache(graph);

        // initialise BNB DFS variables
        Stack<Schedule> stack = new Stack<>();

        // add empty state to the stack
        stack.push(new Schedule(graph.getStartNodes(), getParentCountMap(graph)));

        // dfs bnb algorithm
        executeBNB(stack, graph);

        // wait for threads
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // success
        logger.info("BNB searched " + this.schedulesSearched + " states");
        return this.bestSchedule.get();
    }

    private void executeBNB(Stack<Schedule> stack, IGraph graph) {
        while (!stack.empty()) {
            Schedule s = stack.pop();

            // compare complete schedule
            if (s.getScheduledNodeCount() == graph.getNodeCount()) {
                if (s.getTotalCost() < this.upperBound.get()) {
                    this.upperBound.set(s.getTotalCost());
                    this.bestSchedule.set(s);
                    this.currentSchedule = s;
                }
            } else {
                // expansion
                for (INode node : s.getFree()) {
                    for (int p = 1; p <= Config.getInstance().getNumberOfCores(); p++) {
                        Schedule child = s.expand(node, p);

                        // pruning
                        if (child.getTotalCost() <= this.upperBound.get()) {

                            // duplicate detection
                            if (!seenSchedules.contains(child.getScheduleString())) {
                                seenSchedules.add(child.getScheduleString());
                                this.schedulesSearched++;

                                // execute on new thread
                                if (this.threadCount.getAndIncrement() < Config.getInstance().getNumberOfThreads()) {
                                    Thread thread = new Thread(() -> {
                                        Stack<Schedule> newStack = new Stack<>();
                                        newStack.push(child);
                                        executeBNB(newStack, graph);
                                    });
                                    threads.add(thread);
                                    thread.start();
                                }
                                // execute on current thread
                                else {
                                    this.threadCount.decrementAndGet();
                                    stack.push(child);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns a map of INode to a parent count Integer. The parent count Integer represents the
     * remaining number of parents the INode has. The map contains this information for all INodes
     * of the given IGraph.
     *
     * @param graph for which to calculate the parentCountMap
     * @return a parentCountMap
     */
    private Map<INode, Integer> getParentCountMap(IGraph graph) {
        Map<INode, Integer> parentCount = new HashMap<>();

        for (INode node : graph.getNodes()) {
            parentCount.put(node, node.getParents().size());
        }
        return parentCount;
    }

    @Override
    public ISchedule getCurrentSchedule() {
        // returns the current best schedule, not the schedule currently processed
        return this.currentSchedule;
    }

    @Override
    public int getSchedulesSearched() {
        // gets the total amount of partial schedules processed
        return this.schedulesSearched;
    }

    @Override
    public SchedulerState getCurrentState() {
        return null;
    }
}
