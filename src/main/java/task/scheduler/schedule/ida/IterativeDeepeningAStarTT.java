package task.scheduler.schedule.ida;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import task.scheduler.common.Config;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;
import task.scheduler.schedule.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class IterativeDeepeningAStarTT implements IScheduler {
    private static final Logger logger = LoggerFactory.getLogger(IterativeDeepeningAStarTT.class);

    private static final int FOUND = -2;
    private IGraph graph;
    private Schedule answer;
    public static final Map<String, Integer> transpositionTable = new HashMap<>();
    private static SchedulerUtils schedulerUtils = new SchedulerUtils();


    private ISchedule currentSchedule;
    private int schedulesSearched;
    public IterativeDeepeningAStarTT() {
    }

    @Override
    public ISchedule execute(IGraph graph) {
        SchedulerCache.populateTotalNodeWeighting(graph);
        SchedulerCache.populateBottomLevelCache(graph);
        SchedulerCache.populateSortedNodes(graph);
        this.graph = graph;

        Schedule initialState = new Schedule(graph.getStartNodes(), schedulerUtils.getParentCountMap(graph));
        int limit = initialState.getHeuristicValue();
        Stack<Schedule> stack = new Stack<>();


        while(true) {
            stack.push(initialState);
            int result = DepthLimitedSearchRecursive(initialState, limit);

            if(result == FOUND){
                logger.info(schedulesSearched + " states searched");
                return answer;
            }

            if(result == Integer.MAX_VALUE) {
                return null;
            }

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

                    // check if this state has already been expanded beyond this limit
                    if (lookUp(child) <= limit) {
                        stack.push(child);
                        this.schedulesSearched++;
                    } else {
                        min = Math.min(lookUp(child), min);
                    }
                }
            }
            transpositionTable.put(currentState.getScheduleString(), min);
        }
        return min;
    }

    /**
     * Expands the state tree of the given Schedule but stops expansion
     * once the given limit has been reached. This method uses a recursive approach so
     * requires more memory.
     *
     * @param currentState the schedule at which to begin tree expansion
     * @param limit        the max f-value to which to probe to
     * @return the minimum f-value that exceeded the given limit
     */
    private int DepthLimitedSearchRecursive(Schedule currentState, int limit) {
        this.schedulesSearched++;
        if (currentState.getScheduledNodeCount() == graph.getNodeCount()) {
            answer = currentState;
            return FOUND;
        }

        int min = Integer.MAX_VALUE;

        for (INode node : currentState.getFree()) {
            for (int i = 1; i <= Config.getInstance().getNumberOfCores(); i++) {
                Schedule childState = currentState.expand(node, i);
                int t;

                // check if this state has already been expanded beyond this limit
                if (lookUp(childState) <= limit) {
                    t = DepthLimitedSearchRecursive(childState, limit);
                } else {
                    t = lookUp(childState);
                }

                if (t == FOUND) {
                    return FOUND;
                }

                min = Math.min(t, min);
            }
        }

        transpositionTable.put(currentState.getScheduleString(), min);
        return min;
    }

    /**
     * Looks for the corresponding value of the given Schedule in the transposition table.
     * The value returned is the min f-value that was returned by by doing a depth-limited
     * search on the given schedule. If no value is found, its f-value is stored in the
     * transposition table.
     *
     * @param childState to find in the transposition table
     * @return the f-value found in the transposition table
     */
    private int lookUp(Schedule childState) {
        if (transpositionTable.containsKey(childState.getScheduleString())){
            return transpositionTable.get(childState.getScheduleString());
        } else {
            transpositionTable.put(childState.getScheduleString(), childState.getHeuristicValue());
            return childState.getHeuristicValue();
        }
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
