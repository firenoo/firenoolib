package firenoo.lib.buffer;

/**
 * Implementation of a bounded buffer that holds an integer value.
 */
public class IntBoundBuffer implements IBoundedBuffer<Integer> {

    private int min, max;

    private int value;

    public IntBoundBuffer(int min, int max) {
        this(min, max, min);        
    }

    public IntBoundBuffer(int min, int max, int initValue) {
        this.min = min;
        this.max = max;
        this.value = initValue;
    }

    @Override
    public <U extends Number> IBoundedBuffer<Integer> add(U other) {
        this.value = (int) Math.min(
            Math.max(this.value + other.doubleValue(), min()),
            max()
        );
        return this;
    }

    @Override
    public <U extends Number> IBoundedBuffer<Integer> add(IImmutableBoundBuffer<U> other) {
        return add(other.value());
    }

    @Override
    public <U extends Number> Integer extraFromAdd(U other) {
        double result = this.value + other.doubleValue();
        if(result < 0) {
            return (int)(min() - other.doubleValue());
        }else{
            return result > max() ? (int)(result - max()) : 0;
        }
    }

    @Override
    public <U extends Number> Number transfer(IBoundedBuffer<U> other, U maxAmount) {
        if(maxAmount.doubleValue() < 0) {
            return Integer.valueOf(0);
        }
        double maxTaken = Math.min(this.max - this.value, Math.min(maxAmount.doubleValue(), other.value().doubleValue()));
        if(maxTaken == 0) {
            return Integer.valueOf(0);
        }
        add(maxTaken);
        other.add(-maxTaken);
        return Double.valueOf(maxTaken);
    }

    @Override
    public <U extends Number> IBoundedBuffer<Integer> mul(U other) {
        this.value = (int) Math.min(
            Math.max(this.value * other.doubleValue(), min()),
            max()
        );
        return this;
    }

    @Override
    public <U extends Number> IBoundedBuffer<Integer> mul(IImmutableBoundBuffer<U> other) {
        return mul(other.value());
    }

    @Override
    public <U extends Number> IBoundedBuffer<Integer> set(U other) {
        this.value = (int) Math.min(
            Math.max(other.doubleValue(), min()),
            max()
        );
        return this;
    }

    @Override
    public <U extends Number> IBoundedBuffer<Integer> set(IImmutableBoundBuffer<U> other) {
        return set(other.value());
    }

    @Override
    public Integer min() {
        return min;
    }

    @Override
    public Integer max() {
        return max;
    }

    @Override
    public Integer value() {
        return value;
    }
}