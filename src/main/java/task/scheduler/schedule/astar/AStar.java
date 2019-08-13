package task.scheduler.schedule.astar;

import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class AStar implements IScheduler {
    private PriorityQueue<AStarSchedule> solutions;

    public static final int totalNodeWeighting = 0; // TODO: implement this
    public static final Map<INode, Integer> bottomLevelCache = new HashMap<>(); // TODO: implement this

    public AStar() {

    }

    @Override
    public ISchedule execute(IGraph graph) {
        throw new RuntimeException("not implemented");
    }
}
