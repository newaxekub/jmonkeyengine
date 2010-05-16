package com.jme3.input;

/**
 * Abstract interface for an input device.
 * 
 * @see MouseInput
 * @see KeyInput
 * @see JoyInput
 */
public interface Input {

    /**
     * Initializes the native side to listen into events from the device.
     */
    public void initialize();

    /**
     * Queries the device for input. All events should be sent to the
     * RawInputListener set with setInputListener.
     *
     * @see #setInputListener(com.jme3.input.RawInputListener)
     */
    public void update();

    /**
     * Ceases listening to events from the device.
     */
    public void destroy();

    /**
     * @return True if the device has been initialized and not destroyed.
     * @see #initialize()
     * @see #destroy() 
     */
    public boolean isInitialized();

    /**
     * Sets the input listener to recieve events from this device. The
     * appropriate events should be dispatched through the callbacks
     * in RawInputListener.
     * @param listener
     */
    public void setInputListener(RawInputListener listener);
}
