package task.scheduler.schedule.astar;

import task.scheduler.graph.IGraph;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;

import java.util.Comparator;
import java.util.PriorityQueue;

public class AStar implements IScheduler {

    PriorityQueue<AStarSchedule> solutions;

    public AStar(Comparator comparator){
        this.solutions = new PriorityQueue();
    }

    @Override
    public ISchedule execute(IGraph graph){

    }

}
