package firenoo.lib.structs;

import java.util.Objects;

public class Vec2i {

    public int x, y;

    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vec2i deepCopy() {
        return new Vec2i(x, y);
    }

    @Override
    public boolean equals(Object other) {
        return other.getClass().equals(this.getClass()) && this.x == ((Vec2i)other).x && this.y == ((Vec2i)other).y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public String toString() {
        return super.toString() + String.format(" (%d, %d)", x, y);
    }
}