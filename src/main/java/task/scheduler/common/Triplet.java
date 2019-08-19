package task.scheduler.common;

/**
 * Generic triplet class for storing three related, immutable values of any types
 * @param <X>
 * @param <Y>
 * @param <Z>
 */
public class Triplet<X, Y, Z> {
    public final X x;
    public final Y y;
    public final Z z;

    public Triplet(X x, Y y, Z z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
