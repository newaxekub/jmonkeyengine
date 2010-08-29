package com.jme3.post;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;

/**
 * <code>PostSceneProcessor</code> is an implementation of SceneProcessor
 * that simplifies creation and combination of scene post-processing filters,
 * for example, color correction, radial blur, depth of field, etc.
 *
 * It is used by simply attaching the <code>PostSceneProcessor</code> to the
 * scene's viewport and then attaching and configuring the filters.
 *
 * @author Kirill Vainer
 */
public class PostSceneProcessor implements SceneProcessor {
  
    private FrameBuffer sceneFb;
    private ViewPort viewPort;
    private int samples;

    public PostSceneProcessor(int samples){
        this.samples = samples;
    }

    private void createSceneFB(){
        sceneFb = new FrameBuffer(viewPort.getCamera().getWidth(),
                                  viewPort.getCamera().getHeight(),
                                  samples);
    }

    /**
     * Do not call directly.
     * @see SceneProcessor#isInitialized()
     */
    public void initialize(RenderManager rm, ViewPort vp) {
        viewPort = vp;
        createSceneFB();
    }

    /**
     * Do not call directly.
     * @see SceneProcessor#isInitialized()
     */
    public void reshape(ViewPort vp, int w, int h) {
        viewPort = vp;
        createSceneFB();
    }

    /**
     * Do not call directly.
     * @see SceneProcessor#isInitialized()
     */
    public boolean isInitialized() {
        return sceneFb != null;
    }

    /**
     * Do not call directly.
     * @see SceneProcessor#preFrame(samples)
     */
    public void preFrame(float tpf) {
    }

    /**
     * Do not call directly.
     * @see SceneProcessor#postQueue(com.jme3.renderer.queue.RenderQueue)
     */
    public void postQueue(RenderQueue rq) {
    }

    /**
     * Do not call directly.
     * @see SceneProcessor#postFrame(com.jme3.texture.FrameBuffer)
     */
    public void postFrame(FrameBuffer out) {
    }

    /**
     * Do not call directly.
     * @see SceneProcessor#cleanup()
     */
    public void cleanup() {
    }

}
