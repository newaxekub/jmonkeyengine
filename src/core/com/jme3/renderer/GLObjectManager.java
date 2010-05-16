package com.jme3.renderer;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GLObjectManager tracks all GLObjects used by the Renderer. Using a
 * <code>ReferenceQueue</code> the <code>GLObjectManager</code> can delete
 * unused objects from GPU when their counterparts on the CPU are no longer used.
 *
 * On restart, the renderer may request the objects to be reset, thus allowing
 * the GLObjects to re-initialize with the new display context.
 */
public class GLObjectManager {

    private static final Logger logger = Logger.getLogger(GLObjectManager.class.getName());

    /**
     * The queue will receive notifications of GLObjects which are no longer
     * referenced.
     */
    private ReferenceQueue<Object> refQueue = new ReferenceQueue<Object>();

    /**
     * List of currently active GLObjects.
     */
    private ArrayList<GLObjectRef> refList
            = new ArrayList<GLObjectRef>();

    private class GLObjectRef extends PhantomReference<Object>{
        
        private GLObject objClone;
        private WeakReference<GLObject> realObj;

        public GLObjectRef(GLObject obj){
            super(obj.handleRef, refQueue);
            this.realObj = new WeakReference<GLObject>(obj);
            this.objClone = obj.createDestructableClone();
        }
    }

    /**
     * Register a GLObject with the manager.
     */
    public void registerForCleanup(GLObject obj){
        GLObjectRef ref = new GLObjectRef(obj);
        refList.add(ref);
        if (logger.isLoggable(Level.FINEST))
            logger.log(Level.FINEST, "Registered: {0}", new String[]{obj.toString()});
    }

    /**
     * Deletes unused GLObjects
     */
    public void deleteUnused(Renderer r){
        while (true){
            GLObjectRef ref = (GLObjectRef) refQueue.poll();
            if (ref == null)
                return;

            refList.remove(ref);
            ref.objClone.deleteObject(r);
            if (logger.isLoggable(Level.FINEST))
                logger.log(Level.FINEST, "Deleted: {0}", ref.objClone);
        }
    }

    /**
     * Deletes all objects. Must only be called when display is destroyed.
     */
    public void deleteAllObjects(Renderer r){
        deleteUnused(r);
        for (GLObjectRef ref : refList){
            ref.objClone.deleteObject(r);
        }
        refList.clear();
    }

    /**
     * Resets all GLObjects.
     */
    public void resetObjects(){
        for (GLObjectRef ref : refList){
            // here we use the actual obj not the clone,
            // otherwise its useless
            GLObject realObj = ref.realObj.get();
            if (realObj == null)
                continue;
            
            realObj.resetObject();
            if (logger.isLoggable(Level.FINEST))
                logger.log(Level.FINEST, "Reset: {0}", realObj);
        }
        refList.clear();
    }

//    public void printObjects(){
//        System.out.println(" ------------------- ");
//        System.out.println(" GL Object count: "+ objectList.size());
//        for (GLObject obj : objectList){
//            System.out.println(obj);
//        }
//    }
}
