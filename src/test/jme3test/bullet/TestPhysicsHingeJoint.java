package jme3test.bullet;

import com.jme3.app.SimpleBulletApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.joints.PhysicsHingeJoint;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.input.KeyInput;
import com.jme3.input.binding.BindingListener;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

public class TestPhysicsHingeJoint extends SimpleBulletApplication implements BindingListener {
    private PhysicsHingeJoint joint;

    public static void main(String[] args) {
        TestPhysicsHingeJoint app = new TestPhysicsHingeJoint();
        app.start();
    }

    private void setupKeys() {
        inputManager.registerKeyBinding("Lefts", KeyInput.KEY_H);
        inputManager.registerKeyBinding("Rights", KeyInput.KEY_K);
        inputManager.registerKeyBinding("Space", KeyInput.KEY_SPACE);
        //used with method onBinding in BindingListener interface
        //in order to add function to keys
        inputManager.addBindingListener(this);
    }

    public void onBinding(String binding, float value) {
        if(binding.equals("Lefts")){
            joint.enableMotor(true, 1, .1f);
        }
        else if(binding.equals("Rights")){
            joint.enableMotor(true, -1, .1f);
        }
        else if(binding.equals("Space")){
            joint.enableMotor(false, 0, 0);
        }
    }

    public void onPreUpdate(float tpf) {
    }

    public void onPostUpdate(float tpf) {
    }

    @Override
    public void simpleInitApp() {
        setupKeys();
        setupFloor();
        setupJoint();
    }

    public void setupFloor() {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        TextureKey key = new TextureKey("Interface/Logo/Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        mat.setTexture("m_ColorMap", tex);
        
        Box floor = new Box(Vector3f.ZERO, 100, 1f, 100);
        Geometry floorGeom = new Geometry("Floor", floor);
        floorGeom.setMaterial(mat);
        floorGeom.updateModelBound();

        PhysicsNode tb=new PhysicsNode(floorGeom,new MeshCollisionShape(floorGeom.getMesh()),0);
        rootNode.attachChild(tb);
        tb.setLocalTranslation(new Vector3f(0f,-6,0f));
        tb.updateModelBound();
        tb.updateGeometricState();
        getPhysicsSpace().add(tb);
    }

    public void setupJoint() {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        TextureKey key = new TextureKey("Interface/Logo/Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        mat.setTexture("m_ColorMap", tex);

        Box holder = new Box(Vector3f.ZERO, .1f, .1f, .1f);
        Geometry holderGeom = new Geometry("Holder", holder);
        holderGeom.setMaterial(mat);
        holderGeom.updateModelBound();

        Box hammer = new Box(Vector3f.ZERO, .3f, .3f, .3f);
        Geometry hammerGeom = new Geometry("Hammer", hammer);
        hammerGeom.setMaterial(mat);
        hammerGeom.updateModelBound();

        PhysicsNode holderNode=new PhysicsNode(holderGeom,new BoxCollisionShape(new Vector3f( .1f, .1f, .1f)),0);
        rootNode.attachChild(holderNode);
        holderNode.setLocalTranslation(new Vector3f(0f,0,0f));
        holderNode.updateModelBound();
        holderNode.updateGeometricState();
        getPhysicsSpace().add(holderNode);

        PhysicsNode hammerNode=new PhysicsNode(hammerGeom,new BoxCollisionShape(new Vector3f( .3f, .3f, .3f)),1);
        rootNode.attachChild(hammerNode);
        hammerNode.setLocalTranslation(new Vector3f(0f,-1,0f));
        hammerNode.updateModelBound();
        hammerNode.updateGeometricState();
        getPhysicsSpace().add(hammerNode);

        joint=new PhysicsHingeJoint(holderNode, hammerNode, Vector3f.ZERO, new Vector3f(0f,-1,0f), Vector3f.UNIT_Z, Vector3f.UNIT_Z);
        getPhysicsSpace().add(joint);
    }

    @Override
    public void simpleUpdate(float tpf) {
        
    }


}