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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class IterativeDeepeningAStar implements IScheduler {
    private static final Logger logger = LoggerFactory.getLogger(IterativeDeepeningAStar.class);
    private static final int FOUND = -2;
    private IGraph graph;
    private Schedule answer;
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

        // the iterative deepening loop, repeatedly applies the astar algorithm up to the f-value limit
        while(true) {
            stack.push(initialState);
            int result = DepthLimitedSearchIterative(stack, limit);

            if(result == FOUND){
                logger.info(schedulesSearched + " states searched");
                return answer;
            }

            if(result == Integer.MAX_VALUE) {
                logger.error("No complete solution was found.");
                return null;
            }

            // set new limit as the min f-value that exceeded the previous limit
            limit = result;
        }
    }

    /**
     * Expands the state tree of the Schedule(s) on the given stack but stops expansion
     * once the given limit has been reached.
     *
     * @param stack containing the Schedules being expanded
     * @param limit the max f-value to which to probe to
     * @return the minimum f-value that exceeded the given limit
     */
    private int DepthLimitedSearchIterative(Stack<Schedule> stack, int limit) {
        int min = Integer.MAX_VALUE;

        while(!stack.empty()) {
            Schedule currentState = stack.pop();
            int f = currentState.getHeuristicValue();

            // exit depth limit search if we have reached the limit
            if ( f > limit) {
                min = Math.min(min, f);
                continue;
            }

            // goal test
            if (currentState.getScheduledNodeCount() == graph.getNodeCount()) {
                answer = currentState;
                return FOUND;
            }

            for (INode node : currentState.getFree()) {
                for (int i = 1; i <= Config.getInstance().getNumberOfCores(); i++) {
                    Schedule child = currentState.expand(node, i);
                    stack.push(child);
                    this.schedulesSearched++;
                }
            }
        }
        return min;
    }

    /**
     * Expands the state tree of the Schedule(s) on the given stack but stops expansion
     * once the given limit has been reached. This method uses a recursive approach so
     * requires more memory.
     *
     * @param stack  containing the Schedules being expanded
     * @param closed the schedule strings of the Schedules that have already been visited
     * @param limit  the max f-value to which to probe to
     * @return the minimum f-value that exceeded the given limit
     */
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

                // do not revisit duplicate states
                if (!closed.contains(child.getScheduleString())) {
                    closed.add(child.getScheduleString());
                    stack.push(child);
                    int t = DepthLimitedSearchRecursive(stack, closed, limit);

                    if ( t == FOUND) {
                        return FOUND;
                    }

                    min = Math.min(t, min);
                    stack.pop();
                    this.schedulesSearched++;
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
