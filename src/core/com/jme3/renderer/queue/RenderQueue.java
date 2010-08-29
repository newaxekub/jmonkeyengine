package com.jme3.renderer.queue;

import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

public class RenderQueue {

    private GeometryList opaqueList;
    private GeometryList guiList;
    private GeometryList transparentList;
    private GeometryList skyList;
    private GeometryList shadowRecv;
    private GeometryList shadowCast;

    public RenderQueue(){
        this.opaqueList =  new GeometryList(new OpaqueComparator());
        this.guiList = new GeometryList(new GuiComparator());
        this.transparentList = new GeometryList(new TransparentComparator());
        this.skyList = new GeometryList(new NullComparator());
        this.shadowRecv = new GeometryList(new OpaqueComparator());
        this.shadowCast = new GeometryList(new OpaqueComparator());
    }

    public enum Bucket {
        Gui,
        Opaque,
        Sky,
        Transparent,
        Inherit,
    }

    public enum ShadowMode {
        Off,
        Cast,
        Recieve,
        CastAndRecieve,
        Inherit
    }

    public void addToShadowQueue(Geometry g, ShadowMode shadBucket){
        switch (shadBucket){
            case Inherit: break;
            case Off: break;
            case Cast:
                shadowCast.add(g);
                break;
            case Recieve:
                shadowRecv.add(g);
                break;
            case CastAndRecieve:
                shadowCast.add(g);
                shadowRecv.add(g);
                break;
            default:
                throw new UnsupportedOperationException("Unrecognized shadow bucket type: "+shadBucket);
        }
    }

    public void addToQueue(Geometry g, Bucket bucket) {
        switch (bucket) {
            case Gui:
                guiList.add(g);
                break;
            case Opaque:
                opaqueList.add(g);
                break;
            case Sky:
                skyList.add(g);
                break;
            case Transparent:
                transparentList.add(g);
                break;
            default:
                throw new UnsupportedOperationException("Unknown bucket type: "+bucket);
        }
    }

    public GeometryList getShadowQueueContent(ShadowMode shadBucket){
        switch (shadBucket){
            case Cast:
                return shadowCast;
            case Recieve:
                return shadowRecv;
            default:
                return null;
        }
    }

    private void renderGeometryList(GeometryList list, RenderManager rm, Camera cam, boolean clear){
        list.setCamera(cam); // select camera for sorting
        list.sort();
        for (int i = 0; i < list.size(); i++){
            Spatial obj = list.get(i);
            assert obj != null;

            if (obj instanceof Geometry){
                Geometry g = (Geometry) obj;
                rm.renderGeometry(g);
                // make sure to reset queue distance
            }

            if (obj != null)
                obj.queueDistance = Float.NEGATIVE_INFINITY;
        }
        if (clear)
            list.clear();
    }

    public void renderShadowQueue(ShadowMode shadBucket, RenderManager rm, Camera cam, boolean clear){
        switch (shadBucket){
            case Cast:
                renderGeometryList(shadowCast, rm, cam, clear);
                break;
            case Recieve:
                renderGeometryList(shadowRecv, rm, cam, clear);
                break;
        }
    }

    public boolean isQueueEmpty(Bucket bucket){
        switch (bucket){
            case Gui:
                return guiList.size() == 0;
            case Opaque:
                return opaqueList.size() == 0;
            case Sky:
                return skyList.size() == 0;
            case Transparent:
                return transparentList.size() == 0;
            default:
                throw new UnsupportedOperationException("Unsupported bucket type: "+bucket);
        }
    }

    public void renderQueue(Bucket bucket, RenderManager rm, Camera cam){
        renderQueue(bucket, rm, cam, true);
    }

    public void renderQueue(Bucket bucket, RenderManager rm, Camera cam, boolean clear){
        switch (bucket){
            case Gui:
                renderGeometryList(guiList, rm, cam, clear);
                break;
            case Opaque:
                renderGeometryList(opaqueList, rm, cam, clear);
                break;
            case Sky:
                renderGeometryList(skyList, rm, cam, clear);
                break;
            case Transparent:
                renderGeometryList(transparentList, rm, cam, clear);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported bucket type: "+bucket);
        }
    }

    public void clear(){
        opaqueList.clear();
        guiList.clear();
        transparentList.clear();
        skyList.clear();
        shadowCast.clear();
        shadowRecv.clear();
    }

}
