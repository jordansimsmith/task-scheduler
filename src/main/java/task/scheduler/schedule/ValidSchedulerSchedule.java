package task.scheduler.schedule;

import task.scheduler.common.Tuple;
import task.scheduler.graph.INode;

import java.util.HashMap;
import java.util.Map;


/**
 * Schedule class for the Valid Algorithm.
 */
public class ValidSchedulerSchedule implements ISchedule {
    private Map<INode, Tuple<Integer, Integer>> schedule;
    private int cost = 0;

    public ValidSchedulerSchedule() {
        this.schedule = new HashMap<>();
    }

    public void addSchedule(INode node, int startTime, int processor) {
        schedule.put(node, new Tuple<>(startTime, processor));
        cost = Math.max(node.getProcessingCost() + startTime, cost);
    }

    @Override
    public int getTotalCost() {
        return cost;
    }

    public int getScheduleSize() {
        return schedule.size();
    }

    @Override
    public Tuple<Integer, Integer> getNodeSchedule(INode node) {
        return this.schedule.get(node);
    }
}
