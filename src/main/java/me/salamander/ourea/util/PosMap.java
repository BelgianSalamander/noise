package me.salamander.ourea.util;

import java.util.Iterator;
import java.util.Objects;

/**
 * This class doesn't work :(
 * @param <T>
 */
public class PosMap<T> {
    private static final float LOAD_FACTOR = 0.75f;
    private static final int INITIAL_CAPACITY = 16;

    private int[] x;
    private int[] y;
    private T[] data;

    private int size = 0;
    private int capacity = 0;
    private int mask;

    public PosMap() {
        allocate(INITIAL_CAPACITY);
    }

    private void allocate(int capacity) {
        this.x = new int[capacity];
        this.y = new int[capacity];
        this.data = (T[]) new Object[capacity];

        this.capacity = capacity;
        this.mask = capacity - 1;
    }

    private void resize(){
        int newCapacity = capacity * 2;

        int oldCapacity = capacity;
        int[] oldX = x;
        int[] oldY = y;
        T[] oldData = data;

        allocate(newCapacity);

        for(int i = 0; i < oldCapacity; i++){
            if(oldData[i] != null){
                put(oldX[i], oldY[i], oldData[i]);
            }
        }
    }

    public T put(int x, int y, T data){
        int bucket = findBucket(x, y, true);

        this.x[bucket] = x;
        this.y[bucket] = y;

        T oldData = this.data[bucket];
        this.data[bucket] = data;

        if(oldData == null){
            size++;
        }

        return oldData;
    }

    public T get(int x, int y){
        int bucket = findBucket(x, y, false);
        if(bucket == -1){
            return null;
        }else{
            return data[bucket];
        }
    }

    public T remove(int x, int y){
        int bucket = findBucket(x, y, false);
        if(bucket == -1){
            return null;
        }else{
            T oldData = data[bucket];
            data[bucket] = null;
            size--;

            //Fix the table
            int lastBucket = bucket;
            while(true){
                int nextBucket = (lastBucket + 1) & mask;
                if(data[nextBucket] == null){
                    break;
                }
                lastBucket = nextBucket;
            }

            //This definitely is not the most efficient way to do this, but it works for now
            if(lastBucket != bucket){
                bucket = (bucket + 1) & mask;
                T data = this.data[bucket];
                //this.data[bucket] = null;
                putWithoutModifyingSize(this.x[bucket], this.y[bucket], data);
            }
            return oldData;
        }
    }

    private void putWithoutModifyingSize(int x, int y, T data) {
        int bucket = findBucket(x, y, true);

        this.x[bucket] = x;
        this.y[bucket] = y;

        T oldData = this.data[bucket];
        this.data[bucket] = data;
    }

    public boolean containsKey(int x, int y){
        return findBucket(x, y, false) != -1;
    }

    public boolean containsValue(T value){
        Objects.requireNonNull(value);
        for(int i = 0; i < capacity; i++){
            if(data[i] == value){
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the appropriate bucket for placing/getting the given key.
     * @param x The x-coordinate of the key.
     * @param y The y-coordinate of the key.
     * @param placing If true and there is currently no bucket for the given key, it will return the first empty bucket. If the capacity is met, it will resize and find a bucket.
     * @return
     */
    protected int findBucket(int x, int y, boolean placing){
        int hash = hash(x, y);
        int bucket = hash & mask;
        while(true){
            if(data[bucket] != null){
                if(x == this.x[bucket] && y == this.y[bucket]){
                    return bucket;
                }
            }else{
                if(!placing){
                    return -1;
                }else{
                    if(size >= capacity * LOAD_FACTOR){
                        resize();
                        return findBucket(x, y, true); // Table was modified, so we need to find the bucket again.
                    }else{
                        return bucket;
                    }
                }
            }

            bucket = (bucket + 1) & mask;
        }
    }

    public Iterable<T> values(){
        int startIndex = 0;
        while(startIndex < capacity && data[startIndex] == null){
            startIndex++;
        }

        int finalStartIndex = startIndex;
        return () -> new Iterator<T>() {
            int index = finalStartIndex;

            @Override
            public boolean hasNext() {
                return index < capacity;
            }

            @Override
            public T next() {
                T value = data[index];
                do{
                    index++;
                }while(index < capacity && data[index] == null);
                return value;
            }
        };
    }

    public void forEachKeys(XZConsumer action){
        for(int i = 0; i < capacity; i++){
            if(data[i] != null){
                action.accept(x[i], y[i]);
            }
        }
    }

    private static int hash(int x, int y){
        x ^= y * 174872384L;
        y ^= x * 2654435761L;

        return x ^ y;
    }

    public int size() {
        return size;
    }
}
