package task.scheduler;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a task to be scheduled, and its dependencies
 */
public class GraphTask
{
    private char me;
    private int cost;
    private Map<Character, Integer> dependencies;

    GraphTask(char me, int cost)
    {
        this.me = me;
        this.cost = cost;
        dependencies = new HashMap<>();
    }

    /**
     * Sets the task to be dependent on the given task, with given cost for transfer between processors
     */
    void setDependency(char task, int cost)
    {
        dependencies.put(task, cost);
    }

    @Override
    public String toString()  {
        StringBuilder string = new StringBuilder(me + " (" + cost + ") [");

        for (Map.Entry entry : dependencies.entrySet())
        {
            string.append(entry.getKey()).append("(").append(entry.getValue()).append("),");
        }
        string.append("]");

        return string.toString();
    }
}
