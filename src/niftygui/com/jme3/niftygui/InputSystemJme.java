package com.jme3.niftygui;

import com.jme3.input.KeyInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;
import de.lessvoid.nifty.input.mouse.MouseInputEvent;
import de.lessvoid.nifty.spi.input.InputSystem;
import java.util.ArrayList;
import java.util.List;

public class InputSystemJme implements InputSystem, RawInputListener {

    private final ArrayList<MouseInputEvent> mouseEvents = new ArrayList<MouseInputEvent>();
    private final ArrayList<MouseInputEvent> mouseEventsCopy = new ArrayList<MouseInputEvent>();

    private final ArrayList<KeyboardInputEvent> keyEvents     = new ArrayList<KeyboardInputEvent>();
    private final ArrayList<KeyboardInputEvent> keyEventsCopy = new ArrayList<KeyboardInputEvent>();

    private boolean pressed = false;
    private int x, y;

    private boolean shiftDown = false;
    private boolean ctrlDown  = false;

    public InputSystemJme(){
    }

    public List<MouseInputEvent> getMouseEvents() {
        synchronized (mouseEvents){
            mouseEventsCopy.clear();
            mouseEventsCopy.addAll(mouseEvents);
            mouseEvents.clear();
            return mouseEventsCopy;
        }
    }

    public List<KeyboardInputEvent> getKeyboardEvents() {
        synchronized (keyEvents){
            keyEventsCopy.clear();
            keyEventsCopy.addAll(keyEvents);
            keyEvents.clear();
            return keyEventsCopy;
        }
    }

    public void onJoyAxisEvent(JoyAxisEvent evt) {
    }

    public void onJoyButtonEvent(JoyButtonEvent evt) {
    }

    public void onMouseMotionEvent(MouseMotionEvent evt) {
        synchronized (mouseEvents){
            x = evt.getX();
            y = evt.getY();
            mouseEvents.add(new MouseInputEvent(x, y, pressed));
        }
    }

    public void onMouseButtonEvent(MouseButtonEvent evt) {
        if (evt.getButtonIndex() == 0){
            synchronized (mouseEvents){
                pressed = evt.isPressed();
                mouseEvents.add(new MouseInputEvent(x, y, pressed));
            }
        }
    }

    public void onKeyEvent(KeyInputEvent evt) {
        int code = evt.getKeyCode();
       
        synchronized (keyEvents){
            if (code == KeyInput.KEY_LSHIFT || code == KeyInput.KEY_RSHIFT) {
                shiftDown = evt.isPressed();
            } else if (code == KeyInput.KEY_LCONTROL || code == KeyInput.KEY_RCONTROL) {
                ctrlDown = evt.isPressed();
            }
            KeyboardInputEvent keyEvt = new KeyboardInputEvent(code,
                                                               evt.getKeyChar(),
                                                               evt.isPressed(),
                                                               shiftDown,
                                                               ctrlDown);


            keyEvents.add(keyEvt);
        }
    }
}
