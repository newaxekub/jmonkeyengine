package com.jme3.light;

import com.jme3.bounding.BoundingVolume;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.io.IOException;

/**
 * Represents a point light.
 * A point light emits light from a given position into all directions in space.
 * E.g a lamp or a bright effect.
 */
public class PointLight extends Light {

    protected Vector3f position = new Vector3f();
    protected float radius = 0;

    @Override
    public void computeLastDistance(Spatial owner) {
        if (owner.getWorldBound() != null){
            BoundingVolume bv = owner.getWorldBound();
            lastDistance = bv.distanceSquaredTo(position);
        }else{
            lastDistance = owner.getWorldTranslation().distanceSquared(position);
        }
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position){
        this.position.set(position);
    }

    public float getRadius(){
        return radius;
    }

    public void setRadius(float radius){
        this.radius = radius;
    }

    @Override
    public Light.Type getType() {
        return Light.Type.Point;
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(position, "position", null);
        oc.write(radius, "radius", 0f);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        position = (Vector3f) ic.readSavable("position", null);
        radius = ic.readFloat("radius", 0f);
    }

}
