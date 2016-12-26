package io.tchepannou.kiosk.pipeline.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ThreadMonitor {
    private final Set<Runnable> threads = Collections.synchronizedSet(new HashSet<>());

    public void started(Runnable thread){
        threads.add(thread);
    }

    public void finished(Runnable thread){
        threads.remove(thread);
    }

    public boolean isAlive (){
        return !threads.isEmpty();
    }

    public void waitAllThreads(final long delayMillis, long maxWaitMillis){
        final long start = System.currentTimeMillis();
        while ((System.currentTimeMillis() - start < maxWaitMillis) && isAlive()){
            try{
                Thread.sleep(delayMillis);
            } catch (InterruptedException e){
                break;
            }
        }
    }
}
