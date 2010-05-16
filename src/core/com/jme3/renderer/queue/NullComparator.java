/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.renderer.queue;

import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;

public class NullComparator implements GeometryComparator {
    public int compare(Geometry o1, Geometry o2) {
        return 0;
    }

    public void setCamera(Camera cam) {
    }
}
