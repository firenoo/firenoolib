package firenoo.lib.buffer;

public class DoubleBoundBuffer implements IBoundedBuffer<Double> {

    private double min, max;

    private double value;

    public DoubleBoundBuffer(double min, double max) {
        if(min > max) {
            this.min = max;
            this.max = min;
        } else {
            this.min = min;
            this.max = max;
        }
    }

    @Override
    public <U extends Number> IBoundedBuffer<Double> add(U other) {
        this.value = Math.min(
            Math.max(this.value + other.doubleValue(), min()), 
            max()
        );
        return this;
    }

    @Override
    public <U extends Number> IBoundedBuffer<Double> add(IImmutableBoundBuffer<U> other) {
        return add(other.value());
    }

    @Override
    public <U extends Number> Double extraFromAdd(U other) {
        double result = this.value + other.doubleValue();
        if(result < 0) {
            return min() - other.doubleValue();
        }else{
            return result > max() ? result - max() : 0;
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
    public <U extends Number> IBoundedBuffer<Double> mul(U other) {
        this.value = (int) Math.min(
            Math.max(this.value * other.doubleValue(), min()),
            max()
        );
        return this;
    }

    @Override
    public <U extends Number> IBoundedBuffer<Double> mul(IImmutableBoundBuffer<U> other) {
        return mul(other.value());
    }

    @Override
    public <U extends Number> IBoundedBuffer<Double> set(U other) {
        this.value = Math.min(
            Math.max(other.doubleValue(), min()),
            max()
        );
        return this;
    }
    
    @Override
    public <U extends Number> IBoundedBuffer<Double> set(IImmutableBoundBuffer<U> other) {
        return set(other.value());
    }

    @Override
    public Double min() {
        return min;
    }

    @Override
    public Double max() {
        return max;
    }

    @Override
    public Double value() {
        return value;
    }

    @Override
    public IImmutableBoundBuffer<Double> asImmutableBuffer() {
        return new ImmutableDoubleBuf(this.min, this.max, this.value);
    }


    private static class ImmutableDoubleBuf implements IImmutableBoundBuffer<Double> {

        private final double min, max, value;

        private ImmutableDoubleBuf(double min, double max, double value) {
            this.min = min;
            this.max = max;
            this.value = value;
        }

        @Override
        public Double min() {
            return min;
        }

        @Override
        public Double max() {
            return max;
        }

        @Override
        public Double value() {
            return value;
        }
    }
}