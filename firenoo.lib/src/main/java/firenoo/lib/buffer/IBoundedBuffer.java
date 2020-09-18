package firenoo.lib.buffer;
/**
 * A buffer that contains a value. The values are clamped to minimum
 * and maximum values during mathematical operations.
 * @param <T> Any number type.
 */
public interface IBoundedBuffer<T extends Number> extends IImmutableBoundBuffer<T> {

    /**
     * Adds the parameter to the value contained this buffer, clamping
     * the value as necessary. Negative values work as expected.
     * Note that using float/double values are subject to float arithmetic,
     * but the end result (whether it will be rounded, etc.) depends on the
     * implementation.
     * @return This object reference, which allows for chaining multiple
     *         function calls.
     */
    <U extends Number> IBoundedBuffer<T> add(U other);

    /**
     * Adds the value contained in the parameter to the value contained this 
     * buffer, clamping the value as necessary. Negative values work as 
     * expected. The other buffer is not modified.
     * Note that using float/double values are subject to float arithmetic,
     * but the end result (whether it will be rounded, etc.) depends on the
     * implementation.
     * @return This object reference, which allows for chaining multiple
     *         function calls.
     */
    <U extends Number> IBoundedBuffer<T> add(IImmutableBoundBuffer<U> other);


    /**
     * Adds up to the amount specified in {@code maxAmount} with the value
     * contained in this buffer, and takes the same amount that was added
     * away from the param buffer. Negative values are treated as 0 and the
     * method will not do anything.
     * @return the amount that was actually added.
     */
    <U extends Number> Number transfer(IBoundedBuffer<U> other, U maxAmount);

    /**
     * Simulates an add operation on the value contained in this buffer with
     * the parameter, and returns the amount that is "clipped off" when the
     * value is clamped. Returns 0 if no clamping occurs. A negative value
     * corresponds with clamping occuring at a minimum, and a positive one 
     * corresponds with clamping occuring at a maximum.
     * @param <U>
     * @param other
     * @return The amount that is clipped off after the value is clamped to the
     *         min/max value. That is, if {@code other + value > max}, it returns
     *         {@code other + value - max}. If {@code other + value < min}, this
     *         method returns {@code min - other}. 
     */
    <U extends Number> T extraFromAdd(U other);

    /**
     * Multiplies the parameter to the value contained in this buffer,
     * clamping the value as necessary.
     * Note that using float/double values are subject to float arithmetic,
     * but the end result (whether it will be rounded, etc.) depends on the
     * implementation.
     * @return This object reference, which allows for chaining multiple
     *         function calls.
     */
    <U extends Number> IBoundedBuffer<T> mul(U other);

    /**
     * Multiplies the value contained in the parameter to the value contained 
     * this buffer, clamping the value as necessary. The other buffer is not
     * modified.
     * Note that using float/double values are subject to float arithmetic,
     * but the end result (whether it will be rounded, etc.) depends on the
     * implementation.
     * @return This object reference, which allows for chaining multiple
     *         function calls.
     */
    <U extends Number> IBoundedBuffer<T> mul(IImmutableBoundBuffer<U> other);

    /**
     * Sets the parameter to the value contained in this buffer,
     * clamping the value as necessary. If float/double values are used,
     * the end result is dependent on the implementation.
     * @return This object reference, which allows for chaining multiple
     *         function calls.
     */
    <U extends Number> IBoundedBuffer<T> set(U other);

    /**
     * Sets the value contained in this buffer to the parameter value, 
     * clamping the value as necessary. If float/double values are used,
     * the end result is dependent on the implementation. The other buffer
     * is not modified.
     * @return This object reference, which allows for chaining multiple
     *         function calls.
     */
    <U extends Number> IBoundedBuffer<T> set(IImmutableBoundBuffer<U> other);

    /**
     * Returns an immutable buffer that is the same type as this one.
     * This method should be overriden, regardless of its default status, with
     * a proper implementation of IImmutableBoundBuffer.
     */
    default IImmutableBoundBuffer<T> asImmutableBuffer() {
        return (IImmutableBoundBuffer<T>) this;
    }
}