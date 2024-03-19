package com.example.playem.generics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConcurrentListBackHashMap<K,O> implements Iterable<O> {
    private final ConcurrentHashMap<K, Integer> internalHash;
    private final List<O> internalList;
    private final List<K> internalKeyList;

    public ConcurrentListBackHashMap(){
        internalHash = new ConcurrentHashMap<>();
        internalList = new CopyOnWriteArrayList<>();
        internalKeyList = new CopyOnWriteArrayList<>();
    }
    public boolean ContainsKey(@NonNull K key){
        return internalHash.containsKey(key);
    }

    @Nullable
    public O Get(@NonNull K key){
        if(internalHash.containsKey(key)) {
            int idx = internalHash.get(key);
            if(idx<internalList.size()){
                return internalList.get(idx);
            }
        }
        return null;
    }
    @Nullable
    public synchronized O Remove(@NonNull K key){
        if(internalHash.containsKey(key)){
            int idx = internalHash.get(key);
            int len = internalList.size();
            if(idx<len){
                O retval = internalList.get(idx);
                internalList.set(idx,internalList.get(len-1));
                internalKeyList.set(idx,internalKeyList.get(len-1));
                internalList.remove(len-1);
                internalHash.remove(key);
                return retval;
            }
        }
        return null;
    }
    @NonNull
    public synchronized O AddorUpdate(@NonNull K key, @NonNull O val){
        if(internalHash.containsKey(key)){
            int idx = internalHash.get(key);
            O oldval = internalList.get(idx);
            internalList.set(idx,val);
            return oldval;
        }
        internalList.add(val);
        internalKeyList.add(key);
        internalHash.put(key,internalList.size()-1);
        return val;
    }

    public int size(){
        return internalList.size();
    }
    public List<O> GetValues(){
        return internalList;
    }
    public List<K> GetKeys(){
        return internalKeyList;
    }

    @NonNull
    @Override
    public Iterator<O> iterator() {
        return new LinkBackHashMapIterator(this.internalList);
    }
    public class LinkBackHashMapIterator implements Iterator<O>{
        public LinkBackHashMapIterator(List<O>list){
            this.len = len;
            backinglist = list;
        }
        private final List<O> backinglist;
        private int counter=-1;
        private int len = 0;
        private boolean canRemove = false;
        @Override
        public boolean hasNext() {
            return (counter+1)<len;
        }
        @Override
        public O next() {
            if(hasNext()) {
                counter++;
                canRemove = true;
                return backinglist.get(counter);
            }
            canRemove = false;
            return null;
        }

    }
}
