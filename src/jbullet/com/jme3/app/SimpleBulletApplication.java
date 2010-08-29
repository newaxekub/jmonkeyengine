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
package com.jme3.app;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsSpace.BroadphaseType;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.RenderState;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext.Type;
import com.jme3.system.JmeSystem;
import com.jme3.util.BufferUtils;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author normenhansen
 */
public abstract class SimpleBulletApplication extends Application implements PhysicsTickListener{

    protected ScheduledThreadPoolExecutor executor;
    private PhysicsSpace pSpace;
    protected Node rootNode = new Node("Root Node");
    protected Node guiNode = new Node("Gui Node");
    protected float secondCounter = 0.0f;
    protected BitmapText fpsText;
    protected BitmapFont guiFont;
    protected StatsView statsView;
    protected FlyByCamera flyCam;
    protected boolean showSettings = true;
    protected ThreadingType threadingType = ThreadingType.SEQUENTIAL;
    public BroadphaseType broadphaseType = BroadphaseType.DBVT;
    public Vector3f worldMin = new Vector3f(-10000f, -10000f, -10000f);
    public Vector3f worldMax = new Vector3f(10000f, 10000f, 10000f);

    private AppActionListener actionListener = new AppActionListener();

    private class AppActionListener implements ActionListener {
        public void onAction(String name, boolean value, float tpf) {
            if (!value)
                return;

            if (name.equals("SIMPLEAPP_Exit")){
                    stop();
                }else if (name.equals("SIMPLEAPP_CameraPos")){
                    if (cam != null){
                        Vector3f loc = cam.getLocation();
                        Quaternion rot = cam.getRotation();
                        System.out.println("Camera Position: ("+
                                loc.x+", "+loc.y+", "+loc.z+")");
                        System.out.println("Camera Rotation: "+rot);
                        System.out.println("Camera Direction: "+cam.getDirection());
                    }
                }else if (name.equals("SIMPLEAPP_Memory")){
                    BufferUtils.printCurrentDirectMemory(null);
                }
        }
    }

    public SimpleBulletApplication() {
        super();
    }

    @Override
    public void start() {
        // set some default settings in-case
        // settings dialog is not shown
        if (settings == null) {
            setSettings(new AppSettings(true));
        }

        // show settings dialog
        if (showSettings) {
            if (!JmeSystem.showSettingsDialog(settings)) {
                return;
            }
        }

        super.start();
    }

