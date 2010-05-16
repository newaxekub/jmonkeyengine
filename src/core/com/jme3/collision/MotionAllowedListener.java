package com.jme3.collision;

import com.jme3.math.Vector3f;

public interface MotionAllowedListener {

    /**
     * Check if motion allowed. Modify position and velocity vectors
     * appropriately if not allowed..
     * 
     * @param position
     * @param velocity
     */
    public void checkMotionAllowed(Vector3f position, Vector3f velocity);

}
