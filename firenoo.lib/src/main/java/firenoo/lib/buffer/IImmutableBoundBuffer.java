package firenoo.lib.buffer;

public interface IImmutableBoundBuffer<T extends Number> {
    /**
     * Queries the value stored in this buffer.
     */
    T value();

    /**
     * The minimum value. Any time the stored value is modified such that
     * the stored value falls below the minimum value, the stored value is
     * set to the minimum instead.
     */
    T min();

    /**
     * The maximum value. Any time the stored value is modified such that
     * the stored value lies above the maximum value, the stored value is
     * set to the maximum instead.
     */
    T max();

    /**
     * Checks whether there is space in the buffer.
     * @return true if {@code value() == max()}, false otherwise.
     */
    default boolean atMax() {
        return value() == max();
    }

    /**
     * Checks whether the buffer is empty.
     * @return true if {@code value() == min()}, false otherwise.
     */
    default boolean atMin() {
        return value() == min();
    }
}