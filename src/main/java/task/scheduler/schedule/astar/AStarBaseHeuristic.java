package task.scheduler.schedule.astar;

import java.util.Comparator;

public class AStarBaseHeuristic implements Comparator<AStarSchedule> {
    @Override
    public int compare(AStarSchedule o1, AStarSchedule o2) {
        if (o1.getCost() < o2.getCost()) {
            return -1;
        } else if (o1.getCost() > o2.getCost()) {
            return 1;
        } else {
            if (o1.getNodesScheduled() < o2.getNodesScheduled()) {
                return 1;
            } else if (o1.getNodesScheduled() > o2.getNodesScheduled()) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
