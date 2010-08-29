package com.jme3.input.event;

/**
 * An abstract input event.
 */
public abstract class InputEvent {

    /**
     * Time in ticks when the event occured.
     */
    protected long time;

    public long getTime(){
        return time;
    }

    public void setTime(long time){
        this.time = time;
    }
    
}
