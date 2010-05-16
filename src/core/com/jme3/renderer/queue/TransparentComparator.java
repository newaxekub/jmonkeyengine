package com.jme3.renderer.queue;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.util.TempVars;

public class TransparentComparator implements GeometryComparator {

    private Camera cam;

    public void setCamera(Camera cam){
        this.cam = cam;
    }

    /**
     * Calculates the distance from a spatial to the camera. Distance is a
     * squared distance.
     *
     * @param spat
     *            Spatial to distancize.
     * @return Distance from Spatial to camera.
     */
    public float distanceToCam(Geometry spat, Vector3f tempVec){
        if (spat == null)
            return Float.NEGATIVE_INFINITY;

        if (spat.queueDistance != Float.NEGATIVE_INFINITY)
                return spat.queueDistance;

        Vector3f camPosition = cam.getLocation();
        Vector3f viewVector = cam.getDirection();
        Vector3f spatPosition = null;

        if (spat.getWorldBound() != null){
            spatPosition = spat.getWorldBound().getCenter();
        }else{
            spatPosition = spat.getWorldTranslation();
        }

        spatPosition.subtract(camPosition, tempVec);
        spat.queueDistance = tempVec.dot(tempVec);

        float retval = Math.abs(tempVec.dot(viewVector)
                / viewVector.dot(viewVector));
        tempVec = viewVector.mult(retval, tempVec);

        spat.queueDistance = tempVec.length();

        return spat.queueDistance;
    }

    public int compare(Geometry o1, Geometry o2) {
        assert TempVars.get().lock();
        Vector3f tempVec = TempVars.get().vect1;
        float d1 = distanceToCam(o1, tempVec);
        float d2 = distanceToCam(o2, tempVec);
        assert TempVars.get().unlock();

        if (d1 == d2)
            return 0;
        else if (d1 < d2)
            return 1;
        else
            return -1;
    }
}
