/*
 *  Copyright (c) 2009-2010 jMonkeyEngine
 *  All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 * 
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 
 *  * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.core.scene;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.binding.BindingListener;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 *
 * @author normenhansen
 */
public class SceneCameraController implements BindingListener{

    private boolean leftMouse, rightMouse, middleMouse;
    private float deltaX, deltaY, deltaZ, deltaWheel;
    private int mouseX = 0;
    private int mouseY = 0;
    private Quaternion rot = new Quaternion();
    private Vector3f vector = new Vector3f();
    private Vector3f focus = new Vector3f();
    private Camera cam;
    private InputManager inputManager;

    public SceneCameraController(Camera cam, InputManager inputManager) {
        this.cam = cam;
        this.inputManager = inputManager;

        inputManager.registerMouseAxisBinding("MOUSE_X+", 0, false);
        inputManager.registerMouseAxisBinding("MOUSE_X-", 0, true);
        inputManager.registerMouseAxisBinding("MOUSE_Y+", 1, false);
        inputManager.registerMouseAxisBinding("MOUSE_Y-", 1, true);
        inputManager.registerMouseAxisBinding("MOUSE_W+", 2, false);
        inputManager.registerMouseAxisBinding("MOUSE_W-", 2, true);

        inputManager.registerMouseButtonBinding("MOUSE_LEFT", 0);
        inputManager.registerMouseButtonBinding("MOUSE_RIGHT", 1);
        inputManager.registerMouseButtonBinding("MOUSE_MIDDLE", 2);

        inputManager.registerKeyBinding("Up", KeyInput.KEY_UP);
        inputManager.registerKeyBinding("Down", KeyInput.KEY_DOWN);

    }

    public void enable(){
        inputManager.addBindingListener(this);
    }

    public void disable(){
        inputManager.removeBindingListener(this);
    }

    private void rotateCamera(Vector3f axis, float amount) {
        if (axis.equals(cam.getLeft())) {
            float elevation = -FastMath.asin(cam.getDirection().y);
            amount = Math.min(Math.max(elevation + amount,
                    -FastMath.HALF_PI), FastMath.HALF_PI)
                    - elevation;
        }
        rot.fromAngleAxis(amount, axis);
        cam.getLocation().subtract(focus, vector);
        rot.mult(vector, vector);
        focus.add(vector, cam.getLocation());

        Quaternion curRot = cam.getRotation().clone();
        cam.setRotation(rot.mult(curRot));
    }

    private void panCamera(float left, float up) {
        cam.getLeft().mult(left, vector);
        vector.scaleAdd(up, cam.getUp(), vector);
        cam.setLocation(cam.getLocation().add(vector));
        focus.addLocal(vector);
    }

    private void moveCamera(float forward) {
        cam.getDirection().mult(forward, vector);
        cam.setLocation(cam.getLocation().add(vector));
    }

    private void zoomCamera(float amount) {
        float dist = cam.getLocation().distance(focus);
        amount = dist - Math.max(0f, dist - amount);
        Vector3f loc = cam.getLocation().clone();
        loc.scaleAdd(amount, cam.getDirection(), loc);
        cam.setLocation(loc);
    }

    public void onBinding(String binding, float value) {
        if (binding.equals("UPDATE")) {
        } else if (binding.equals("Up")) {
            deltaZ = 10;
        } else if (binding.equals("Down")) {
            deltaZ = -10;
        } else if (binding.equals("MOUSE_LEFT")) {
            leftMouse = value > 0f;
        } else if (binding.equals("MOUSE_RIGHT")) {
            rightMouse = value > 0f;
        } else if (binding.equals("MOUSE_MIDDLE")) {
            middleMouse = value > 0f;
        } else if (binding.equals("MOUSE_X+")) {
            deltaX = value;
        } else if (binding.equals("MOUSE_X-")) {
            deltaX = -value;
        } else if (binding.equals("MOUSE_Y+")) {
            deltaY = value;
        } else if (binding.equals("MOUSE_Y-")) {
            deltaY = -value;
        } else if (binding.equals("MOUSE_W+")) {
            deltaWheel = value;
        } else if (binding.equals("MOUSE_W-")) {
            deltaWheel = -value;
        }
    }

    public void onPreUpdate(float f) {
        if (leftMouse) {
            rotateCamera(Vector3f.UNIT_Y, -deltaX * 5);
            rotateCamera(cam.getLeft(), -deltaY * 5);
        }
        if (deltaWheel != 0) {
            zoomCamera(deltaWheel * 10);
        }
        if (rightMouse) {
            panCamera(deltaX * 10, -deltaY * 10);
        }

        moveCamera(deltaZ);

        leftMouse = false;
        rightMouse = false;
        middleMouse = false;
        deltaX = 0;
        deltaY = 0;
        deltaZ = 0;
        deltaWheel = 0;
    }

    public void onPostUpdate(float f) {
    }

}
