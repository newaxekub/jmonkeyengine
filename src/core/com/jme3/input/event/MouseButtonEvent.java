package com.jme3.input.event;

public class MouseButtonEvent extends InputEvent {

    int btnIndex;
    boolean pressed;

    public MouseButtonEvent(int btnIndex, boolean pressed) {
        this.btnIndex = btnIndex;
        this.pressed = pressed;
    }

    public int getButtonIndex() {
        return btnIndex;
    }

    public boolean isPressed() {
        return pressed;
    }

    public boolean isReleased() {
        return !pressed;
    }
    
    public String toString(){
        String str = "MouseButton(BTN="+btnIndex;
        if (pressed){
            return str + ", PRESSED)";
        }else{
            return str + ", RELEASED)";
        }
    }

}
