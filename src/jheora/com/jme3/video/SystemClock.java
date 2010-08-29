package com.jme3.video;

public class SystemClock implements Clock {

    private long startTime = 0;

    public SystemClock(){
    }

    public boolean needReset(){
        return startTime == 0;
    }

    public void reset(){
        startTime = System.nanoTime();
    }

    public long getTime() {
        return System.nanoTime() - startTime;
    }

    public double getTimeSeconds(){
        return (double) getTime() / Clock.SECONDS_TO_NANOS;
    }
    
}
