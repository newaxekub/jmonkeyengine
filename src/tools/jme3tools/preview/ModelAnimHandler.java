package jme3tools.preview;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.app.Application;
import com.jme3.scene.Spatial;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModelAnimHandler implements IAnimationHandler {

    private Spatial model;
    private AnimControl control;
    private AnimChannel channel;
    private Application app;

    public void setApp(Application app){
        this.app = app;
    }

    public ModelAnimHandler(Spatial model){
        this.model = model;
        control = model.getControl(AnimControl.class);
        channel = control.createChannel();
    }

    public Collection<String> list() {
        try{
            Collection<String> anims = (Collection<String>) app.enqueue(new Callable<Collection<String>>(){
            public Collection<String> call() throws Exception {
                return control.getAnimationNames();
            }
        }).get();
            return anims;
        }catch (InterruptedException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }catch (ExecutionException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public float getLength(final String name) {
        try{
            float len = app.enqueue(new Callable<Float>() {
                public Float call() throws Exception {
                    return control.getAnimationLength(name);
                }
            }).get();
            return len;
        }catch (InterruptedException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }catch (ExecutionException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    public void play(final String name) {
        blendTo(name, 0);
    }

    public void blendTo(final String name, final float time) {
        app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                channel.setAnim(name, time);
                return null;
            }
        });
    }

    public void setSpeed(final float speed) {
        app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                channel.setSpeed(speed);
                return null;
            }
        });
    }

    public float getSpeed() {
        return 1.0f;
    }

    public String getCurrent() {
        try{
            return app.enqueue(new Callable<String>() {
                public String call() throws Exception {
                    return channel.getAnimationName();
                }
            }).get();
        }catch (InterruptedException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }catch (ExecutionException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
