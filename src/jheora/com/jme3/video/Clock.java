package com.jme3.video;

public interface Clock {

    public static final long MILLIS_TO_NANOS  = 1000000;
    public static final long SECONDS_TO_NANOS = 1000000000;

    public long getTime();
    public double getTimeSeconds();
}
