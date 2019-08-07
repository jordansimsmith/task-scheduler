package task.scheduler.schedule.astar;

import task.scheduler.common.Config;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class AStar implements IScheduler {

    PriorityQueue<AStarSchedule> solutions;

    public AStar(Comparator comparator) {
        this.solutions = new PriorityQueue<>(comparator);
    }

    @Override
    public ISchedule execute(IGraph graph) {
        AStarSchedule schedule = new AStarSchedule();
        schedule.setSchedulable(graph.getStartNodes());
        this.solutions.add(schedule);
        while (true) {
            AStarSchedule current = this.solutions.poll();
            if (current.getNodesScheduled() == graph.getNodeCount()) {
                return current;
            }
            List<INode> nodes = current.getSchedulable();
            for (INode node : nodes) {
                if (current.getNodesScheduled() == 0) {
                    AStarSchedule toSchedule = new AStarSchedule(current.getScheduled(), current.getSchedulable(), current.getEarliestTimes(), current.getCost());
                    toSchedule.scheduleNode(node, 1);
                    this.solutions.add(toSchedule);
                } else {
                    for (int i = 1; i <= Config.getInstance().getNumberOfCores(); i++) {
                        AStarSchedule toSchedule = new AStarSchedule(current.getScheduled(), current.getSchedulable(), current.getEarliestTimes(), current.getCost());
                        toSchedule.scheduleNode(node, i);
                        this.solutions.add(toSchedule);
                    }
                }
            }
        }
    }

}
