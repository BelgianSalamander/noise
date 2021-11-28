package me.salamander.ourea.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class SortedList<T> implements Iterable<T> {
    private final Comparator<T> comparator;

    private final List<T> backingList = new ArrayList<>();

    public SortedList(Comparator<T> comparator){
        this.comparator = comparator;
    }

    public void add(T element){
        backingList.add(getPlacementIndexOf(element), element);
    }

    public int getPlacementIndexOf(T element){
        //If element is already in the list return its first index otherwise return the index it should be placed in

        int low = 0;
        int high = backingList.size() - 1;

        while(low <= high){
            int mid = (low + high) / 2;
            int cmp = comparator.compare(backingList.get(mid), element);
            if(cmp < 0){
                low = mid + 1;
            }else if(cmp > 0){
                high = mid - 1;
            }else {
                return mid;
            }
        }

        return low;
    }

    public int getPlacementIndexOf(CustomComparator<T> comparator){
        //If element is already in the list return its first index otherwise return the index it should be placed in

        int low = 0;
        int high = backingList.size() - 1;

        while(low <= high){
            int mid = (low + high) / 2;
            int cmp = comparator.compare(backingList.get(mid));
            if(cmp < 0){
                low = mid + 1;
            }else if(cmp > 0){
                high = mid - 1;
            }else {
                return mid;
            }
        }

        return low;
    }

    public T get(int index){
        return backingList.get(index);
    }

    public int size() {
        return backingList.size();
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return backingList.iterator();
    }

    @FunctionalInterface
    public interface CustomComparator<T> {
        int compare(T a);
    }
}
