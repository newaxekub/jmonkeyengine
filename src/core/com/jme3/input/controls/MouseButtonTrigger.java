package com.jme3.input.controls;

/**
 * Mouse-button trigger.
 */
public class MouseButtonTrigger implements Trigger {

    private final int mouseButton;

    public MouseButtonTrigger(int mouseButton) {
        if  (mouseButton < 0)
            throw new IllegalArgumentException("Mouse Button cannot be negative");

        this.mouseButton = mouseButton;
    }

    public int getMouseButton() {
        return mouseButton;
    }

    public String getName() {
        return "Mouse Button " + mouseButton;
    }

    public static int mouseButtonHash(int mouseButton){
        return 256 | (mouseButton & 0xff);
    }

    @Override
    public int hashCode(){
        return mouseButtonHash(mouseButton);
    }

}
