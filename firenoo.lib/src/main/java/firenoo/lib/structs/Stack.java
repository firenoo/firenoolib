package firenoo.lib.structs;

import java.util.Arrays;

@SuppressWarnings("unchecked")
public class Stack<E> {
    
    private Object[] array;

    private int ptr = 0;

    public Stack(int size) {
        this.array = new Object[size + 1];
    }

    public void push(E object) {
        if(ptr + 1 < array.length) {
            array[ptr++] = object;
        } else {
            throw new IllegalStateException("Stack is full.");
        }
    }

    public E pop() {
        if(ptr < 1) {
            throw new IllegalStateException("Stack is empty.");
        }
        return (E) array[--ptr];
    }

    public E peek() {
        return (E) array[ptr];
    }

    public String toString() {
        return Arrays.toString(array) + String.format(" | PTR %d", ptr);
    }

    public boolean isEmpty() {
        return ptr == 0;
    }

    public void clear() {
        ptr = 0;
    }

}