    private boolean startPhysicsOnExecutor() {
        if (executor != null) {
            executor.shutdown();
        }
        executor = new ScheduledThreadPoolExecutor(1);
        final SimpleBulletApplication app=this;
        Callable<Boolean> call = new Callable<Boolean>() {

            public Boolean call() throws Exception {
                detachedPhysicsLastUpdate = System.currentTimeMillis();
                pSpace = new PhysicsSpace(worldMin, worldMax, broadphaseType);
                pSpace.addTickListener(app);
                return true;
            }
        };
        try {
            return executor.submit(call).get();
        } catch (InterruptedException ex) {
            Logger.getLogger(SimpleBulletApplication.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (ExecutionException ex) {
            Logger.getLogger(SimpleBulletApplication.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    private Callable<Boolean> parallelPhysicsUpdate = new Callable<Boolean>() {

        public Boolean call() throws Exception {
            pSpace.update(timer.getTimePerFrame() * speed);
            return true;
        }
    };
    long detachedPhysicsLastUpdate = 0;
    private Callable<Boolean> detachedPhysicsUpdate = new Callable<Boolean>() {

        public Boolean call() throws Exception {
            pSpace.update(getPhysicsSpace().getAccuracy() * speed);
            long update = System.currentTimeMillis() - detachedPhysicsLastUpdate;
            detachedPhysicsLastUpdate = System.currentTimeMillis();
            executor.schedule(detachedPhysicsUpdate, Math.round(getPhysicsSpace().getAccuracy() * 1000000.0f) - (update * 1000), TimeUnit.MICROSECONDS);
            return true;
        }
    };

    public PhysicsSpace getPhysicsSpace() {
        return pSpace;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    public FlyByCamera getFlyByCamera() {
        return flyCam;
    }

    public Node getGuiNode() {
        return guiNode;
    }

    public Node getRootNode() {
        return rootNode;
    }

    public boolean isShowSettings() {
        return showSettings;
    }

    public void setShowSettings(boolean showSettings) {
        this.showSettings = showSettings;
    }

    public void loadFPSText() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        fpsText = new BitmapText(guiFont, false);
        fpsText.setSize(guiFont.getCharSet().getRenderedSize());
        fpsText.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        fpsText.setText("Frames per second");
        guiNode.attachChild(fpsText);
    }

    public void loadStatsView() {
        statsView = new StatsView("Statistics View", assetManager, renderer.getStatistics());
        // move it up so it appears above fps text
        statsView.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        guiNode.attachChild(statsView);
    }

    @Override
    public void initialize() {
        super.initialize();
        //start physics thread(pool)
        if (threadingType == ThreadingType.PARALLEL) {
            startPhysicsOnExecutor();
        } else if (threadingType == ThreadingType.DETACHED) {
            startPhysicsOnExecutor();
            executor.submit(detachedPhysicsUpdate);
        } else {
            pSpace = new PhysicsSpace(worldMin, worldMax, broadphaseType);
            pSpace.addTickListener(this);
        }

        // enable depth test and back-face culling for performance
        renderer.applyRenderState(RenderState.DEFAULT);

        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);
        loadFPSText();
        loadStatsView();
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);

        if (inputManager != null){
            flyCam = new FlyByCamera(cam);
            flyCam.setMoveSpeed(1f);
            flyCam.registerWithInput(inputManager);

            if (context.getType() == Type.Display)
                inputManager.addMapping("SIMPLEAPP_Exit", new KeyTrigger(KeyInput.KEY_ESCAPE));

            inputManager.addMapping("SIMPLEAPP_CameraPos", new KeyTrigger(KeyInput.KEY_C));
            inputManager.addMapping("SIMPLEAPP_Memory", new KeyTrigger(KeyInput.KEY_M));
            inputManager.addListener(actionListener, "SIMPLEAPP_Exit",
                                     "SIMPLEAPP_CameraPos", "SIMPLEAPP_Memory");
        }

        // call user code
        simpleInitApp();
    }

    @Override
    public void update() {
        if (speed == 0 || paused) {
            return;
        }

        super.update();
        float tpf = timer.getTimePerFrame() * speed;

        secondCounter += timer.getTimePerFrame();
        int fps = (int) timer.getFrameRate();
        if (secondCounter >= 1.0f) {
            fpsText.setText("Frames per second: " + fps);
            secondCounter = 0.0f;
        }

        simpleUpdate(tpf);
        rootNode.updateLogicalState(tpf);
        guiNode.updateLogicalState(tpf);
        rootNode.updateGeometricState();
        guiNode.updateGeometricState();

        //TODO: maybe create own thread instead of executor.
        if (threadingType == ThreadingType.PARALLEL) {
            //submit physics update
            Future physicsFuture = executor.submit(parallelPhysicsUpdate);
            //render frame concurrently
            renderManager.render(tpf);
            simpleRender(renderManager);
            try {
                physicsFuture.get();
            } catch (InterruptedException ex) {
                Logger.getLogger(SimpleBulletApplication.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(SimpleBulletApplication.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (threadingType == ThreadingType.SEQUENTIAL) {
            pSpace.update(tpf);
            renderManager.render(tpf);
            simpleRender(renderManager);
        } else {
            renderManager.render(tpf);
            simpleRender(renderManager);
        }
    }

    public abstract void simpleInitApp();

    public void physicsTick(PhysicsSpace space, float f) {
        simplePhysicsUpdate(f);
    }

    public void simplePhysicsUpdate(float tpf) {
    }

    public void simpleUpdate(float tpf) {
    }

    public void simpleRender(RenderManager rm) {
    }

    /**
     * @return the threadingType
     */
    public ThreadingType getThreadingType() {
        return threadingType;
    }

    /**
     * @param threadingType the threadingType to set
     */
    public void setThreadingType(ThreadingType threadingType) {
        this.threadingType = threadingType;
    }

    public void setBroadphaseType(BroadphaseType broadphaseType) {
        this.broadphaseType = broadphaseType;
    }

    public void setWorldMin(Vector3f worldMin) {
        this.worldMin = worldMin;
    }

    public void setWorldMax(Vector3f worldMax) {
        this.worldMax = worldMax;
    }

    public enum ThreadingType {

        SEQUENTIAL,
        PARALLEL,
        DETACHED
    }
}
