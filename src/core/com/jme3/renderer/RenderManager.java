package com.jme3.renderer;

import com.jme3.material.Material;
import com.jme3.math.Matrix3f;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.queue.GeometryList;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shader.Uniform;
import com.jme3.shader.VarType;
import com.jme3.system.Timer;
import com.jme3.util.TempVars;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * <code>RenderManager</code> is a high-level rendering interface that is
 * above the Renderer implementation. RenderManager takes care
 * of rendering the scene graphs attached to each viewport and
 * handling SceneProcessors.
 *
 * @see SceneProcessor
 * @see ViewPort
 * @see Spatial
 */
public class RenderManager {

    private static final Logger logger = Logger.getLogger(RenderManager.class.getName());

    private Renderer renderer;
    private Timer timer;
    private ArrayList<ViewPort> preViewPorts = new ArrayList<ViewPort>();
    private ArrayList<ViewPort> viewPorts = new ArrayList<ViewPort>();
    private ArrayList<ViewPort> postViewPorts = new ArrayList<ViewPort>();
    private Camera prevCam = null;
    private Material forcedMaterial = null;
    private final boolean shader;

    private int viewX, viewY, viewWidth, viewHeight;
    private Matrix4f orthoMatrix = new Matrix4f();
    private Matrix4f viewMatrix = new Matrix4f();
    private Matrix4f projMatrix = new Matrix4f();
    private Matrix4f viewProjMatrix = new Matrix4f();
    private Matrix4f worldMatrix = new Matrix4f();
    private Vector3f camUp = new Vector3f(),
                     camLeft = new Vector3f(),
                     camDir = new Vector3f(),
                     camLoc = new Vector3f();

    /**
     * Create a high-level rendering interface over the
     * low-level rendering interface.
     * @param renderer
     */
    public RenderManager(Renderer renderer){
        this.renderer = renderer;
        this.shader = renderer.getCaps().contains(Caps.GLSL100);
    }

    /**
     * Creates a new viewport, to display the given camera's content.
     * The view will be processed before the primary viewport.
     * @param viewName
     * @param cam
     * @return
     */
     public ViewPort createPreView(String viewName, Camera cam){
        ViewPort vp = new ViewPort(viewName, cam);
        preViewPorts.add(vp);
        return vp;
     }

     public ViewPort createMainView(String viewName, Camera cam){
        ViewPort vp = new ViewPort(viewName, cam);
        viewPorts.add(vp);
        return vp;
     }

     public ViewPort createPostView(String viewName, Camera cam){
        ViewPort vp = new ViewPort(viewName, cam);
        postViewPorts.add(vp);
        return vp;
     }

     private void notifyReshape(ViewPort vp, int w, int h){
        List<SceneProcessor> processors = vp.getProcessors();
        for (SceneProcessor proc : processors){
            if (!proc.isInitialized()){
                proc.initialize(this, vp);
            }else{
                proc.reshape(vp, w, h);
            }
        }
     }

     /**
      * @param w
      * @param h
      */
     public void notifyReshape(int w, int h) {
        for (ViewPort vp : preViewPorts){
            if (vp.getOutputFrameBuffer() == null){
                Camera cam = vp.getCamera();
                cam.resize(w, h, true);
            }
            notifyReshape(vp, w, h);
        }
        for (ViewPort vp : viewPorts){
            if (vp.getOutputFrameBuffer() == null){
                Camera cam = vp.getCamera();
                cam.resize(w, h, true);
            }
            notifyReshape(vp, w, h);
        }
        for (ViewPort vp : postViewPorts){
            if (vp.getOutputFrameBuffer() == null){
                Camera cam = vp.getCamera();
                cam.resize(w, h, true);
            }
            notifyReshape(vp, w, h);
        }
    }

