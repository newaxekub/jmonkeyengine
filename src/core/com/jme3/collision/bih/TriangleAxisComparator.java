/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.collision.bih;

import com.jme3.math.Vector3f;
import java.util.Comparator;

public class TriangleAxisComparator implements Comparator<BIHTriangle> {

    private final int axis;

    public TriangleAxisComparator(int axis){
        this.axis = axis;
    }

    public int compare(BIHTriangle o1, BIHTriangle o2) {
        float v1, v2;
        Vector3f c1 = o1.getCenter();
        Vector3f c2 = o2.getCenter();
        switch (axis){
            case 0: v1 = c1.x; v2 = c2.x; break;
            case 1: v1 = c1.y; v2 = c2.y; break;
            case 2: v1 = c1.z; v2 = c2.z; break;
            default: assert false; return 0;
        }
        if (v1 > v2)
            return 1;
        else if (v1 < v2)
            return -1;
        else
            return 0;
    }
}