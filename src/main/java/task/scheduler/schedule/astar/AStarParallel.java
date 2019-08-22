package task.scheduler.schedule.astar;

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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AStarParallel implements IScheduler {
    private static final Logger logger = LoggerFactory.getLogger(AStar.class);
    private static SchedulerState state = SchedulerState.NOT_STARTED;

    // TODO: Try with different sync threshold
    public static final int SYNC_THRESHOLD = 100000;
    // TODO: Try without duplicate detection
    public static final boolean DUPLICATE_DETECTION = true;
    private static final int numberOfWorkers = Config.getInstance().getNumberOfThreads();
    // TODO: optimise to access int for cost instead
    private static AtomicReference<Schedule> bestSolution = new AtomicReference<>();
    private static AtomicInteger idleWorkerCount = new AtomicInteger(0);
    private static PriorityBlockingQueue<Schedule>[] priorityQueues;
    private static boolean[] hasWork;
    private static final Set<String> closed = ConcurrentHashMap.newKeySet();


    private ISchedule currentSchedule;
    private int schedulesSearched;
    private IGraph graph;

    public AStarParallel() {
    }

    private Void aStarWorker(int id) {
        PriorityBlockingQueue<Schedule> localPriorityQueue = priorityQueues[id];
        int localBestKnown = bestSolution.get().getTotalCost();
        int syncCounter = 0;

        if(DUPLICATE_DETECTION) {
            for(Object schedule : localPriorityQueue) {
                closed.add(((Schedule) schedule).getScheduleString());
            }
        }

        while (idleWorkerCount.intValue() < numberOfWorkers) {
            while (!localPriorityQueue.isEmpty()) {
                Schedule currentState = localPriorityQueue.poll();
                syncCounter++;

                if (syncCounter == SYNC_THRESHOLD) {
                    // TODO: Check, should be the same as getTotalCost for a complete solution
                    localBestKnown = bestSolution.get().getHeuristicValue();
                    syncCounter = 0;
                }

                if(currentState.getHeuristicValue() < localBestKnown) {
                    if(currentState.getScheduledNodeCount() == graph.getNodeCount()) {
                        bestSolution.set(currentState);
                        // TODO: not in the pseudocode, but worth?
                        localBestKnown = currentState.getHeuristicValue();
                    }

                    for (INode node : currentState.getFree()) {
                        for (int i = 1; i <= Config.getInstance().getNumberOfCores(); i++) {
                            Schedule child = currentState.expand(node, i);

                            if(DUPLICATE_DETECTION) {
                                if (!closed.contains(child.getScheduleString())) {
                                    closed.add(child.getScheduleString());
                                    localPriorityQueue.add(child);
                                }
                            } else {
                                localPriorityQueue.add(child);
                            }

                        }
                    }

                } else {
                    // No more useful work
                    localPriorityQueue.clear();
                }
            }

            // work stealing section
            hasWork[id] = false;
            idleWorkerCount.incrementAndGet();

            // TODO: improve random number generation
            Random r = new Random();
            int victimThreadId = r.nextInt(numberOfWorkers);

            if (hasWork[victimThreadId]) {
                Schedule stolenState = priorityQueues[victimThreadId].poll();
                localPriorityQueue.add(stolenState);
                hasWork[id] = true;
                idleWorkerCount.decrementAndGet();
            }
        }
        return null;
    }

    @Override
    public ISchedule execute(IGraph graph) {
        // populate global state
        SchedulerCache.populateTotalNodeWeighting(graph);
        SchedulerCache.populateSortedNodes(graph);
        SchedulerCache.populateBottomLevelCache(graph);

        this.graph = graph;

        state = SchedulerState.RUNNING;

        Schedule empty = new Schedule(graph.getStartNodes(), getParentCountMap(graph));

        Set<Schedule> seedStates = new HashSet<>();
        seedStates.add(empty);
        while (seedStates.size() < numberOfWorkers) {
            Schedule s = seedStates.iterator().next();
            seedStates.remove(s);

            for (INode node : s.getFree()) {
                for (int p = 1; p < Config.getInstance().getNumberOfCores(); p++) {

                    Schedule child = s.expand(node ,p);
                    seedStates.add(child);
                    closed.add(child.getScheduleString());
                }
            }
        }

        logger.info("expanded to seed states");

        List<Schedule> seedStateList = new ArrayList<>(seedStates);
        List<Set<Schedule>> decomposedList = new ArrayList<>();
        for (int i = 0; i < Config.getInstance().getNumberOfThreads(); i++) {
            decomposedList.add(new HashSet<>());
        }
        for (int i = 0; i < seedStateList.size(); i++) {
            Schedule s = seedStateList.get(i);

            int index = i % Config.getInstance().getNumberOfThreads();
            decomposedList.get(index).add(s);
        }

        logger.info("placed seed states into sets");

        priorityQueues = new PriorityBlockingQueue[numberOfWorkers];
        for(int i = 0; i < priorityQueues.length; i++) {
            priorityQueues[i] = new PriorityBlockingQueue<>(decomposedList.get(i));
        }

        logger.info("placed seed states into priorityQueues");

        hasWork = new boolean[numberOfWorkers];
        Arrays.fill(hasWork, true);

        logger.info("set has work to true");

        bestSolution.set(priorityQueues[0].peek());
        logger.info("set initial bestSolution");

        // now we have enough items in open
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        List<Future<Void>> futures = new ArrayList<>();

        for (int workerId = 0; workerId < numberOfWorkers; workerId++) {
            int finalWorkerId = workerId;

            Future<Void> future = executor.submit(() -> aStarWorker(finalWorkerId));
            futures.add(future);
        }

        for (Future<Void> future : futures) {
            try {
                future.get();

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        state = SchedulerState.STOPPED;
        return bestSolution.get();
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
