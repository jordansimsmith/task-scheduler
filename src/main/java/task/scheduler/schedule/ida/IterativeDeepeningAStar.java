package task.scheduler.schedule.ida;

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

public class IterativeDeepeningAStar implements IScheduler {
    private static final Logger logger = LoggerFactory.getLogger(IterativeDeepeningAStar.class);
    private static final int FOUND = -2;
    private IGraph graph;
    private int searchCount = 0;
    private static SchedulerState state = SchedulerState.NOT_STARTED;

    private ISchedule currentSchedule;
    private int schedulesSearched;

    public IterativeDeepeningAStar() {
    }

    @Override
    public ISchedule execute(IGraph graph) {
        SchedulerCache.populateSortedNodes(graph);
        SchedulerCache.populateBottomLevelCache(graph);
        SchedulerCache.populateSortedNodes(graph);
        state = SchedulerState.RUNNING;

        this.graph = graph;


        Schedule initialState = new Schedule(graph.getStartNodes(), getParentCountMap(graph));
        int limit = initialState.getHeuristicValue();
        Stack<Schedule> stack = new Stack<>();
        Set<String> closed = new HashSet<>();
        stack.push(initialState);
        closed.add(initialState.getScheduleString());

        while(!stack.isEmpty()) {
            closed.clear();
            int result = DepthLimitedSearchRecursive(stack, closed, limit);

            if(result == FOUND){
                logger.info(searchCount + " states searched");
                return stack.peek();
            }

            if(result == Integer.MAX_VALUE) {
                return null;
            }

            limit = result;
        }
        return null;
    }

    private int DepthLimitedSearchRecursive(Stack<Schedule> stack, Set<String> closed, int limit) {
        Schedule currentState = stack.peek();
        int f = currentState.getHeuristicValue();

        // exit depth limit search if we have reached the limit
        if ( f > limit) {
            return f;
        }

        // goal test
        if (currentState.getScheduledNodeCount() == graph.getNodeCount()) {
            return FOUND;
        }

        int min = Integer.MAX_VALUE;

        for (INode node : currentState.getFree()) {
            for (int i = 1; i <= Config.getInstance().getNumberOfCores(); i++) {
                Schedule child = currentState.expand(node, i);
                if (!closed.contains(child.getScheduleString())) {
                    closed.add(child.getScheduleString());
                    stack.push(child);
                    int t = DepthLimitedSearchRecursive(stack, closed, limit);

                    if ( t == FOUND) {
                        return FOUND;
                    }

                    min = Math.min(t, min);
                    stack.pop();
                    searchCount++;
                }
            }
        }
        return min;
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
        return this.currentSchedule;
    }

    @Override
    public int getSchedulesSearched() {
        return this.schedulesSearched;
    }

    @Override
    public SchedulerState getCurrentState() {
        return null;
    }

}
