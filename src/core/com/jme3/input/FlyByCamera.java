package com.jme3.input;

import com.jme3.collision.MotionAllowedListener;
import com.jme3.input.binding.BindingListener;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 * A first person view camera controller.
 * After creation, you must register the camera controller with the
 * dispatcher using #registerWithDispatcher().
 *
 * Controls:
 *  - Move the mouse to rotate the camera
 *  - Mouse wheel for zooming in or out
 *  - WASD keys for moving forward/backward and strafing
 *  - QZ keys raise or lower the camera
 */
public class FlyByCamera implements BindingListener {

    private Camera cam;
    private Vector3f initialUpVec;
    private float rotationSpeed = 1f;
    private float moveSpeed = 3f;
    private MotionAllowedListener motionAllowed = null;
    private boolean enabled = true;
    private boolean dragToRotate = false;
    private boolean canRotate = false;
    private InputManager inputManager;

    /**
     * Creates a new FlyByCamera to control the given Camera object.
     * @param cam
     */
    public FlyByCamera(Camera cam){
        this.cam = cam;
        initialUpVec = cam.getUp().clone();
    }

    public void setMotionAllowedListener(MotionAllowedListener listener){
        this.motionAllowed = listener;
    }

    /**
     * Sets the move speed. The speed is given in world units per second.
     * @param moveSpeed
     */
    public void setMoveSpeed(float moveSpeed){
        this.moveSpeed = moveSpeed;
    }

    /**
     * Sets the rotation speed.
     * @param rotationSpeed
     */
    public void setRotationSpeed(float rotationSpeed){
        this.rotationSpeed = rotationSpeed;
    }

    /**
     * @param enable If false, the camera will ignore input.
     */
    public void setEnabled(boolean enable){
        enabled = enable;
    }

    /**
     * @return If enabled
     * @see FlyByCamera#setEnabled(boolean)
     */
    public boolean isEnabled(){
        return enabled;
    }

    /**
     * @return If drag to rotate feature is enabled.
     *
     * @see FlyByCamera#setDragToRotate(boolean) 
     */
    public boolean isDragToRotate() {
        return dragToRotate;
    }

    /**
     * @param dragToRotate When true, the user must hold the mouse button
     * and drag over the screen to rotate the camera, and the cursor is
     * visible until dragged. Otherwise, the cursor is invisible at all times
     * and holding the mouse button is not needed to rotate the camera.
     * This feature is disabled by default.
     */
    public void setDragToRotate(boolean dragToRotate) {
        this.dragToRotate = dragToRotate;
    }

    /**
     * Registers the FlyByCamera to recieve input events from the provided
     * Dispatcher.
     * @param dispacher
     */
    public void registerWithInput(InputManager inputManager){
        this.inputManager = inputManager;
        
        inputManager.registerJoystickAxisBinding("FLYCAM_Left",  2, JoyInput.AXIS_X, true);
        inputManager.registerJoystickAxisBinding("FLYCAM_Right", 2, JoyInput.AXIS_X, false);
        inputManager.registerJoystickAxisBinding("FLYCAM_Up",    2, JoyInput.AXIS_Y, true);
        inputManager.registerJoystickAxisBinding("FLYCAM_Down",  2, JoyInput.AXIS_Y, false);

        inputManager.registerJoystickAxisBinding("FLYCAM_StrafeLeft",  2, JoyInput.POV_X, true);
        inputManager.registerJoystickAxisBinding("FLYCAM_StrafeRight", 2, JoyInput.POV_X, false);
        inputManager.registerJoystickAxisBinding("FLYCAM_Forward",     2, JoyInput.POV_Y, true);
        inputManager.registerJoystickAxisBinding("FLYCAM_Backward",    2, JoyInput.POV_Y, false);
        
        inputManager.registerMouseAxisBinding("FLYCAM_Left", 0, true);
        inputManager.registerMouseAxisBinding("FLYCAM_Right", 0, false);
        inputManager.registerMouseAxisBinding("FLYCAM_Up", 1, false);
        inputManager.registerMouseAxisBinding("FLYCAM_Down", 1, true);

        inputManager.registerMouseAxisBinding("FLYCAM_ZoomIn", 2, false);
        inputManager.registerMouseAxisBinding("FLYCAM_ZoomOut", 2, true);
        
        inputManager.registerMouseButtonBinding("FLYCAM_RotateDrag", 0);

        inputManager.registerKeyBinding("FLYCAM_Left", KeyInput.KEY_LEFT);
        inputManager.registerKeyBinding("FLYCAM_Right", KeyInput.KEY_RIGHT);
        inputManager.registerKeyBinding("FLYCAM_Up", KeyInput.KEY_UP);
        inputManager.registerKeyBinding("FLYCAM_Down", KeyInput.KEY_DOWN);

        inputManager.registerKeyBinding("FLYCAM_StrafeLeft", KeyInput.KEY_A);
        inputManager.registerKeyBinding("FLYCAM_StrafeRight", KeyInput.KEY_D);
        inputManager.registerKeyBinding("FLYCAM_Forward", KeyInput.KEY_W);
        inputManager.registerKeyBinding("FLYCAM_Backward", KeyInput.KEY_S);

        inputManager.registerKeyBinding("FLYCAM_Rise", KeyInput.KEY_Q);
        inputManager.registerKeyBinding("FLYCAM_Lower", KeyInput.KEY_Z);

        inputManager.addBindingListener(this);
    }

