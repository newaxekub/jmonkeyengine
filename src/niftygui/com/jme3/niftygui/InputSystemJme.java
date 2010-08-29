package com.jme3.niftygui;

import com.jme3.input.KeyInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import de.lessvoid.nifty.NiftyInputConsumer;
import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;
import de.lessvoid.nifty.input.mouse.MouseInputEvent;
import de.lessvoid.nifty.spi.input.InputSystem;
import java.util.ArrayList;

public class InputSystemJme implements InputSystem, RawInputListener {

    private final ArrayList<MouseInputEvent> mouseEvents = new ArrayList<MouseInputEvent>();
    private final ArrayList<KeyboardInputEvent> keyEvents     = new ArrayList<KeyboardInputEvent>();

    private boolean pressed = false;
    private int x, y;
    private int height;

    private boolean shiftDown = false;
    private boolean ctrlDown  = false;

    public InputSystemJme(){
    }

    /**
     * @param height The height of the viewport. Used to convert
     * buttom-left origin to upper-left origin.
     */
    public void setHeight(int height){
        this.height = height;
    }

    public void onJoyAxisEvent(JoyAxisEvent evt) {
    }

    public void onJoyButtonEvent(JoyButtonEvent evt) {
    }

    public void onMouseMotionEvent(MouseMotionEvent evt) {
        synchronized (mouseEvents){
            x = evt.getX();
            y = height - evt.getY();
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

    public void forwardEvents(NiftyInputConsumer nic) {
        synchronized (mouseEvents){
            for (MouseInputEvent evt : mouseEvents){
                nic.processMouseEvent(evt);
            }
            mouseEvents.clear();
        }
        synchronized (keyEvents){
            for (KeyboardInputEvent evt : keyEvents){
                nic.processKeyboardEvent(evt);
            }
            keyEvents.clear();
        }
    }
}
