package com.jme3.audio;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class Listener {

    private Vector3f location;
    private Vector3f velocity;
    private Quaternion rotation;
    private float gain = 1;
    private boolean needRefresh = true;

    public Listener(){
        location = new Vector3f();
        velocity = new Vector3f();
        rotation = new Quaternion();
    }
    
    public Listener(Listener source){
        location = source.location.clone();
        velocity = source.velocity.clone();
        rotation = source.rotation.clone();
        gain = source.gain;
    }

    public float getGain() {
        return gain;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public Vector3f getLocation() {
        return location;
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public Vector3f getLeft(){
        return rotation.getRotationColumn(0);
    }

    public Vector3f getUp(){
        return rotation.getRotationColumn(1);
    }

    public Vector3f getDirection(){
        return rotation.getRotationColumn(2);
    }
    
    public void setLocation(Vector3f location) {
        this.location.set(location);
        needRefresh = true;
    }

    public void setRotation(Quaternion rotation) {
        this.rotation.set(rotation);
        needRefresh = true;
    }

    public void setVelocity(Vector3f velocity) {
        this.velocity.set(velocity);
        needRefresh = true;
    }

    public boolean isRefreshNeeded(){
        return needRefresh;
    }

    public void clearRefreshNeeded(){
        needRefresh = false;
    }

}
