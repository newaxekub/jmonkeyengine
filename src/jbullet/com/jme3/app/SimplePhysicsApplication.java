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

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.BindingAdapter;
import com.jme3.input.FlyByCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.binding.BindingListener;
import com.jme3.material.RenderState;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext.Type;
import com.jme3.system.JmeSystem;
import com.jme3.util.BufferUtils;
import java.net.URL;

@Deprecated
public abstract class SimplePhysicsApplication extends PhysicsApplication {

    protected Node rootNode = new Node("Root Node");
    protected Node guiNode = new Node("Gui Node");

    protected float secondCounter = 0.0f;
    protected BitmapText fpsText;

    protected FlyByCamera flyCam;
    protected boolean showSettings = true;

    public SimplePhysicsApplication(){
        super();
        
        // set some default settings in-case
        // settings dialog is not shown
        setSettings(new AppSettings(true));
    }

    @Override
    public void start(){
        // show settings dialog
        if (showSettings){
            if (!JmeSystem.showSettingsDialog(settings))
                return;
        }

        super.start();
    }

    public boolean isShowSettings() {
        return showSettings;
    }

    public void setShowSettings(boolean showSettings) {
        this.showSettings = showSettings;
    }

    public void loadFPSText(){
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        fpsText = new BitmapText(font, false);
        fpsText.setSize(font.getCharSet().getRenderedSize());
        fpsText.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        fpsText.setText("Frames per second");
        guiNode.attachChild(fpsText);
    }

    @Override
    public void initialize(){
        super.initialize();

        // enable depth test and back-face culling for performance
        renderer.applyRenderState(RenderState.DEFAULT);

        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);
        loadFPSText();
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);

        if (inputManager != null){
            flyCam = new FlyByCamera(cam);
            flyCam.setMoveSpeed(1f);
            flyCam.registerWithInput(inputManager);

            if (context.getType() == Type.Display){
                inputManager.registerKeyBinding("SIMPLEAPP_Exit", KeyInput.KEY_ESCAPE);
            }

            inputManager.registerKeyBinding("SIMPLEAPP_CameraPos", KeyInput.KEY_C);
            inputManager.registerKeyBinding("SIMPLEAPP_Memory",    KeyInput.KEY_M);
            inputManager.addBindingListener(new BindingAdapter() {
                @Override
                public void onBinding(String binding, float value) {
                    if (binding.equals("SIMPLEAPP_Exit")){
                        stop();
                    }else if (binding.equals("SIMPLEAPP_CameraPos")){
                        if (cam != null){
                            Vector3f loc = cam.getLocation();
                            Quaternion rot = cam.getRotation();
                            System.out.println("Camera Position: ("+
                                    loc.x+", "+loc.y+", "+loc.z+")");
                            System.out.println("Camera Position: "+rot);
                        }
                    }else if (binding.equals("SIMPLEAPP_Memory")){
                        BufferUtils.printCurrentDirectMemory(null);
                    }
                }
            });
        }
        //start physics
        startPhysics();
        // call user code
        simpleInitApp();
    }

    @Override
    public void update() {
        if (speed == 0 || paused)
            return;
        
        super.update();
        float tpf = timer.getTimePerFrame();

        secondCounter += tpf;
        int fps = (int) timer.getFrameRate();
        if (secondCounter >= 1.0f){
            fpsText.setText("Frames per second: "+fps);
            secondCounter = 0.0f;
        }
        
        simpleUpdate(tpf);
        rootNode.updateLogicalState(tpf);
        guiNode.updateLogicalState(tpf);
        rootNode.updateGeometricState();
        guiNode.updateGeometricState();

        renderManager.render(tpf);
        simpleRender(renderManager);
    }

    @Override
    public void physicsUpdate(float tpf) {
        super.physicsUpdate(tpf);
        simplePhysicsUpdate(tpf);
    }

    public abstract void simpleInitApp();

    public void simpleUpdate(float tpf){
    }

    public void simplePhysicsUpdate(float tpf){
    }

    public void simpleRender(RenderManager rm){
    }

}
