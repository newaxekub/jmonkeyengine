package com.jme3.app.state;

import com.jme3.app.Application;
import com.jme3.renderer.RenderManager;

/**
 * <code>AbstractAppState</code> implements some common methods
 * that make creation of AppStates easier.
 * @author Kirill Vainer
 */
public class AbstractAppState implements AppState {

    private boolean initialized = false;

    public void initialize(AppStateManager stateManager, Application app) {
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void stateAttached(AppStateManager stateManager) {
    }

    public void stateDetached(AppStateManager stateManager) {
    }

    public void update(float tpf) {
    }

    public void render(RenderManager rm) {
    }

    public void cleanup() {
        initialized = false;
    }

}
