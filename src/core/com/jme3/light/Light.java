package com.jme3.light;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;
import java.io.IOException;

/**
 * Abstract class for representing a light source.
 */
public abstract class Light implements Savable, Cloneable {

    public static enum Type {

        Directional(0),
        Point(1),
        Spot(2);

        private int typeId;

        Type(int type){
            this.typeId = type;
        }

        public int getId(){
            return typeId;
        }
    }

    protected ColorRGBA color = new ColorRGBA(1f,1f,1f,1f);
    
    /**
     * Used in LightList for caching the distance 
     * to the owner spatial. Should be reset after the sorting.
     */
    protected transient float lastDistance = -1;

    /**
     * If light is disabled, it will not take effect.
     */
    protected boolean enabled = true;

    /**
     * @return The color of the light.
     */
    public ColorRGBA getColor() {
        return color;
    }

    public void setLastDistance(float lastDistance){
        this.lastDistance = lastDistance;
    }

    public float getLastDistance(){
        return lastDistance;
    }

    public void setColor(ColorRGBA color){
        this.color.set(color);
    }

    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public Light clone(){
        try {
            return (Light) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(color, "color", null);
        oc.write(enabled, "enabled", true);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        color = (ColorRGBA) ic.readSavable("color", null);
        enabled = ic.readBoolean("enabled", true);
    }

    public abstract void computeLastDistance(Spatial owner);
    public abstract Type getType();

}
