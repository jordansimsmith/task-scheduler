package task.scheduler;

public interface ISchedule {
    public Tuple<Integer, Integer> getNodeSchedule(INode);
}