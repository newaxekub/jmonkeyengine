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
package com.jme3.gde.scenecomposer;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.RawInputListener;
import com.jme3.input.binding.BindingListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import org.openide.awt.StatusDisplayer;

/**
 *
 * @author normenhansen
 */
public class ComposerCameraController implements BindingListener, RawInputListener {

    private boolean leftMouse, rightMouse, middleMouse;
    private float deltaX, deltaY, deltaZ, deltaWheel;
    private int mouseX = 0;
    private int mouseY = 0;
    private Quaternion rot = new Quaternion();
    private Vector3f vector = new Vector3f();
    private Vector3f focus = new Vector3f();
    private Camera cam;
    private Node rootNode;

    public ComposerCameraController(Camera cam, Node rootNode) {
        this.cam = cam;
        this.rootNode = rootNode;
    }

    public Geometry checkClick() {
        if (leftMouse) {
            StatusDisplayer.getDefault().setStatusText("Do click check");
            CollisionResults results = new CollisionResults();
            Ray ray = new Ray();
            ray.setOrigin(cam.getWorldCoordinates(new Vector2f(mouseX, mouseY), 0));
//            ray.setOrigin(cam.getLocation());
            ray.setDirection(cam.getDirection());
            rootNode.collideWith(ray, results);
            CollisionResult result = results.getClosestCollision();
            if (result != null) {
                return result.getGeometry();
            }
        }
        return null;
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
        //TODO: wrong, gotta call checkClick() from application after update
//        rootNode.updateGeometricState();
//        Geometry geom = checkClick();
//        if (geom != null) {
//            StatusDisplayer.getDefault().setStatusText("Clicked Geometry: " + geom.toString());
//        }
    }

    public void onJoyAxisEvent(JoyAxisEvent jae) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onJoyButtonEvent(JoyButtonEvent jbe) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onMouseMotionEvent(MouseMotionEvent mme) {
        mouseX = mme.getX();
        mouseY = mme.getY();
    }

    public void onMouseButtonEvent(MouseButtonEvent mbe) {
//        mbe.
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onKeyEvent(KeyInputEvent kie) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    /*
     * methods to move camera
     */
    
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
}
