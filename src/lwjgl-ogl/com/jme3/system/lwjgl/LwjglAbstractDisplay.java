/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jme3.system.lwjgl;

import com.jme3.input.JoyInput;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.lwjgl.JInputJoyInput;
import com.jme3.input.lwjgl.LwjglKeyInput;
import com.jme3.input.lwjgl.LwjglMouseInput;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext.Type;
import com.jme3.system.JmeSystem;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.OpenGLException;
import org.lwjgl.opengl.Util;


public abstract class LwjglAbstractDisplay extends LwjglContext implements Runnable {

    private static final Logger logger = Logger.getLogger(LwjglDisplay.class.getName());
    protected AtomicBoolean needClose = new AtomicBoolean(false);
    protected boolean wasActive = false;
    protected int frameRate = 0;
    protected boolean autoFlush = true;

    /**
     * @return Type.Display or Type.Canvas
     */
    public abstract Type getType();

    /**
     * Set the title if its a windowed display
     * @param title
     */
    public abstract void setTitle(String title);

    /**
     * Restart if its a windowed or full-screen display.
     */
    public abstract void restart();

    /**
     * Apply the settings, changing resolution, etc.
     * @param settings
     */
    protected abstract void createContext(AppSettings settings) throws LWJGLException;

    /**
     * Does LWJGL display initialization in the OpenGL thread
     */
    protected void initInThread(){
        try{
            if (!JmeSystem.isLowPermissions()){
                Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    public void uncaughtException(Thread thread, Throwable thrown) {
                        listener.handleError("Uncaught exception thrown in "+thread.toString(), thrown);
                    }
                });
            }

            createContext(settings);
//            String rendererStr = settings.getString("Renderer");

            logger.info("Display created.");
            logger.log(Level.FINE, "Running on thread: {0}", Thread.currentThread().getName());

            logger.log(Level.INFO, "Adapter: {0}", Display.getAdapter());
            logger.log(Level.INFO, "Driver Version: {0}", Display.getVersion());

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
        } catch (Exception ex){
            listener.handleError("Failed to create display", ex);
        } finally {
            // TODO: It is possible to avoid "Failed to find pixel format"
            // error here by creating a default display.

            if (!created.get()){
                if (Display.isCreated())
                    Display.destroy();

                return; // if we failed to create display, do not continue
            }
        }
        super.internalCreate();
        listener.initialize();
    }

    protected boolean checkGLError(){
        try {
            Util.checkGLError();
        } catch (OpenGLException ex){
            listener.handleError("An OpenGL error has occured!", ex);
        }
        // NOTE: Always return true since this is used in an "assert" statement
        return true;
    }

    /**
     * execute one iteration of the render loop in the OpenGL thread
     */
    protected void runLoop(){
        if (!created.get())
            throw new IllegalStateException();

        listener.update();
        assert checkGLError();

        // calls swap buffers, etc.
        try {
            if (autoFlush){
                Display.update();
            }else{
                Display.processMessages();
                Thread.sleep(50);
                // add a small wait
                // to reduce CPU usage
            }
        } catch (Throwable ex){
            listener.handleError("Error while swapping buffers", ex);
        }

        if (frameRate > 0)
            Display.sync(frameRate);

        if (autoFlush)
            renderer.onFrame();
    }

    /**
     * De-initialize in the OpenGL thread.
     */
    protected void deinitInThread(){
        if (Display.isCreated()){
            renderer.cleanup();
            Display.destroy();
        }else{
            // If using canvas temporary closing, the display would
            // be closed at this point
            renderer.resetGLObjects();
        }

        listener.destroy();
        logger.info("Display destroyed.");
        super.internalDestroy();
    }

    public void run(){
        if (listener == null)
            throw new IllegalStateException("SystemListener is not set on context!"
                                          + "Must set with JmeContext.setSystemListner().");

        logger.log(Level.INFO, "Using LWJGL {0}", Sys.getVersion());
        initInThread();
        while (true){
            if (Display.isCloseRequested())
                listener.requestClose(false);

            if (wasActive != Display.isActive()){
                if (!wasActive){
                    listener.gainFocus();
                    wasActive = true;
                }else{
                    listener.loseFocus();
                    wasActive = false;
                }
            }

            runLoop();

            if (needClose.get())
                break;
        }
        deinitInThread();
    }

    public JoyInput getJoyInput() {
        return new JInputJoyInput();
    }

    public MouseInput getMouseInput() {
        return new LwjglMouseInput();
    }

    public KeyInput getKeyInput() {
        return new LwjglKeyInput();
    }

    public void setAutoFlushFrames(boolean enabled){
        this.autoFlush = enabled;
    }

    public void destroy(boolean waitFor){
        needClose.set(true);
        if (waitFor)
            waitFor(false);
    }

}
