package com.jme3.renderer.queue;

import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.ui.UIElement;

public class GuiComparator implements GeometryComparator {

    public int compare(Geometry o1, Geometry o2) {
        if (o1 instanceof UIElement && o2 instanceof UIElement){
            UIElement e1 = (UIElement) o1;
            UIElement e2 = (UIElement) o2;
            if (e1.getZOrder() > e2.getZOrder())
                return 1;
            else if (e1.getZOrder() < e2.getZOrder())
                return -1;
            else
                return 0;
        }
        return 0;
    }

    public void setCamera(Camera cam) {
    }

}
