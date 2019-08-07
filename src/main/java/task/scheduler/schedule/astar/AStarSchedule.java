package task.scheduler.schedule.astar;

import task.scheduler.common.Config;
import task.scheduler.common.Tuple;
import task.scheduler.graph.INode;
import task.scheduler.schedule.ISchedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AStarSchedule implements ISchedule {

    private List<INode> schedulable;
    private Map<INode, Tuple<Integer, Integer>> scheduled;

    private int[] earliestTimes;

    public AStarSchedule() {
        this.schedulable = new ArrayList<>();
        this.scheduled = new HashMap<>();
        this.earliestTimes = new int[Config.getInstance().getNumberOfCores()];
    }

    public AStarSchedule(Map<INode, Tuple<Integer, Integer>> map, List<INode> list, int[] times) {
        this.schedulable = list;
        this.scheduled = map;
        this.earliestTimes = times;

    }

    @Override
    public Tuple<Integer, Integer> getNodeSchedule(INode node) {
        return scheduled.get(node);
    }

    public List<INode> getSchedulable() {
        return new ArrayList<>(schedulable);
    }

    public Map<INode, Tuple<Integer, Integer>> getScheduled() {
        return new HashMap<>(scheduled);
    }

    public int[] getEarliestTimes() {
        int[] copy = new int[this.earliestTimes.length];
        System.arraycopy(this.earliestTimes, 0, copy, 0, this.earliestTimes.length);
        return copy;
    }

    public void scheduleNode(INode node, int processor) {
        Map<INode, Integer> parents = node.getParents();
        int latest = 0;
        for (Map.Entry<INode, Integer> parent : parents.entrySet()) {
            Tuple<Integer, Integer> time = getNodeSchedule(parent.getKey());
            if (time.y == processor) {
                latest = Math.max(latest, (time.x + parent.getKey().getProcessingCost()));
            } else {
                latest = Math.max(latest, (time.x + parent.getKey().getProcessingCost() + parent.getValue()));
            }
        }
        // assuming first processor is 1
        latest = Math.max(latest, this.earliestTimes[processor - 1]);
        this.scheduled.put(node, new Tuple<>(latest, processor));
        this.earliestTimes[processor - 1] = latest + node.getProcessingCost();
    }
}
