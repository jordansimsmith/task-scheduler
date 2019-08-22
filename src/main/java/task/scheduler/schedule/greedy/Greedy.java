package task.scheduler.schedule.greedy;

import task.scheduler.common.Config;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;
import task.scheduler.schedule.ISchedule;
import task.scheduler.schedule.IScheduler;
import task.scheduler.schedule.Schedule;
import task.scheduler.schedule.SchedulerState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Greedy implements IScheduler {
    public Greedy(){
    }

    @Override
    public ISchedule execute(IGraph graph){
        Schedule s = new Schedule(graph.getStartNodes(), getParentCountMap(graph));
        SchedulerState.populateTotalNodeWeighting(graph);
        SchedulerState.populateSortedNodes(graph);
        SchedulerState.populateBottomLevelCache(graph);
        while(s.getScheduledNodeCount() < graph.getNodeCount()){
            Schedule next = s;
            int cost = Integer.MAX_VALUE;
            for(INode node:s.getFree()){
                for(int i=1;i<= Config.getInstance().getNumberOfCores();i++){
                    Schedule current = s.expand(node,i);
                    if(current.getTotalCost() < cost){
                        next = current;
                        cost = current.getTotalCost();
                    }
                }
            }
            s = next;
        }
        return s;
    }

    private Map<INode, Integer> getParentCountMap(IGraph graph) {
        Map<INode, Integer> parentCount = new HashMap<>();

        for (INode node : graph.getNodes()) {
            parentCount.put(node, node.getParents().size());
        }
        return parentCount;
    }


    @Override
    public ISchedule getCurrentSchedule() {
        return null;
    }

    @Override
    public int getSchedulesSearched() {
        return 0;
    }
}
