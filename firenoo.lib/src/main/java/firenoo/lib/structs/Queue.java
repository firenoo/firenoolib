package firenoo.lib.structs;

@SuppressWarnings("unchecked")
public class Queue<E> {
    
    private Object[] array;
    //pointer to queue objects
    private int ptr = 0;
    //pointer to remove objects
    private int out_ptr = 0;

    public Queue(int capacity) {
        if(capacity < 1) {
            throw new IllegalArgumentException("Size must be 1 or greater");            
        }
        this.array = new Object[capacity + 1];
    }

    public void enqueue(E object) {
        if(getNext() == out_ptr) {
            throw new IllegalStateException("Queue is full.");
        } else {
            array[ptr] = object;
            ptr = getNext();
        }
    }

    public E dequeue() {
        E result = (E) array[out_ptr];
        if(out_ptr == ptr) {
            return null;
        }
        out_ptr = (out_ptr + 1) % array.length;
        return result;
    }

    public E peek() {
        return (E) array[out_ptr];
    }

    private int getNext() {
        return (ptr + 1) % array.length;
    }

    public boolean isEmpty() {
        return out_ptr == ptr;
    }
    
    public void clear() {
         out_ptr = ptr;
    }

    public int capacity() {
        return array.length - 1;
    }

    public int elementCt() {
        return ptr > out_ptr ? ptr - out_ptr : array.length - (out_ptr - ptr);
    }

}