package com.jme3.post;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;

public interface SceneProcessor {

    /**
     * Called in the render thread to initialize the scene processor.
     *
     * @param rm The render manager to which the SP was added to
     * @param vp The viewport to which the SP is assigned
     */
    public void initialize(RenderManager rm, ViewPort vp);

    /**
     * Called when the resolution of the viewport has been changed.
     * @param vp
     */
    public void reshape(ViewPort vp, int w, int h);

    /**
     * @return True if initialize() has been called on this SceneProcessor,
     * false if otherwise.
     */
    public boolean isInitialized();

    /**
     * Called before a frame
     *
     * @param tpf Time per frame
     */
    public void preFrame(float tpf);

    /**
     * Called after the scene graph has been queued, but before it is flushed.
     *
     * @param rq The render queue
     */
    public void postQueue(RenderQueue rq);

    /**
     * Called after a frame has been rendered and the queue flushed.
     *
     * @param out The FB to which the scene was rendered.
     */
    public void postFrame(FrameBuffer out);

    /**
     * Called when the SP is removed from the RM.
     */
    public void cleanup();

}
