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

import com.jme3.animation.AnimControl;
import com.jme3.bullet.BulletAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.PhysicsRagdollControl;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.SkeletonDebugger;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.texture.Texture;

/**
 * PHYSICS RAGDOLLS ARE NOT WORKING PROPERLY YET!
 * @author normenhansen
 */
public class TestBoneRagdoll  extends SimpleApplication {

    private BulletAppState bulletAppState;

    public static void main(String[] args){
        TestBoneRagdoll app = new TestBoneRagdoll();
        app.start();
    }

    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
//        speed=0.01f;
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f, -0.7f, -1).normalizeLocal());
        dl.setColor(new ColorRGBA(1f, 1f, 1f, 1.0f));
        rootNode.addLight(dl);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        TextureKey key = new TextureKey("Interface/Logo/Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        mat.setTexture("m_ColorMap", tex);

        // the floor, does not move (mass=0)
        Geometry geom5=new Geometry("box2",new Box(Vector3f.ZERO,100f,1f,100f));
        geom5.setMaterial(mat);
        PhysicsNode node3=new PhysicsNode(geom5,new BoxCollisionShape(new Vector3f(100f,1f,100f)),0);
        node3.setLocalTranslation(new Vector3f(0f,-6,0f));
        rootNode.attachChild(node3);
        getPhysicsSpace().add(node3);

        Node model = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
//        Node model = (Node)assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
//        model.scale(0.1f);

        //debug view
        AnimControl control= model.getControl(AnimControl.class);
        SkeletonDebugger skeletonDebug = new SkeletonDebugger("skeleton", control.getSkeleton());
        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
        mat2.setColor("m_Color", ColorRGBA.Green);
        mat2.getAdditionalRenderState().setDepthTest(false);
        skeletonDebug.setMaterial(mat2);
        

        //Note: PhysicsRagdollControl is still TODO, constructor will change
        PhysicsRagdollControl ragdoll = new PhysicsRagdollControl(getPhysicsSpace());
        
        ragdoll.setPhysicsActive(true);

//        ragdoll.setSpatial(model);
        model.addControl(ragdoll);
        ragdoll.createDebugView(renderManager, assetManager);
//        model.setLocalScale(0.2f);

        speed = .2f;

//        model.setCullHint(Spatial.CullHint.Always);

        rootNode.attachChild(model);
        rootNode.attachChild(skeletonDebug);
    }

    private PhysicsSpace getPhysicsSpace(){
        return bulletAppState.getPhysicsSpace();
    }

    public Spatial createCylinder(float radius, float height){
//        Box b = new Box(Vector3f.ZERO, radius, height/2.0f,  radius);
        Cylinder b=new Cylinder(8,8,radius,height);
        Geometry geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        TextureKey key = new TextureKey("Interface/Logo/Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        mat.setTexture("m_ColorMap", tex);

        geom.setMaterial(mat);
        
        return geom;


    }

    public Node getRootNode() {
        return rootNode;
    }

}