    public void updateUniformBindings(List<Uniform> params){
        // assums worldMatrix is properly set.
        TempVars vars = TempVars.get();
        assert vars.lock();
        
        Matrix4f tempMat4 = vars.tempMat4;
        Matrix3f tempMat3 = vars.tempMat3;
        Vector2f tempVec2 = vars.vect2d;
        Quaternion tempVec4 = vars.quat1;

        for (int i = 0; i < params.size(); i++){
            Uniform u = params.get(i);
            switch (u.getBinding()){
                case WorldMatrix:
                    u.setValue(VarType.Matrix4, worldMatrix);
                    break;
                case ViewMatrix:
                    u.setValue(VarType.Matrix4, viewMatrix);
                    break;
                case ProjectionMatrix:
                    u.setValue(VarType.Matrix4, projMatrix);
                    break;
                case WorldViewMatrix:
                    tempMat4.set(viewMatrix);
                    tempMat4.multLocal(worldMatrix);
                    u.setValue(VarType.Matrix4, tempMat4);
                    break;
                case NormalMatrix:
                    tempMat4.set(viewMatrix);
                    tempMat4.multLocal(worldMatrix);
                    tempMat4.toRotationMatrix(tempMat3);
                    tempMat3.invertLocal();
                    tempMat3.transposeLocal();
                    u.setValue(VarType.Matrix3, tempMat3);
                    break;
                case WorldViewProjectionMatrix:
                    tempMat4.set(viewProjMatrix);
                    tempMat4.multLocal(worldMatrix);
                    u.setValue(VarType.Matrix4, tempMat4);
                    break;
                case ViewMatrixInverse:
                    tempMat4.set(viewMatrix);
                    tempMat4.invertLocal();
                    u.setValue(VarType.Matrix4, tempMat4);
                    break;
                case ViewPort:
                    tempVec4.set(viewX, viewY, viewWidth, viewHeight);
                    u.setValue(VarType.Vector4, tempVec4);
                    break;
                case Resolution:
                    tempVec2.set(viewWidth, viewHeight);
                    u.setValue(VarType.Vector2, tempVec2);
                    break;
                case Aspect:
                    float aspect = ((float) viewWidth) / viewHeight;
                    u.setValue(VarType.Float, aspect);
                    break;
                case CameraPosition:
                    u.setValue(VarType.Vector3, camLoc);
                    break;
                case CameraDirection:
                    u.setValue(VarType.Vector3, camDir);
                    break;
                case CameraLeft:
                    u.setValue(VarType.Vector3, camLeft);
                    break;
                case CameraUp:
                    u.setValue(VarType.Vector3, camUp);
                    break;
                case Time:
                    u.setValue(VarType.Float, timer.getTimeInSeconds());
                    break;
                case Tpf:
                    u.setValue(VarType.Float, timer.getTimePerFrame());
                    break;
                case FrameRate:
                    u.setValue(VarType.Float, timer.getFrameRate());
                    break;
            }
        }

        assert vars.unlock();
    }

    /**
     * Set the material to use to render all future objects.
     * This overrides the material set on the geometry and renders
     * with the provided material instead.
     * Use null to clear the material and return renderer to normal
     * functionality.
     * @param mat
     */
    public void setForcedMaterial(Material mat){
        forcedMaterial = mat;
    }

    public void setWorldMatrix(Matrix4f mat){
        if (shader)
            worldMatrix.set(mat);
        else
            renderer.setWorldMatrix(mat);
    }

    public void renderGeometry(Geometry g) {
        if (g.isIgnoreTransform()){
            setWorldMatrix(Matrix4f.IDENTITY);
        }else{
            setWorldMatrix(g.getWorldMatrix());
        }

        if (forcedMaterial != null){
            // use forced material
            forcedMaterial.render(g, this);
        }else{
            // use geometry's material
            g.getMaterial().render(g, this);
        }
    }

    public void renderGeometryList(GeometryList gl){
        for (int i = 0; i < gl.size(); i++){
            renderGeometry(gl.get(i));
        }
    }

     /**
      * If a spatial is not inside the eye frustum, it
      * is still rendered in the shadow frustum through this
      * recursive method.
      * @param s
      * @param r
      */
    private void renderShadow(Spatial s, RenderQueue rq) {
        if (s instanceof Node){
            Node n = (Node) s;
            List<Spatial> children = n.getChildren();
            for (int i = 0; i < children.size(); i++){
                renderShadow(children.get(i), rq);
            }
        }else if (s instanceof Geometry){
            Geometry gm = (Geometry) s;
            RenderQueue.ShadowMode shadowMode = s.getShadowMode();
            if (shadowMode != RenderQueue.ShadowMode.Off){
                rq.addToShadowQueue(gm, shadowMode);
            }
        }
    }

     /**
      * Render scene graph
      * @param s
      * @param r
      * @param cam
      */
    public void renderScene(Spatial scene, ViewPort vp) {
        // check culling first.
        if (!scene.checkCulling(vp.getCamera())){
            // move on to shadow-only render
            if (scene.getShadowMode() != RenderQueue.ShadowMode.Off)
                renderShadow(scene, vp.getQueue());

            return;
        }

        scene.runControlRender(this, vp);
        if (scene instanceof Node){
            // recurse for all children
            Node n = (Node) scene;
            List<Spatial> children = n.getChildren();
            for (int i = 0; i < children.size(); i++){
                renderScene(children.get(i), vp);
            }
        }else if (scene instanceof Geometry){
            // add to the render queue
            Geometry gm = (Geometry) scene;
            vp.getQueue().addToQueue(gm, scene.getQueueBucket());

            // add to shadow queue if needed
            RenderQueue.ShadowMode shadowMode = scene.getShadowMode();
            if (shadowMode != RenderQueue.ShadowMode.Off){
                vp.getQueue().addToShadowQueue(gm, shadowMode);
            }
        }
    }

