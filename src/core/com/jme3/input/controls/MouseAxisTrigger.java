package com.jme3.input.controls;

import com.jme3.input.MouseInput;

public class MouseAxisTrigger implements Trigger {

    private int mouseAxis;
    private boolean negative;

    public MouseAxisTrigger(int mouseAxis, boolean negative){
        if (mouseAxis < 0 || mouseAxis > 2)
            throw new IllegalArgumentException("Mouse Axis must be between 0 and 2");

        this.mouseAxis = mouseAxis;
        this.negative = negative;
    }

    public int getMouseAxis(){
        return mouseAxis;
    }

    public boolean isNegative() {
        return negative;
    }

    public String getName() {
        String sign = negative ? "Negative" : "Positive";
        switch (mouseAxis){
            case MouseInput.AXIS_X: return "Mouse X Axis " + sign;
            case MouseInput.AXIS_Y: return "Mouse Y Axis " + sign;
            case MouseInput.AXIS_WHEEL: return "Mouse Wheel " + sign;
            default: throw new AssertionError();
        }
    }

    public static final int mouseAxisHash(int mouseAxis, boolean negative){
        return (negative ? 768 : 512) | (mouseAxis & 0xff);
    }

    @Override
    public int hashCode(){
        return mouseAxisHash(mouseAxis, negative);
    }
}
