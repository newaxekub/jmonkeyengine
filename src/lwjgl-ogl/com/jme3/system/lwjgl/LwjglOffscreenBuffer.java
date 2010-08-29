/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.system.lwjgl;

import com.jme3.input.JoyInput;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.dummy.DummyKeyInput;
import com.jme3.input.dummy.DummyMouseInput;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;


public class LwjglOffscreenBuffer extends LwjglContext implements Runnable {

    private static final Logger logger = Logger.getLogger(LwjglOffscreenBuffer.class.getName());
    private Pbuffer pbuffer;
    protected AtomicBoolean needClose = new AtomicBoolean(false);
    private int width;
    private int height;
    private PixelFormat pixelFormat;

    protected void initInThread(){
        if ((Pbuffer.getCapabilities() & Pbuffer.PBUFFER_SUPPORTED) == 0){
            logger.severe("Offscreen surfaces are not supported.");
            return;
        }

        pixelFormat = new PixelFormat(settings.getBitsPerPixel(),
                                         0,
                                         settings.getDepthBits(),
                                         settings.getStencilBits(),
                                         settings.getSamples());
        width = settings.getWidth();
        height = settings.getHeight();
        try{
            //String rendererStr = settings.getString("Renderer");
//            if (rendererStr.startsWith("LWJGL-OpenGL3")){
//                ContextAttribs attribs;
//                if (rendererStr.equals("LWJGL-OpenGL3.1")){
//                    attribs = new ContextAttribs(3, 1);
//                }else{
//                    attribs = new ContextAttribs(3, 0);
//                }
//                attribs.withForwardCompatible(true);
//                attribs.withDebug(false);
//                Display.create(pf, attribs);
//            }else{
              pbuffer = new Pbuffer(width, height, pixelFormat, null);
//            }

            pbuffer.makeCurrent();

            logger.info("Offscreen buffer created.");
            logger.log(Level.FINE, "Running on thread: {0}", Thread.currentThread().getName());

            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread thread, Throwable thrown) {
                    listener.handleError("Uncaught exception thrown in "+thread.toString(), thrown);
                }
            });

            String vendor = GL11.glGetString(GL11.GL_VENDOR);
            logger.log(Level.INFO, "Vendor: {0}", vendor);

            String version = GL11.glGetString(GL11.GL_VERSION);
            logger.log(Level.INFO, "OpenGL Version: {0}", version);

            String renderer = GL11.glGetString(GL11.GL_RENDERER);
            logger.log(Level.INFO, "Renderer: {0}", renderer);

            if (GLContext.getCapabilities().OpenGL20){
                String shadingLang = GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION);
                logger.log(Level.INFO, "GLSL Ver: {0}", shadingLang);
            }

            created.set(true);
        } catch (LWJGLException ex){
            listener.handleError("Failed to create display", ex);
        } finally {
            // TODO: It is possible to avoid "Failed to find pixel format"
            // error here by creating a default display.
        }
        super.internalCreate();
        listener.initialize();
    }

    protected void runLoop(){
        if (!created.get())
            throw new IllegalStateException();

        if (pbuffer.isBufferLost()){
            pbuffer.destroy();
            try{
                pbuffer = new Pbuffer(width, height, pixelFormat, null);
            }catch (LWJGLException ex){
                listener.handleError("Failed to restore pbuffer content", ex);
            }
        }

        try{
            pbuffer.makeCurrent();
        }catch (LWJGLException ex){
            listener.handleError( "Error occured while making pbuffer current", ex);
        }
        listener.update();
        renderer.onFrame();
    }

    protected void deinitInThread(){
        listener.destroy();
        renderer.cleanup();
        pbuffer.destroy();
        logger.info("Offscreen buffer destroyed.");
    }

    public void run(){
        logger.log(Level.INFO, "Using LWJGL {0}", Sys.getVersion());
        initInThread();
        while (!needClose.get()){
            runLoop();
        }
        deinitInThread();
    }

    public void destroy(boolean waitFor){
        needClose.set(true);
        if (waitFor)
            waitFor(false);
    }

    public void create(boolean waitFor){
        if (created.get()){
            logger.warning("create() called when pbuffer is already created!");
            return;
        }

        new Thread(this, "LWJGL Renderer Thread").start();
        if (waitFor)
            waitFor(true);
    }

    public void restart() {
    }

    public void setAutoFlushFrames(boolean enabled){
    }

    public Type getType() {
        return Type.OffscreenSurface;
    }

    public MouseInput getMouseInput() {
        return new DummyMouseInput();
    }

    public KeyInput getKeyInput() {
        return new DummyKeyInput();
    }

    public JoyInput getJoyInput() {
        return null;
    }

    public void setTitle(String title) {
    }

}
