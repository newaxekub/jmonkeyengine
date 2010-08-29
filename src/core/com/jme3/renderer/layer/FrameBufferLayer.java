package com.jme3.renderer.layer;

import com.jme3.texture.FrameBuffer;

/**
 * Layer for handling framebuffers
 */
public interface FrameBufferLayer {

    /**
     * Copies contents from src to dst, scaling if neccessary.
     */
    public void copyFrameBuffer(FrameBuffer src, FrameBuffer dst);

    /**
     * Sets the framebuffer that will be drawn to.
     */
    public void setFrameBuffer(FrameBuffer fb);

    /**
     * Initializes the framebuffer, creating it if neccessary and allocating
     * requested renderbuffers.
     */
    public void updateFrameBuffer(FrameBuffer fb);

    /**
     * Deletes a framebuffer and all attached renderbuffers
     */
    public void deleteFrameBuffer(FrameBuffer fb);

}
