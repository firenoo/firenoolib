package firenoo.lib.buffer;

public class ImmutableDoubleBuffer extends DoubleBoundBuffer {
    
    public ImmutableDoubleBuffer() {
        super(0, 0);
    }

    @Override
    public <U extends Number> IBoundedBuffer<Double> add(U other) {
        return this;
    }

    @Override
    public <U extends Number> IBoundedBuffer<Double> add(IImmutableBoundBuffer<U> other) {
        return this;
    }

    @Override
    public <U extends Number> Double extraFromAdd(U other) {
        return other.doubleValue();
    }

    @Override
    public <U extends Number> Number transfer(IBoundedBuffer<U> other, U maxAmount) {
        return 0;
    }

    @Override
    public <U extends Number> IBoundedBuffer<Double> mul(U other) {
        return this;
    }

    @Override
    public <U extends Number> IBoundedBuffer<Double> mul(IImmutableBoundBuffer<U> other) {
        return this;
    }

    @Override
    public <U extends Number> IBoundedBuffer<Double> set(U other) {
        return this;
    }
    
    @Override
    public <U extends Number> IBoundedBuffer<Double> set(IImmutableBoundBuffer<U> other) {
        return this;
    }

    

}