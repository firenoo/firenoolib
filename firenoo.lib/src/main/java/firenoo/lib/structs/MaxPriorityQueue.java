package firenoo.lib.structs;

@SuppressWarnings("unchecked")
public class MaxPriorityQueue<E> {

    //This array starts storing objects at index 1
    private QueueObject<E>[] entries;
    //Points to the farthest empty entry.
    private int currentEntry;

    public MaxPriorityQueue(int size) {
        if(size < 1) {
            throw new IllegalArgumentException("Size must be 1 or greater.");
        }
        //Compiler: IT'S THE END OF THE WORLD!!!!!!!!!!!!!!
        this.entries = new QueueObject[size + 1];
        this.currentEntry = 1;
    }

    public void enqueue(E obj, int priority) {
        QueueObject<E> q_obj = new QueueObject<>(obj, priority);
        if(this.currentEntry >= entries.length) {
            resize((int)(this.entries.length * 2 + 1));
        }
        entries[currentEntry] = q_obj;
        percolateUp(currentEntry++);
    }

    public E peek() {
        return !isEmpty() ? this.entries[1].obj : null;
    }

    public int peekPriority() {
        if(!isEmpty()) {
            return this.entries[1].priority;
        }
        throw new IllegalStateException("P. Queue is empty.");

    }

    public E dequeue() {
        QueueObject<E> q_obj = entries[1];
        if(q_obj != null && currentEntry > 1) {
            entries[1] = entries[--currentEntry];
            percolateDown(1);
            return q_obj.obj;
        } else {
            return null;
        }
    }

    public void updatePriority(E object, int newPriority) {
        for(int i = 1; i < currentEntry; i++) {
            if(entries[i].obj.equals(object)) {
                int oldPriority = entries[i].priority;
                entries[i].priority = newPriority;
                if(oldPriority > newPriority) {
                    percolateDown(i);
                } else {
                    percolateUp(i);
                }
            }
        }
    }

    public boolean isEmpty() {
        return this.entries[1] == null;
    }

    public void clear() {
        this.currentEntry = 1;
    }

    public int capacity() {
        return entries.length - 1;
    }

    public int elementCt() {
        return currentEntry - 1;
    }

    public boolean contains(T object) {
        for(int i = 1; i < currentEntry; i++) {
            if(entries[i].equals(object)) {
                return true;
            }
        }
        return false;
    }

    private void percolateUp(int holePos) {
        int parent = holePos >>> 1;
        QueueObject<E> cmp = this.entries[holePos];
        while(parent > 0) {
            if(entries[parent].priority < cmp.priority) {
                entries[holePos] = entries[parent];
                holePos = parent;
                parent = holePos >>> 1;
            } else {
                break;
            }
        }
        entries[holePos] = cmp;
    }

    private void percolateDown(int holePos) {
        int child = holePos << 1;
        QueueObject<E> cmp = this.entries[holePos];
        while(child < currentEntry) {
            if(entries[child].priority < entries[child + 1].priority) {
                child++;
            }
            if(cmp.priority >= entries[child].priority) {
                break;
            }
            entries[holePos] = entries[child];
            holePos = child;
            child = holePos << 1;
        }
        entries[holePos] = cmp;
    }

    private void resize(int newSize) {
        QueueObject<E>[] resized = new QueueObject[newSize];
        System.arraycopy(entries, 0, resized, 0, entries.length);
        this.entries = resized;
    }


    private static class QueueObject<T> {

        private int priority;
        private T obj;

        private QueueObject(T obj, int priority) {
            this.priority = priority;
            this.obj = obj;
        }
        
    }

}