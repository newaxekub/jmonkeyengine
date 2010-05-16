package com.jme3.terrain;

public interface SharedGeomap extends Geomap {
    
    /**
     * Copies the data shared by this SharedGeomap into a new Geomap
     */
    public Geomap copy();

    /**
     * Returns the Geomap from which the data is shared
     */
    public Geomap getParent();

    /**
     * Returns the X offset of the shared Geomap relative to the parent origin
     */
    public int getXOffset();

    /**
     * Returns the Y offset of the shared Geomap relative to the parent origin
     */
    public int getYOffset();

}
