package com.jme3.input.event;

import com.jme3.input.*;

public class MouseMotionEvent extends InputEvent {

    private int x, y, dx, dy, wheel, deltaWheel;

    public MouseMotionEvent(int x, int y, int dx, int dy, int wheel, int deltaWheel) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.wheel = wheel;
        this.deltaWheel = deltaWheel;
    }

    public int getDeltaWheel() {
        return deltaWheel;
    }

    public int getDX() {
        return dx;
    }

    public int getDY() {
        return dy;
    }

    public int getWheel() {
        return wheel;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String toString(){
        return "MouseMotion(X="+x+", Y="+y+", DX="+dx+", DY="+dy+")";
    }

}
