/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
