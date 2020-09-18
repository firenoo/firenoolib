package firenoo.lib.structs;

@SuppressWarnings("unchecked")
public class MinPriorityQueue<T> {

    //This array starts storing objects at index 1
    private QueueObject<T>[] entries;
    //Points to the farthest empty entry.
    private int currentEntry;

    public MinPriorityQueue(int size) {
        if(size < 1) {
            throw new IllegalArgumentException("Size must be 1 or greater.");
        }
        //Compiler: IT'S THE END OF THE WORLD!!!!!!!!!!!!!!
        this.entries = new QueueObject[size + 1];
        this.currentEntry = 1;
    }

    public void enqueue(T obj, int priority) {
        QueueObject<T> q_obj = new QueueObject<>(obj, priority);
        if(this.currentEntry >= entries.length) {
            resize((int)(this.entries.length * 2 + 1));
        }
        entries[currentEntry] = q_obj;
        percolateUp(currentEntry++);
    }

    public T peek() {
        return !isEmpty() ? this.entries[1].obj : null;
    }

    public int peekPriority() {
        if(!isEmpty()) {
            return this.entries[1].priority;
        }
        throw new IllegalStateException("P. Queue is empty.");
    }

    public T dequeue() {
        QueueObject<T> q_obj = entries[1];
        if(q_obj != null && currentEntry > 1) {
            entries[1] = entries[--currentEntry];
            percolateDown(1);
            entries[currentEntry] = null;
            return q_obj.obj;
        } else {
            return null;
        }
    }

    public void updatePriority(T object, int newPriority) {
        for(int i = 1; i < currentEntry; i++) {
            if(entries[i].obj.equals(object)) {
                int oldPriority = entries[i].priority;
                entries[i].priority = newPriority;
                if(oldPriority > newPriority) {
                    percolateDown(i);
                } else {
                    percolateUp(i);
                }
                //All instances are updated
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
        QueueObject<T> cmp = this.entries[holePos];
        while(parent > 0) {
            if(entries[parent].priority > cmp.priority) {
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
        QueueObject<T> cmp = this.entries[holePos];
        while(child < currentEntry) {
            //left child has higher priority than right child
            if(entries[child].priority > entries[child + 1].priority) {
                child++; //child on the right will move up
            }
            if(cmp.priority <= entries[child].priority) {
                break;
            }
            entries[holePos] = entries[child];
            holePos = child;
            child = holePos << 1;
        }
        entries[holePos] = cmp;
    }

    private void resize(int newSize) {
        QueueObject<T>[] resized = new QueueObject[newSize];
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
        
        public String toString() {
            return String.format("%s - %d", obj.toString(), priority);
        }
    }

}