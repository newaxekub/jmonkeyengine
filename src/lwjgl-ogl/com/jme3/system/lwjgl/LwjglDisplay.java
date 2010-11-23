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

import com.jme3.system.JmeContext.Type;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import com.jme3.system.AppSettings;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.PixelFormat;

public class LwjglDisplay extends LwjglAbstractDisplay {

    private static final Logger logger = Logger.getLogger(LwjglDisplay.class.getName());

    private final AtomicBoolean needRestart = new AtomicBoolean(false);

    protected DisplayMode getFullscreenDisplayMode(int width, int height, int bpp, int freq){
        try {
            DisplayMode[] modes = Display.getAvailableDisplayModes();
            for (DisplayMode mode : modes){
                if (mode.getWidth() == width
                 && mode.getHeight() == height
                 && (mode.getBitsPerPixel() == bpp || (bpp==24&&mode.getBitsPerPixel()==32))
                 && mode.getFrequency() == freq){
                    return mode;
                }
            }
        } catch (LWJGLException ex) {
            logger.log(Level.SEVERE, "Failed to acquire fullscreen display mode!", ex);
        }
        return null;
    }

    protected void createContext(AppSettings settings) throws LWJGLException{
        DisplayMode displayMode = null;
        if (settings.getWidth() <= 0 || settings.getHeight() <= 0){
            displayMode = Display.getDesktopDisplayMode();
            settings.setResolution(displayMode.getWidth(), displayMode.getHeight());
        }else if (settings.isFullscreen()){
            displayMode = getFullscreenDisplayMode(settings.getWidth(), settings.getHeight(),
                                                   settings.getBitsPerPixel(), settings.getFrequency());
            if (displayMode == null)
                throw new RuntimeException("Unable to find fullscreen display mode matching settings");
        }else{
            displayMode = new DisplayMode(settings.getWidth(), settings.getHeight());
        }

        frameRate = settings.getFrameRate();
        logger.log(Level.INFO, "Selected display mode: {0}", displayMode);
        
        Display.setTitle(settings.getTitle());
        if (displayMode != null)
            Display.setDisplayMode(displayMode);

        Display.setFullscreen(settings.isFullscreen());
        Display.setVSyncEnabled(settings.isVSync());

        if (!created.get()){
            PixelFormat pf = new PixelFormat(settings.getBitsPerPixel(),
                                             0,
                                             settings.getDepthBits(),
                                             settings.getStencilBits(),
                                             settings.getSamples());

            if (settings.getBoolean("GraphicsDebug") || settings.getRenderer().equals(AppSettings.LWJGL_OPENGL3)){
                ContextAttribs attr;
                if (settings.getRenderer().equals(AppSettings.LWJGL_OPENGL3)){
                    attr = new ContextAttribs(3, 3);
                    attr = attr.withProfileCore(true).withForwardCompatible(true).withProfileCompatibility(false);
                }else{
                    attr = new ContextAttribs();
                }
                if (settings.getBoolean("GraphicsDebug")){
                    attr = attr.withDebug(true);
                }
                Display.create(pf, attr);
            }else{
                Display.create(pf);
            }
        }
    }

    public void create(boolean waitFor){
        if (created.get()){
            logger.warning("create() called when display is already created!");
            return;
        }

        new Thread(this, "LWJGL Renderer Thread").start();
        if (waitFor)
            waitFor(true);
    }

    @Override
    public void runLoop(){
        if (needRestart.getAndSet(false)){
            try{
                createContext(settings);
            }catch (LWJGLException ex){
                logger.log(Level.SEVERE, "Failed to set display settings!", ex);
            }
            listener.reshape(settings.getWidth(), settings.getHeight());
            logger.info("Display restarted.");
        }

        super.runLoop();
    }

    @Override
    public void restart() {
        if (created.get()){
            needRestart.set(true);
        }else{
            logger.warning("Display is not created, cannot restart window.");
        }
    }

    public Type getType() {
        return Type.Display;
    }

    public void setTitle(String title){
        if (created.get())
            Display.setTitle(title);
    }

}