    private void rotateCamera(float value, Vector3f axis){
        if (dragToRotate){
            if (canRotate){
//                value = -value;
            }else{
                return;
            }
        }

        Matrix3f mat = new Matrix3f();
        mat.fromAngleNormalAxis(rotationSpeed * value, axis);

        Vector3f up = cam.getUp();
        Vector3f left = cam.getLeft();
        Vector3f dir = cam.getDirection();

        mat.mult(up, up);
        mat.mult(left, left);
        mat.mult(dir, dir);

        Quaternion q = new Quaternion();
        q.fromAxes(left, up, dir);
        q.normalize();

        cam.setAxes(q);
    }

    private void zoomCamera(float value){
        // derive fovY value
        float h = cam.getFrustumTop();
        float w = cam.getFrustumRight();
        float aspect = w / h;

        float near = cam.getFrustumNear();

        float fovY = FastMath.atan(h / near)
                  / (FastMath.DEG_TO_RAD * .5f);
        fovY += value * 0.1f;

        h = FastMath.tan( fovY * FastMath.DEG_TO_RAD * .5f) * near;
        w = h * aspect;

        cam.setFrustumTop(h);
        cam.setFrustumBottom(-h);
        cam.setFrustumLeft(-w);
        cam.setFrustumRight(w);
    }

    private void riseCamera(float value){
        Vector3f vel = new Vector3f(0, value * moveSpeed, 0);
        Vector3f pos = cam.getLocation().clone();

        if (motionAllowed != null)
            motionAllowed.checkMotionAllowed(pos, vel);
        else
            pos.addLocal(vel);

        cam.setLocation(pos);
    }

    private void moveCamera(float value, boolean sideways){
        Vector3f vel = new Vector3f();
        Vector3f pos = cam.getLocation().clone();

        if (sideways){
            cam.getLeft(vel);
        }else{
            cam.getDirection(vel);
        }
        vel.multLocal(value * moveSpeed);

        if (motionAllowed != null)
            motionAllowed.checkMotionAllowed(pos, vel);
        else
            pos.addLocal(vel);

        cam.setLocation(pos);
    }

    public void onBinding(String binding, float value) {
        if (!enabled)
            return;

        if (binding.equals("FLYCAM_Left")){
            rotateCamera(value, initialUpVec);
        }else if (binding.equals("FLYCAM_Right")){
            rotateCamera(-value, initialUpVec);
        }else if (binding.equals("FLYCAM_Up")){
            rotateCamera(-value, cam.getLeft());
        }else if (binding.equals("FLYCAM_Down")){
            rotateCamera(value, cam.getLeft());
        }else if (binding.equals("FLYCAM_RotateDrag") && dragToRotate){
            canRotate = true;
        }else if (binding.equals("FLYCAM_Forward")){
            moveCamera(value, false);
        }else if (binding.equals("FLYCAM_Backward")){
            moveCamera(-value, false);
        }else if (binding.equals("FLYCAM_StrafeLeft")){
            moveCamera(value, true);
        }else if (binding.equals("FLYCAM_StrafeRight")){
            moveCamera(-value, true);
        }else if (binding.equals("FLYCAM_Rise")){
            riseCamera(value);
        }else if (binding.equals("FLYCAM_Lower")){
            riseCamera(-value);
        }else if (binding.equals("FLYCAM_ZoomIn")){
            zoomCamera(value);
        }else if (binding.equals("FLYCAM_ZoomOut")){
            zoomCamera(-value);
        }
    }

    public void onPreUpdate(float tpf) {
        canRotate = false;
        
    }

    public void onPostUpdate(float tpf) {
        if (enabled){
            if (dragToRotate){
                inputManager.setCursorVisible(!canRotate);
            }else{
                inputManager.setCursorVisible(false);
            }
        }
    }

    
}