    public Camera getCurrentCamera() {
        return prevCam;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public void flushQueue(ViewPort vp){
        RenderQueue rq = vp.getQueue();
        Camera cam = vp.getCamera();
        boolean depthRangeChanged = false;

        // render opaque objects with default depth range
        // opaque objects are sorted front-to-back, reducing overdraw
        rq.renderQueue(Bucket.Opaque, this, cam);

        // render the sky, with depth range set to the farthest
        if (!rq.isQueueEmpty(Bucket.Sky)){
            renderer.setDepthRange(1, 1);
            rq.renderQueue(Bucket.Sky, this, cam);
            depthRangeChanged = true;
        }


        // transparent objects are last because they require blending with the
        // rest of the scene's objects. Consequently, they are sorted
        // back-to-front.
        if (!rq.isQueueEmpty(Bucket.Transparent)){
            if (depthRangeChanged){
                renderer.setDepthRange(0, 1);
                depthRangeChanged = false;
            }
            rq.renderQueue(Bucket.Transparent, this, cam);
        }

        if (!rq.isQueueEmpty(Bucket.Gui)){
            renderer.setDepthRange(0, 0);
            setCamera(cam, true);
            rq.renderQueue(Bucket.Gui, this, cam);
            setCamera(cam, false);
            depthRangeChanged = true;
        }

        // restore range to default
        if (depthRangeChanged)
            renderer.setDepthRange(0, 1);
    }

    private void setViewPort(Camera cam){
        // this will make sure to update viewport only if needed
         if (cam != prevCam || cam.isViewportChanged()){
             viewX      = (int) (cam.getViewPortLeft() * cam.getWidth());
             viewY      = (int) (cam.getViewPortBottom() * cam.getHeight());
             viewWidth  = (int) ((cam.getViewPortRight() - cam.getViewPortLeft()) * cam.getWidth());
             viewHeight = (int) ((cam.getViewPortTop() - cam.getViewPortBottom()) * cam.getHeight());
             renderer.setViewPort(viewX, viewY, viewWidth, viewHeight);
             cam.clearViewportChanged();
             prevCam = cam;

             float translateX = viewWidth  == viewX ? 0 : -(viewWidth  + viewX) / (viewWidth  - viewX);
             float translateY = viewHeight == viewY ? 0 : -(viewHeight + viewY) / (viewHeight - viewY);
             float scaleX     = viewWidth  == viewX ? 1f : 2f / (viewWidth  - viewX);
             float scaleY     = viewHeight == viewY ? 1f : 2f / (viewHeight - viewY);
             orthoMatrix.loadIdentity();
             orthoMatrix.setTranslation(translateX, translateY, 0);
             orthoMatrix.setScale(scaleX, scaleY, -1f);
         }
    }

    private void setViewProjection(Camera cam, boolean ortho){
        if (shader){
            if (ortho){
                viewMatrix.set(Matrix4f.IDENTITY);
                projMatrix.set(orthoMatrix);
                viewProjMatrix.set(orthoMatrix);
            }else{
                viewMatrix.set(cam.getViewMatrix());
                projMatrix.set(cam.getProjectionMatrix());
                viewProjMatrix.set(cam.getViewProjectionMatrix());
            }
            

            camLoc.set(cam.getLocation());
            cam.getLeft(camLeft);
            cam.getUp(camUp);
            cam.getDirection(camDir);
        }else{
            if (ortho){
                renderer.setViewProjectionMatrices(Matrix4f.IDENTITY, orthoMatrix);
            }else{
                renderer.setViewProjectionMatrices(cam.getViewMatrix(),
                                                   cam.getProjectionMatrix());
            }
            
        }
    }

    public void setCamera(Camera cam, boolean ortho){
        setViewPort(cam);
        setViewProjection(cam, ortho);
    }

    public void renderViewPortRaw(ViewPort vp){
        setCamera(vp.getCamera(), false);
        List<Spatial> scenes = vp.getScenes();
        for (int i = scenes.size() - 1; i >= 0; i--){
            renderScene(scenes.get(i), vp);
        }
        flushQueue(vp);
    }

    public void renderViewPort(ViewPort vp, float tpf){
        List<SceneProcessor> processors = vp.getProcessors();
        if (processors.size() == 0)
            processors = null;

        if (processors != null){
            for (SceneProcessor proc : processors){
                if (!proc.isInitialized()){
                    proc.initialize(this, vp);
                }
                proc.preFrame(tpf);
            }
        }
        
        renderer.setFrameBuffer(vp.getOutputFrameBuffer());
        if (vp.isClearEnabled()){
//            renderer.setBackgroundColor(vp.getBackgroundColor());
            renderer.clearBuffers(true, true, true);
        }
        setCamera(vp.getCamera(), false);
        List<Spatial> scenes = vp.getScenes();
        for (int i = scenes.size() - 1; i >= 0; i--){
            renderScene(scenes.get(i), vp);
        }

        if (processors != null){
            for (SceneProcessor proc : processors){
                proc.postQueue(vp.getQueue());
            }
        }

        flushQueue(vp);

        if (processors != null){
            for (SceneProcessor proc : processors){
                proc.postFrame(vp.getOutputFrameBuffer());
            }
        }
    }

     public void render(float tpf){
         for (int i = preViewPorts.size() - 1; i >= 0; i--){
             renderViewPort(preViewPorts.get(i), tpf);
         }
         for (int i = viewPorts.size() - 1; i >= 0; i--){
             renderViewPort(viewPorts.get(i), tpf);
         }
         for (int i = postViewPorts.size() - 1; i >= 0; i--){
             renderViewPort(postViewPorts.get(i), tpf);
         }
     }

}
