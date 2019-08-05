package task.scheduler;

/**
 * Tuple is a generic class used to store two immutable objects.
 *
 * @param <X> 2 tuple first object.
 * @param <Y> 2 tuple second object.
 */
public class Tuple<X, Y> {
    public final X x;
    public final Y y;

    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }
}