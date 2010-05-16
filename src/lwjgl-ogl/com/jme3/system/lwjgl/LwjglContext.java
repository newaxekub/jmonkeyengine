package com.jme3.system.lwjgl;

import com.jme3.renderer.Renderer;
import com.jme3.renderer.lwjgl.LwjglRenderer;
import com.jme3.system.AppSettings;
import com.jme3.system.SystemListener;
import com.jme3.system.JmeContext;
import com.jme3.system.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A LWJGL implementation of a graphics context.
 */
public abstract class LwjglContext implements JmeContext {

    protected AtomicBoolean created = new AtomicBoolean(false);
    protected final Object createdLock = new Object();

    protected AppSettings settings = new AppSettings(true);
    protected LwjglRenderer renderer;
    protected Timer timer;
    protected SystemListener listener;

    public void setSystemListener(SystemListener listener){
        this.listener = listener;
    }

    public void internalDestroy(){
        renderer = null;
        timer = null;
        synchronized (createdLock){
            created.set(false);
            createdLock.notifyAll();
        }
    }
    
    public void internalCreate(){
        timer = new LwjglTimer();
        renderer = new LwjglRenderer();
        renderer.initialize();
        synchronized (createdLock){
            created.set(true);
            createdLock.notifyAll();
        }
    }

    public void create(){
        create(false);
    }

    public void destroy(){
        destroy(false);
    }

    protected void waitFor(boolean createdVal){
        synchronized (createdLock){
            while (created.get() != createdVal){
                try {
                    createdLock.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    public boolean isCreated(){
        return created.get();
    }

    public void setSettings(AppSettings settings) {
        this.settings.copyFrom(settings);
    }

    public AppSettings getSettings(){
        return settings;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public Timer getTimer() {
        return timer;
    }

}
