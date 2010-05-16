package com.jme3.scene.control;

import com.jme3.export.Savable;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;

/**
 * An interface for scene-graph controls. 
 *
 * @author Kirill Vainer
 */
public interface Control extends Savable {

    /**
     * Creates a clone of the Control, the given Spatial is the cloned
     * version of the spatial to which this control is attached to.
     * @param spatial
     * @return
     */
    public Control cloneForSpatial(Spatial spatial);

    /**
     * @param spatial the spatial to be controlled. This should not be called
     * from user code.
     */
    public void setSpatial(Spatial spatial);

    /**
     * @param enabled Enable or disable the control. If disabled, update()
     * should do nothing.
     */
    public void setEnabled(boolean enabled);

    /**
     * @return True if enabled, false otherwise.
     * @see Control#setEnabled(boolean)
     */
    public boolean isEnabled();

    /**
     * Updates the control. This should not be called from user code.
     * @param tpf Time per frame.
     */
    public void update(float tpf);

    /**
     * Should be called prior to queuing the spatial by the RenderManager. This
     * should not be called from user code.
     *
     * @param rm
     * @param vp
     */
    public void render(RenderManager rm, ViewPort vp);
}
