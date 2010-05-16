package com.jme3.input.event;

public class JoyAxisEvent extends InputEvent {

    private int joyIdx;
    private int axisIdx;
    private int realAxisIdx;
    private float value;

    public JoyAxisEvent(int joyIdx, int axisIdx, int realAxisIdx, float value) {
        this.joyIdx = joyIdx;
        this.axisIdx = axisIdx;
        this.realAxisIdx = realAxisIdx;
        this.value = value;
    }

    public int getAxisIndex() {
        return axisIdx;
    }

    public int getJoyIndex() {
        return joyIdx;
    }

    public int getRealAxisIndex() {
        return realAxisIdx;
    }

    public float getValue() {
        return value;
    }



}
