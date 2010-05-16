package com.jme3.scene.control;

import com.jme3.bounding.BoundingVolume;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import java.io.IOException;

public class LodControl extends AbstractControl implements Cloneable {

    private float trisPerPixel = 1f;
    private float distTolerance = 1f;
    private float lastDistance = 0f;
    private int lastLevel = 0;
    private int numLevels;
    private int[] numTris;

    public LodControl(Geometry geom){
        super(geom);
        Mesh mesh = geom.getMesh();
        numLevels = mesh.getNumLodLevels();
        numTris = new int[numLevels];
        for (int i = numLevels - 1; i >= 0; i--)
            numTris[i] = mesh.getTriangleCount(i);

    }

    public LodControl(){
    }

    public Control cloneForSpatial(Spatial spatial) {
        try {
            LodControl clone = (LodControl) super.clone();
            clone.lastDistance = 0;
            clone.lastLevel = 0;
            clone.numTris = numTris != null ? numTris.clone() : null;
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    protected void controlRender(RenderManager rm, ViewPort vp){
        BoundingVolume bv = spatial.getWorldBound();

        Camera cam = vp.getCamera();
        float atanNH = FastMath.atan(cam.getFrustumNear() * cam.getFrustumTop());
        float ratio = (FastMath.PI / (8f * atanNH));
        float newDistance = bv.distanceTo(vp.getCamera().getLocation()) / ratio;
        int level;

        if (Math.abs(newDistance - lastDistance) <= distTolerance)
            level = lastLevel; // we haven't moved relative to the model, send the old measurement back.
        else if (lastDistance > newDistance && lastLevel == 0)
            level = lastLevel; // we're already at the lowest setting and we just got closer to the model, no need to keep trying.
        else if (lastDistance < newDistance && lastLevel == numLevels - 1)
            level = lastLevel; // we're already at the highest setting and we just got further from the model, no need to keep trying.
        else{
            lastDistance = newDistance;

            // estimate area of polygon via bounding volume
            float area = AreaUtils.calcScreenArea(bv, lastDistance, cam.getWidth());
            float trisToDraw = area * trisPerPixel;
            level = numLevels - 1;
            for (int i = numLevels; --i >= 0;){
                if (trisToDraw - numTris[i] < 0){
                    break;
                }
                level = i;
            }
            lastLevel = level;
        }

        spatial.setLodLevel(level);
    }

    @Override
    public void write(JmeExporter ex) throws IOException{
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(trisPerPixel, "trisPerPixel", 1f);
        oc.write(distTolerance, "distTolerance", 1f);
        oc.write(numLevels, "numLevels", 0);
        oc.write(numTris, "numTris", null);
    }

    @Override
    public void read(JmeImporter im) throws IOException{
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        trisPerPixel = ic.readFloat("trisPerPixel", 1f);
        distTolerance = ic.readFloat("distTolerance", 1f);
        numLevels = ic.readInt("numLevels", 0);
        numTris = ic.readIntArray("numTris", null);
    }

}
