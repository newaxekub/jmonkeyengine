package com.jme3.app.state;

import com.jme3.app.Application;
import com.jme3.renderer.RenderManager;
import java.util.ArrayList;

public class AppStateManager {

    private final ArrayList<AppState> states = new ArrayList<AppState>();
    private final Application app;

    public AppStateManager(Application app){
        this.app = app;
    }

    /**
     * Attach a state to the AppStateManager, the same state cannot be attached
     * twice.
     *
     * @param state The state to attach
     * @return True if the state was successfully attached, false if the state
     * was already attached.
     */
    public boolean attach(AppState state){
        if (!states.contains(state)){
            state.stateAttached(this);
            states.add(state);
            return true;
        }else{
            return false;
        }
    }

    /**
     * Detaches the state from the AppStateManager. 
     *
     * @param state The state to detach
     * @return True if the state was detached successfully, false
     * if the state was not attached in the first place.
     */
    public boolean detach(AppState state){
        if (states.contains(state)){
            state.stateDetached(this);
            states.remove(state);
            return true;
        }else{
            return false;
        }
    }

    /**
     * Check if a state is attached or not.
     *
     * @param state The state to check
     * @return True if the state is currently attached to this AppStateManager.
     * 
     * @see AppStateManager#attach(com.jme3.app.state.AppState)
     */
    public boolean hasState(AppState state){
        return states.contains(state);
    }

    /**
     * Returns the first state that is an instance of subclass of the specified class.
     * @param <T>
     * @param stateClass
     * @return First attached state that is an instance of stateClass
     */
    public <T extends AppState> T getState(Class<T> stateClass){
        int num = states.size();
        for (int i = 0; i < num; i++){
            AppState state = states.get(i);
            if (stateClass.isAssignableFrom(state.getClass())){
                return (T) state;
            }
        }
        return null;
    }

    /**
     * Calls update for attached states, do not call directly.
     * @param tpf Time per frame.
     */
    public void update(float tpf){
        synchronized (states){
            int num = states.size();
            for (int i = 0; i < num; i++){
                AppState state = states.get(i);
                if (!state.isInitialized())
                    state.initialize(this, app);

                state.update(tpf);
            }
        }
        
    }

    /**
     * Calls render for all attached states, do not call directly.
     * @param rm The RenderManager
     */
    public void render(RenderManager rm){
        synchronized (states){
            int num = states.size();
            for (int i = 0; i < num; i++){
                AppState state = states.get(i);
                if (!state.isInitialized())
                    state.initialize(this, app);

                state.render(rm);
            }
        }
    }

    /**
     * Calls cleanup on attached states, do not call directly.
     */
    public void cleanup(){
        synchronized (states){
            for (int i = 0; i < states.size(); i++){
                AppState state = states.get(i);
                state.cleanup();
            }
        }
    }
}
