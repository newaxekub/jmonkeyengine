package jme3tools.optimize;

import com.jme3.math.Vector3f;

public final class OCTTriangle {

    private final Vector3f pointa = new Vector3f();
    private final Vector3f pointb = new Vector3f();
    private final Vector3f pointc = new Vector3f();
    private final int index;
    private final int geomIndex;

    public OCTTriangle(Vector3f p1, Vector3f p2, Vector3f p3, int index, int geomIndex) {
        pointa.set(p1);
        pointb.set(p2);
        pointc.set(p3);
        this.index = index;
        this.geomIndex = geomIndex;
    }

    public int getGeometryIndex() {
        return geomIndex;
    }

    public int getTriangleIndex() {
        return index;
    }
    
    public Vector3f get1(){
        return pointa;
    }

    public Vector3f get2(){
        return pointb;
    }

    public Vector3f get3(){
        return pointc;
    }

    public Vector3f getNormal(){
        Vector3f normal = new Vector3f(pointb);
        normal.subtractLocal(pointa).crossLocal(pointc.x-pointa.x, pointc.y-pointa.y, pointc.z-pointa.z);
        normal.normalizeLocal();
        return normal;
    }

}
