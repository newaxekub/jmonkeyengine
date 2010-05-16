package com.jme3.shadow;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.renderer.queue.GeometryList;
import com.jme3.asset.AssetManager;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

public class BasicShadowRenderer implements SceneProcessor {

    private RenderManager renderManager;
    private ViewPort viewPort;

    private FrameBuffer shadowFB;
    private Texture2D shadowMap;
    private Camera shadowCam;

    private Material preshadowMat;
    private Material postshadowMat;

    private Picture dispPic = new Picture("Picture");
    private boolean noOccluders = false;

    private Vector3f[] points = new Vector3f[8];
    private Vector3f direction = new Vector3f();

    public BasicShadowRenderer(AssetManager manager, int size){
        shadowFB =  new FrameBuffer(size,size,0);
        shadowMap = new Texture2D(size,size,Format.Depth);
        shadowFB.setDepthTexture(shadowMap);
        shadowCam = new Camera(size,size);
        
        preshadowMat  = new Material(manager, "Common/MatDefs/Shadow/PreShadow.j3md");
        postshadowMat = new Material(manager, "Common/MatDefs/Shadow/PostShadow.j3md");
        postshadowMat.setTexture("m_ShadowMap", shadowMap);

        dispPic.setTexture(manager, shadowMap, false);

        for (int i = 0; i < points.length; i++){
            points[i] = new Vector3f();
        }
    }

    public void initialize(RenderManager rm, ViewPort vp){
        renderManager = rm;
        viewPort = vp;
    }

    public boolean isInitialized(){
        return viewPort != null;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction.set(direction).normalizeLocal();
    }

    public Vector3f[] getPoints() {
        return points;
    }

    public Camera getShadowCamera(){
        return shadowCam;
    }

    public void postQueue(RenderQueue rq){
        GeometryList occluders = rq.getShadowQueueContent(ShadowMode.Cast);
        if (occluders.size() == 0){
            noOccluders = true;
            return;
        }else{
            noOccluders = false;
        }

        GeometryList recievers = rq.getShadowQueueContent(ShadowMode.Recieve);

        // update frustum points based on current camera
        Camera viewCam = viewPort.getCamera();
        ShadowUtil.updateFrustumPoints(viewCam,
                                       viewCam.getFrustumNear(),
                                       viewCam.getFrustumFar(), 
                                       1.0f,
                                       points);

        Vector3f frustaCenter = new Vector3f();
        for (Vector3f point : points){
            frustaCenter.addLocal(point);
        }
        frustaCenter.multLocal(1f / 8f);

        // update light direction
        shadowCam.setProjectionMatrix(null);
        shadowCam.setParallelProjection(true);
//        shadowCam.setFrustumPerspective(45, 1, 1, 20);
        
        shadowCam.setDirection(direction);
        shadowCam.update();
        shadowCam.setLocation(frustaCenter);
        shadowCam.update();
        shadowCam.updateViewProjection();

        // render shadow casters to shadow map
        ShadowUtil.updateShadowCamera(occluders, recievers, shadowCam, points);

        Renderer r = renderManager.getRenderer();
        renderManager.setCamera(shadowCam, false);
        renderManager.setForcedMaterial(preshadowMat);

        r.setFrameBuffer(shadowFB);
        r.clearBuffers(false,true,false);
        viewPort.getQueue().renderShadowQueue(ShadowMode.Cast, renderManager, shadowCam);
        r.setFrameBuffer(viewPort.getOutputFrameBuffer());

        renderManager.setForcedMaterial(null);
        renderManager.setCamera(viewCam, false);
    }

    public void displayShadowMap(Renderer r){
        Camera cam = viewPort.getCamera();
        int w = cam.getWidth();
        int h = cam.getHeight();

        dispPic.setPosition(w / 20f, h / 20f);
        dispPic.setWidth(w / 5f);
        dispPic.setHeight(h / 5f);
        dispPic.updateGeometricState();
        renderManager.renderGeometry(dispPic);
    }

    public void postFrame(FrameBuffer out){
        if (!noOccluders){
            postshadowMat.setMatrix4("m_LightViewProjectionMatrix", shadowCam.getViewProjectionMatrix());
            renderManager.setForcedMaterial(postshadowMat);
            viewPort.getQueue().renderShadowQueue(ShadowMode.Recieve, renderManager, viewPort.getCamera());
            renderManager.setForcedMaterial(null);
        }

//        displayShadowMap(renderManager.getRenderer());
    }

    public void preFrame(float tpf) {
    }

    public void cleanup() {
    }

    public void reshape(ViewPort vp, int w, int h) {
    }

}
