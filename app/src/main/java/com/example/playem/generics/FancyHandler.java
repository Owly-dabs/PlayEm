package com.example.playem.generics;

import android.app.Activity;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.List;

public class FancyHandler<O> implements Iterable<O>{
    private final ConcurrentListBackHashMap<Integer,O> eventBus = new ConcurrentListBackHashMap<>();
    private final ConcurrentListBackHashMap<Integer,Activity> applicationLifeMap = new ConcurrentListBackHashMap<>();
    private final ConcurrentListBackHashMap<Integer,Integer> activitycodes = new ConcurrentListBackHashMap<>();

    public boolean Subscribe(@NonNull Activity activity,@NonNull O callback){
        if(isActivityValid(activity)){
            int a = activity.hashCode();
            applicationLifeMap.AddorUpdate(a,activity);
            eventBus.AddorUpdate(a,callback);
            activitycodes.AddorUpdate(a,a);
            return true;
        }
        return false;
    }
    private boolean isActivityValid(Activity a){
        if(a==null)
            return false;
        return !(a.isFinishing()||a.isDestroyed());
    }
    public boolean UnSubscribe(@NonNull Activity activity){
        if(isActivityValid(activity)) {
            int a = activity.hashCode();
            eventBus.Remove(a);
            activitycodes.Remove(a);
            applicationLifeMap.Remove(a);
            return true;
        }
        return false;
    }

    @NonNull
    @Override
    public Iterator<O> iterator() {
        return new ServiceIterator(this.eventBus,this.applicationLifeMap,activitycodes);
    }

    public class ServiceIterator implements Iterator<O>{
        private final ConcurrentListBackHashMap<Integer,O> evBus;
        private final ConcurrentListBackHashMap<Integer,Activity> appMap;
        private final ConcurrentListBackHashMap<Integer,Integer> keyMap;

        private final List<Integer> keys;

        public ServiceIterator(ConcurrentListBackHashMap<Integer,O> eventBus, ConcurrentListBackHashMap<Integer,Activity> lifemap,ConcurrentListBackHashMap<Integer,Integer> keymap){

            this.pt = -1;
            //Just to be safe
            int evbsize = keymap.size();
            int lmsize = lifemap.size();
            if(evbsize!=lmsize)
                throw new IllegalStateException("Something went wrong with this class");
            this.dlen = lmsize;
            //this.evb = eventBus.iterator();
            //this.lm = lifemap.iterator();
            keys = keymap.GetValues();
            evBus = eventBus;
            appMap = lifemap;
            keyMap = keymap;
        }
        private int pt;
        private int dlen;
        @Override
        public boolean hasNext() {
            return (pt+1<dlen);
        }
        @Override
        public O next() {
            pt++;
            if(!hasNext())
                return null;
            int key = keys.get(pt);
            Activity a = appMap.Get(key);
            if(a==null){
                throw new IllegalStateException("Something went wrong here");
            }
            if(!isActivityValid(a)){
                evBus.Remove(key);
                appMap.Remove(key);
                keyMap.Remove(key);
                keys.set(pt,keys.get(dlen));
                dlen--;
                pt--;
                return next();
            }else{
                return eventBus.Get(key);
            }
        }
        private boolean isActivityValid(Activity a){
            if(a==null)
                return false;
            return !(a.isFinishing()||a.isDestroyed());
        }
    }
}
