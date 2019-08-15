package task.scheduler.schedule.bnb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import task.scheduler.common.Config;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;
import task.scheduler.schedule.Schedule;
import task.scheduler.schedule.SchedulerState;

import java.util.*;

public class BNB implements IScheduler {
    private static final Logger logger = LoggerFactory.getLogger(BNB.class);

    private ISchedule currentSchedule;
    private int schedulesSearched;

    public BNB() {
    }

    @Override
    public ISchedule execute(IGraph graph) {
        // populate global state
        SchedulerState.populateTotalNodeWeighting(graph);
        SchedulerState.populateSortedNodes(graph);
        SchedulerState.populateBottomLevelCache(graph);

        // initialise BNB DFS variables
        Stack<Schedule> stack = new Stack<>();
        Set<String> seenSchedules = new HashSet<>();
        int upperBound = Integer.MAX_VALUE;
        Schedule bestSchedule = null;

        // add empty state to the stack
        stack.push(new Schedule(graph.getStartNodes(), getParentCountMap(graph)));

        // dfs bnb algorithm
        while (!stack.empty()) {
            Schedule s = stack.pop();

            // compare complete schedule
            if (s.getScheduledNodeCount() == graph.getNodeCount()) {
                if (s.getTotalCost() < upperBound) {
                    upperBound = s.getTotalCost();
                    bestSchedule = s;
                    this.currentSchedule = s;
                }
            } else {
                for (INode node : s.getFree()) {
                    // expansion
                    for (int p = 1; p <= Config.getInstance().getNumberOfCores(); p++) {
                        Schedule child = s.expand(node, p);

                        // pruning
                        if (child.getTotalCost() <= upperBound) {

                            // duplicate detection
                            if (!seenSchedules.contains(child.getScheduleString())) {
                                seenSchedules.add(child.getScheduleString());
                                stack.push(child);
                                this.schedulesSearched++;
                            }
                        }
                    }
                }
            }
        }

        logger.info("BNB searched " + this.schedulesSearched + " states");
        return bestSchedule;
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
}
