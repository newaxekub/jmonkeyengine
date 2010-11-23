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

package jme3test.bullet;

import com.jme3.bullet.BulletAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.joints.PhysicsHingeJoint;
import com.jme3.bullet.nodes.PhysicsGhostNode;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

/**
 * Tests attaching ghost nodes to physicsnodes via the scenegraph
 * @author normenhansen
 */
public class TestAttachGhostObject extends SimpleApplication implements AnalogListener {

    private PhysicsHingeJoint joint;
    private PhysicsGhostNode gNode;
    private PhysicsNode collisionNode;

    private BulletAppState bulletAppState;

    public static void main(String[] args) {
        TestAttachGhostObject app = new TestAttachGhostObject();
        app.start();
    }

    private void setupKeys() {
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "Lefts", "Rights", "Space");
    }

    public void onAnalog(String binding, float value, float tpf) {
        if (binding.equals("Lefts")) {
            joint.enableMotor(true, 1, .1f);
        } else if (binding.equals("Rights")) {
            joint.enableMotor(true, -1, .1f);
        } else if (binding.equals("Space")) {
            joint.enableMotor(false, 0, 0);
        }
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        setupKeys();
        setupJoint();
    }

    private PhysicsSpace getPhysicsSpace(){
        return bulletAppState.getPhysicsSpace();
    }

    public void setupJoint() {

        Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Gray);

        PhysicsNode holderNode = new PhysicsNode(new BoxCollisionShape(new Vector3f(.1f, .1f, .1f)), 0);
        holderNode.setLocalTranslation(new Vector3f(0f, 0, 0f));
        holderNode.attachDebugShape(mat);
        rootNode.attachChild(holderNode);
        getPhysicsSpace().add(holderNode);

        //movable
        PhysicsNode hammerNode = new PhysicsNode(new BoxCollisionShape(new Vector3f(.3f, .3f, .3f)), 1);
        hammerNode.setLocalTranslation(new Vector3f(0f, -1, 0f));
        hammerNode.attachDebugShape(assetManager);
        rootNode.attachChild(hammerNode);
        getPhysicsSpace().add(hammerNode);

        //immovable
        collisionNode = new PhysicsNode(new BoxCollisionShape(new Vector3f(.3f, .3f, .3f)), 0);
        collisionNode.setLocalTranslation(new Vector3f(1.8f, 0, 0f));
        collisionNode.attachDebugShape(assetManager);
        rootNode.attachChild(collisionNode);
        getPhysicsSpace().add(collisionNode);

        //ghost node
        gNode = new PhysicsGhostNode(new SphereCollisionShape(0.7f));
        gNode.attachDebugShape(mat);

        //"trick": ghostNode is simply attached to the movable node
        //and is updated via the scenegraph - no "real" physics connection
        hammerNode.attachChild(gNode);
        getPhysicsSpace().add(gNode);

        joint = new PhysicsHingeJoint(holderNode, hammerNode, Vector3f.ZERO, new Vector3f(0f, -1, 0f), Vector3f.UNIT_Z, Vector3f.UNIT_Z);
        getPhysicsSpace().add(joint);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (gNode.getOverlappingObjects().contains(collisionNode)) {
            fpsText.setText("collide");
        }
    }
}
