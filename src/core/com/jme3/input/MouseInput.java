package com.jme3.input;

/**
 * A specific API for interfacing with the mouse.
 */
public interface MouseInput extends Input {

    /**
     * @param visible Whether the mouse cursor should be visible or not.
     */
    public void setCursorVisible(boolean visible);

    /**
     * @return The number of buttons the mouse has. Typically 3 for most mice.
     */
    public int getButtonCount();
}
