package com.jme3.input;

import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;

/**
 * An interface used for recieving raw input from devices.
 */
public interface RawInputListener {
    public void onJoyAxisEvent(JoyAxisEvent evt);
    public void onJoyButtonEvent(JoyButtonEvent evt);
    public void onMouseMotionEvent(MouseMotionEvent evt);
    public void onMouseButtonEvent(MouseButtonEvent evt);
    public void onKeyEvent(KeyInputEvent evt);
}
