package com.jme3.app.state;

import com.jme3.app.Application;
import com.jme3.renderer.RenderManager;

/**
 * AppState represents a continously executing code inside the main loop.
 *
 * @author Kirill Vainer
 */
public interface AppState {

    /**
     * Called to initialize the AppState.
     *
     * @param stateManager The state manager
     * @param app
     */
    public void initialize(AppStateManager stateManager, Application app);

    /**
     * @return True if <code>initialize()</code> was called on the state,
     * false otherwise.
     */
    public boolean isInitialized();

    /**
     * Called when the state was attached.
     *
     * @param stateManager State manager to which the state was attached to.
     */
    public void stateAttached(AppStateManager stateManager);

   /**
    * Called when the state was detached.
    *
    * @param stateManager The state manager from which the state was detached from.
    */
    public void stateDetached(AppStateManager stateManager);

    /**
     * Called to update the state.
     *
     * @param tpf Time per frame.
     */
    public void update(float tpf);

    /**
     * Render the state.
     *
     * @param rm RenderManager
     */
    public void render(RenderManager rm);

    /**
     * Cleanup the game state. 
     */
    public void cleanup();

}
