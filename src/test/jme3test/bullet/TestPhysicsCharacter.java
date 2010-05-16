package jme3test.bullet;

import com.jme3.app.SimpleBulletApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.nodes.PhysicsCharacterNode;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.input.KeyInput;
import com.jme3.input.binding.BindingListener;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;

/**
 *
 * @author normenhansen
 */
public class TestPhysicsCharacter extends SimpleBulletApplication implements BindingListener{
    private PhysicsCharacterNode physicsCharacter;
    private Vector3f walkDirection=new Vector3f();

    public static void main(String[] args){
        TestPhysicsCharacter app = new TestPhysicsCharacter();
        app.start();
    }

    private void setupKeys() {
        inputManager.registerKeyBinding("Lefts", KeyInput.KEY_H);
        inputManager.registerKeyBinding("Rights", KeyInput.KEY_K);
        inputManager.registerKeyBinding("Ups", KeyInput.KEY_U);
        inputManager.registerKeyBinding("Downs", KeyInput.KEY_J);
        inputManager.registerKeyBinding("Space", KeyInput.KEY_SPACE);
        //used with method onBinding in BindingListener interface
        //in order to add function to keys
        inputManager.addBindingListener(this);
    }

    public void onBinding(String binding, float value) {
        if(binding.equals("Lefts")){
            walkDirection.addLocal(new Vector3f(-.1f,0,0));
        }
        else if(binding.equals("Rights")){
            walkDirection.addLocal(new Vector3f(.1f,0,0));
        }
        else if(binding.equals("Ups")){
            walkDirection.addLocal(new Vector3f(0,0,-.1f));
        }
        else if(binding.equals("Downs")){
            walkDirection.addLocal(new Vector3f(0,0,.1f));
        }
        else if(binding.equals("Space")){
            physicsCharacter.jump();
        }
    }

    public void onPreUpdate(float tpf) {
        walkDirection.set(0,0,0);
    }

    public void onPostUpdate(float tpf) {
    }
    
    @Override
    public void simpleInitApp() {

        setupKeys();
        
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
        physicsCharacter=new PhysicsCharacterNode(geom,new SphereCollisionShape(1),.1f);
        physicsCharacter.setLocalTranslation(new Vector3f(3,6,0));
//        physicsSphere.updateGeometricState();
        physicsCharacter.updateModelBound();
        rootNode.attachChild(physicsCharacter);
        getPhysicsSpace().add(physicsCharacter);

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

        // an obstacle mesh, does not move (mass=0)
        Geometry geom4=new Geometry("node2",new Sphere(16,16,1.2f));
        geom4.setMaterial(mat);
        PhysicsNode node2=new PhysicsNode(geom4,new MeshCollisionShape(geom4.getMesh()),0);
        node2.setLocalTranslation(new Vector3f(2.5f,-4,0f));
        rootNode.attachChild(node2);
        getPhysicsSpace().add(node2);

        // the floor, does not move (mass=0)
        Geometry geom5=new Geometry("box2",new Box(Vector3f.ZERO,100f,1f,100f));
        geom5.setMaterial(mat);
        geom5.updateGeometricState();
        PhysicsNode node3=new PhysicsNode(geom5,new BoxCollisionShape(new Vector3f(100,1,100)),0);
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
    public void simplePhysicsUpdate(float tpf) {
        physicsCharacter.setWalkDirection(walkDirection);
   }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

}
