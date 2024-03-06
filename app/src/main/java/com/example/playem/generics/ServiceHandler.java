package com.example.playem.generics;

import java.util.ArrayList;
import java.util.List;

public class ServiceHandler<K,O> {
    public ServiceHandler(int size){
        KeyArray = new ArrayList<>();
        ObjectArray = new ArrayList<>();
        maxLength = Math.min(size, 16);
    }
    private int maxLength;
    private int length =0;
    private ArrayList<K> KeyArray;
    private ArrayList<O> ObjectArray;

    public List<O> GetObjects(){return ObjectArray;}
    public List<K> GetKeys(){return KeyArray;}
    public int size(){
        return length;
    }
    public void PutPair(K k,O o){
        if(length==maxLength){
            return;
        }
        int idx = KeyArray.indexOf(k);
        if(idx ==-1){
            KeyArray.add(k);
            ObjectArray.add(o);
            length+=1;
            return;
        }
        KeyArray.set(idx,k);
        ObjectArray.set(idx,o);
    }
    public O Get(int idx){
        return ObjectArray.get(idx);
    }
    public K GetK(int idx){
        return KeyArray.get(idx);
    }

    public void Remove(K k){
        int idx = KeyArray.indexOf(k);
        if(idx==-1){
            return;
        }
        int last = KeyArray.size()-1;
        KeyArray.set(idx,KeyArray.get(last));
        ObjectArray.set(idx,ObjectArray.get(last));
        KeyArray.remove(last);
        ObjectArray.remove(last);
        length-=1;
    }

}
