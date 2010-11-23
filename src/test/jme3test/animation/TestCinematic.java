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
package jme3test.animation;

import com.jme3.cinematic.Cinematic;
import com.jme3.cinematic.GuiTrack;
import com.jme3.animation.LoopMode;
import com.jme3.cinematic.MotionTrack;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.cinematic.SoundTrack;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioNode;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.shape.Box;
import de.lessvoid.nifty.Nifty;
import org.lwjgl.opengl.APPLEAuxDepthStencil;

public class TestCinematic extends SimpleApplication {

    private Spatial teapot;
    private MotionPath path;
    private MotionTrack cameraMotionTrack;
    
    private CameraNode camNode;

    public static void main(String[] args) {
        TestCinematic app = new TestCinematic();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        createScene();

        Cinematic cinematic = new Cinematic();
        cinematic.setInitalDuration(20);
        stateManager.attach(cinematic);

        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager,
                inputManager,
                audioRenderer,
                guiViewPort);
        Nifty nifty = niftyDisplay.getNifty();

        nifty.fromXmlWithoutStartScreen(/*"tutorial/tutorial.xml"*/"jme3test/niftygui/hellojme.xml");


        // attach the nifty display to the gui view port as a processor
        guiViewPort.addProcessor(niftyDisplay);

        createCameraMotion();

        cinematic.addCinematicEvent(1, cameraMotionTrack);
        cinematic.addCinematicEvent(0, new SoundTrack(new AudioNode(assetManager, "Sound/Environment/Nature.ogg"), audioRenderer));
        cinematic.addCinematicEvent(3, new SoundTrack(new AudioNode(assetManager, "Sound/Effects/kick.wav"), audioRenderer));
        SoundTrack beep = new SoundTrack(new AudioNode(assetManager, "Sound/Effects/Beep.ogg"), audioRenderer);
        beep.setInitalDuration(1);
        cinematic.addCinematicEvent(5.0f, beep);
        cinematic.addCinematicEvent(3, new GuiTrack(nifty, "start", 3));


        flyCam.setEnabled(false);

        cinematic.play();
    }

    private void createCameraMotion() {
        camNode = new CameraNode(cam);
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        camNode.setName("Motion cam");
        camNode.setLocalTranslation(new Vector3f(43.301273f, 25.0f, 0.0f));
        camNode.lookAt(teapot.getWorldTranslation(), Vector3f.UNIT_Y);
        path = new MotionPath();
        path.setCycle(true);
        path.addWayPoint(new Vector3f(20, 3, 0));
        path.addWayPoint(new Vector3f(0, 3, 20));
        path.addWayPoint(new Vector3f(-20, 3, 0));
        path.addWayPoint(new Vector3f(0, 3, -20));
        path.setCurveTension(0.83f);
        cameraMotionTrack = new MotionTrack(camNode, path);
        cameraMotionTrack.setLoopMode(LoopMode.Loop);
        cameraMotionTrack.setLookAt(teapot.getWorldTranslation(), Vector3f.UNIT_Y);
        cameraMotionTrack.setDirectionType(MotionTrack.Direction.LookAt);
        rootNode.attachChild(camNode);
    }

    private void createScene() {
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setFloat("m_Shininess", 1f);
        mat.setBoolean("m_UseMaterialColors", true);
        mat.setColor("m_Ambient", ColorRGBA.Black);
        mat.setColor("m_Diffuse", ColorRGBA.DarkGray);
        mat.setColor("m_Specular", ColorRGBA.White.mult(0.6f));
        Material matSoil = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matSoil.setBoolean("m_UseMaterialColors", true);
        matSoil.setColor("m_Ambient", ColorRGBA.Gray);
        matSoil.setColor("m_Diffuse", ColorRGBA.Gray);
        matSoil.setColor("m_Specular", ColorRGBA.Black);
        teapot = assetManager.loadModel("Models/Teapot/Teapot.obj");
        teapot.setLocalScale(3);
        teapot.setMaterial(mat);
        rootNode.attachChild(teapot);
        Geometry soil = new Geometry("soil", new Box(new Vector3f(0, -1.0f, 0), 50, 1, 50));
        soil.setMaterial(matSoil);
        rootNode.attachChild(soil);
        DirectionalLight light = new DirectionalLight();
        light.setDirection(new Vector3f(0, -1, 0).normalizeLocal());
        light.setColor(ColorRGBA.White.mult(1.5f));
        rootNode.addLight(light);
    }
}
