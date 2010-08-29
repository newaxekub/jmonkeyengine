package com.jme3.renderer.layer;

import com.jme3.renderer.*;
import com.jme3.math.ColorRGBA;
import java.util.Collection;

/**
 * Handles basic renderer functions.
 */
public interface MiscLayer {

    /**
     * @return The capabilities of the renderer.
     */
    public Collection<Caps> getCaps();

    /**
     * Clears certain channels of the current bound framebuffer.
     *
     * @param color True if to clear colors (RGBA)
     * @param depth True if to clear depth/z
     * @param stencil True if to clear stencil buffer (if available, otherwise
     * ignored)
     */
    public void clearBuffers(boolean color, boolean depth, boolean stencil);

    /**
     * Sets the background (aka clear) color.
     * @param color
     */
    public void setBackgroundColor(ColorRGBA color);

}
