/*
 * Copyright (c) 2009 Normen Hansen
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
 * * Neither the name of 'Normen Hansen' nor the names of its contributors
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


import com.jme3.app.SimpleBulletApplication;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.scene.shape.Cylinder;

/**
 * This is a basic Test of jbullet-jme functions
 *
 * @author normenhansen
 */
public class TestSimplePhysics extends SimpleBulletApplication{
//    private PhysicsSpace pSpace=PhysicsSpace.getPhysicsSpace();

    public static void main(String[] args){
        TestSimplePhysics app = new TestSimplePhysics();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        TextureKey key = new TextureKey("Interface/Logo/Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        mat.setTexture("m_ColorMap", tex);

//         Add a physics sphere to the world
        Sphere sphere=new Sphere(16,16,1f);
        Geometry geom=new Geometry("sphere",sphere);
        geom.setMaterial(mat);
        PhysicsNode physicsSphere=new PhysicsNode(geom,new SphereCollisionShape(1),1);
        physicsSphere.setLocalTranslation(new Vector3f(3,6,0));
        physicsSphere.updateGeometricState();
        physicsSphere.updateModelBound();
        rootNode.attachChild(physicsSphere);
        getPhysicsSpace().add(physicsSphere);

        // Add a physics sphere to the world using the collision shape from sphere one
        Sphere sphere2=new Sphere(16,16,1f);
        Geometry geom2=new Geometry("sphere2",sphere2);
        geom2.setMaterial(mat);
        PhysicsNode physicsSphere2=new PhysicsNode(geom2,physicsSphere.getCollisionShape(),1);
        physicsSphere2.setLocalTranslation(new Vector3f(4,8,0));
        physicsSphere2.updateGeometricState();
        physicsSphere2.updateModelBound();
        rootNode.attachChild(physicsSphere2);
        getPhysicsSpace().add(physicsSphere2);

        // Add a physics box to the world
        Box boxGeom=new Box(Vector3f.ZERO,1f,1f,1f);
        Geometry geom3=new Geometry("box",boxGeom);
        geom3.setMaterial(mat);
        PhysicsNode physicsBox=new PhysicsNode(geom3,new BoxCollisionShape(new Vector3f(1,1,1)),1);
        physicsBox.setFriction(0.1f);
        physicsBox.setLocalTranslation(new Vector3f(.6f,4,.5f));
        physicsBox.updateGeometricState();
        physicsBox.updateModelBound();
        rootNode.attachChild(physicsBox);
        getPhysicsSpace().add(physicsBox);

        Cylinder cylGeom=new Cylinder(16,16,1f,3f);
        Geometry geom6=new Geometry("box",cylGeom);
        geom6.updateModelBound();
        BoundingBox box=(BoundingBox)geom6.getModelBound();
        geom6.setMaterial(mat);
        PhysicsNode physicsCylinder=new PhysicsNode(geom6, new CylinderCollisionShape(box.getExtent(null)));
        physicsCylinder.setLocalTranslation(new Vector3f(2,2,0));
        rootNode.attachChild(physicsCylinder);
        getPhysicsSpace().add(physicsCylinder);
//
//        Capsule capGeom=new Capsule(16,16,16,0.5f,2f);
//        PhysicsNode physicsCapsule=new PhysicsNode(capGeom, CollisionShape.ShapeTypes.CAPSULE);
//        physicsCapsule.setFriction(0.1f);
//        physicsCapsule.setLocalTranslation(new Vector3f(-8,4,0));
//        rootNode.attachChild(physicsCapsule);
//        pSpace.add(physicsCapsule);
//        physicsCapsule.setMass(10f);

        // Join the physics objects with a Point2Point joint
//        PhysicsPoint2PointJoint joint=new PhysicsPoint2PointJoint(physicsSphere, physicsBox, new Vector3f(-2,0,0), new Vector3f(2,0,0));
//        PhysicsHingeJoint joint=new PhysicsHingeJoint(physicsSphere, physicsBox, new Vector3f(-2,0,0), new Vector3f(2,0,0), Vector3f.UNIT_Z,Vector3f.UNIT_Z);
//        pSpace.add(joint);

        // an obstacle mesh, does not move (mass=0)
        Geometry geom4=new Geometry("node2",new Sphere(16,16,1.2f));
        geom4.setMaterial(mat);
        PhysicsNode node2=new PhysicsNode(geom4,new MeshCollisionShape(geom4.getMesh()),0);
        node2.setLocalTranslation(new Vector3f(2.5f,-4,0f));
        rootNode.attachChild(node2);
        getPhysicsSpace().add(node2);

        // the floor, does not move (mass=0)
        Geometry geom5=new Geometry("box2",new Box(Vector3f.ZERO,100f,0.2f,100f));
        geom5.setMaterial(mat);
        geom5.updateGeometricState();
        PhysicsNode node3=new PhysicsNode(geom5,new MeshCollisionShape(geom5.getMesh()),0);
        node3.setLocalTranslation(new Vector3f(0f,-6,0f));
        rootNode.attachChild(node3);
        node3.updateModelBound();
        node3.updateGeometricState();
        getPhysicsSpace().add(node3);
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

}
