package com.jme3.input.controls;

/**
 * A trigger represents a physical input, such as a keyboard key, a mouse
 * button, or joystick axis.
 */
public interface Trigger {

    /**
     * @return A user friendly name for the trigger.
     */
    public String getName();

    /**
     * @return Hash-code for the trigger, can map into the entire
     * 32 bit space, and there must be no two different triggers with same
     * hash-code.
     */
    @Override
    public int hashCode();
}
