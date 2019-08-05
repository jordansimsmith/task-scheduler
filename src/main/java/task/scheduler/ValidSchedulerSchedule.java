package task.scheduler;

import java.util.HashMap;
import java.util.Map;


/**
 * Schedule class for the Valid Algorithm.
 */
public class ValidSchedulerSchedule implements ISchedule {
    private Map<INode, Tuple<Integer, Integer>> schedule;

    public ValidSchedulerSchedule() {
        this.schedule = new HashMap<>();
    }

    public void addSchedule(INode node, int startTime, int processor) {
        schedule.put(node, new Tuple<Integer, Integer>(processor, startTime));
    }

    public int getScheduleSize() {
        return schedule.size();
    }

    @Override
    public Tuple<Integer, Integer> getNodeSchedule(INode node) {
        return this.schedule.get(node);
    }
}
