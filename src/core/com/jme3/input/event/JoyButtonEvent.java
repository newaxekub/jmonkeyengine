package com.jme3.input.event;

public class JoyButtonEvent extends InputEvent {

    private int joyIdx;
    private int btnIdx;
    private boolean pressed;

    public JoyButtonEvent(int joyIdx, int btnIdx, boolean pressed) {
        this.joyIdx = joyIdx;
        this.btnIdx = btnIdx;
        this.pressed = pressed;
    }

    public int getButtonIndex() {
        return btnIdx;
    }

    public int getJoyIndex() {
        return joyIdx;
    }

    public boolean isPressed() {
        return pressed;
    }



}
