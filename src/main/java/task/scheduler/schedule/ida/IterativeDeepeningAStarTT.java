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

public class IterativeDeepeningAStarTT implements IScheduler {
    private static final Logger logger = LoggerFactory.getLogger(IterativeDeepeningAStarTT.class);

    private static final int NOT_FOUND = -1;
    private static final int FOUND = -2;
    private IGraph graph;
    private Schedule answer;
    private Map<String, Integer> transpositionTable = new HashMap<>();

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


        Schedule initialState = new Schedule(graph.getStartNodes(), getParentCountMap(graph));
        int limit = initialState.getHeuristicValue();

        while(true) {
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

    private int lookUp(Schedule childState) {
        if (transpositionTable.containsKey(childState.getScheduleString())){
            return transpositionTable.get(childState.getScheduleString());
        } else {
            transpositionTable.put(childState.getScheduleString(), childState.getHeuristicValue());
            return childState.getHeuristicValue();
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
