package com.jme3.renderer.queue;

import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import java.util.Comparator;

public interface GeometryComparator extends Comparator<Geometry> {
    public void setCamera(Camera cam);
}
