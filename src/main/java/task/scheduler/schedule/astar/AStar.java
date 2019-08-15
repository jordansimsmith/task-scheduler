package task.scheduler.schedule.astar;

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

public class AStar implements IScheduler {
    private static final Logger logger = LoggerFactory.getLogger(AStar.class);

    private ISchedule currentSchedule;
    private int schedulesSearched;

    public AStar() {
    }

    @Override
    public ISchedule execute(IGraph graph) {
        // populate global state
        SchedulerState.populateTotalNodeWeighting(graph);
        SchedulerState.populateSortedNodes(graph);
        SchedulerState.populateBottomLevelCache(graph);

        Queue<Schedule> open = new PriorityQueue<>();
        Set<String> closed = new HashSet<>();

        open.add(new Schedule(graph.getStartNodes(), getParentCountMap(graph)));

        while (!open.isEmpty()) {
            Schedule s = open.peek();
            open.remove(s);

            if (s.getScheduledNodeCount() == graph.getNodeCount()) {
                logger.info("ASTAR searched " + this.schedulesSearched + " states");
                return s; // optimal schedule found
            }

            for (INode node : s.getFree()) {
                for (int i = 1; i <= Config.getInstance().getNumberOfCores(); i++) {
                    Schedule child = s.expand(node, i);

                    // do not add duplicate states to the priority queue
                    if (!closed.contains(child.getScheduleString())) {
                        open.add(child);
                        closed.add(child.getScheduleString());
                        this.schedulesSearched++;
                        this.currentSchedule = child;
                    }
                }
            }
        }
        return null;
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
}
