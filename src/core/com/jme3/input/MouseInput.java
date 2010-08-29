package com.jme3.input;

/**
 * A specific API for interfacing with the mouse.
 */
public interface MouseInput extends Input {

    public static final int AXIS_X = 0,
                            AXIS_Y = 1,
                            AXIS_WHEEL = 2;

    public static final int BUTTON_LEFT   = 0,
                            BUTTON_RIGHT  = 1,
                            BUTTON_MIDDLE = 2;

    /**
     * @param visible Whether the mouse cursor should be visible or not.
     */
    public void setCursorVisible(boolean visible);

    /**
     * @return The number of buttons the mouse has. Typically 3 for most mice.
     */
    public int getButtonCount();
}
