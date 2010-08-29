package jme3test.bullet;

import com.jme3.app.SimpleBulletApplication;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.nodes.PhysicsCharacterNode;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;

/**
 *
 * @author normenhansen
 */
public class TestPhysicsCharacter extends SimpleBulletApplication implements ActionListener {

    private PhysicsCharacterNode physicsCharacter;
    private Vector3f walkDirection = new Vector3f();
    private Material mat;
    private static final Sphere bullet;
    private static final SphereCollisionShape bulletCollisionShape;

    static {
        bullet = new Sphere(32, 32, 0.4f, true, false);
        bullet.setTextureMode(TextureMode.Projected);
        bulletCollisionShape=new SphereCollisionShape(0.4f);
    }

    public static void main(String[] args) {
        TestPhysicsCharacter app = new TestPhysicsCharacter();
        app.start();
    }

    private void setupKeys() {
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("shoot", new MouseButtonTrigger(0));
        inputManager.addListener(this, "shoot");
        inputManager.addListener(this, "Lefts");
        inputManager.addListener(this, "Rights");
        inputManager.addListener(this, "Ups");
        inputManager.addListener(this, "Downs");
        inputManager.addListener(this, "Space");
    }

    @Override
    public void simpleInitApp() {

        setupKeys();

        mat = new Material(getAssetManager(), "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Red);

        // Add a physics character to the world
        physicsCharacter = new PhysicsCharacterNode(new SphereCollisionShape(1.2f), .1f);
        physicsCharacter.setLocalTranslation(new Vector3f(3, 6, 0));
        physicsCharacter.attachDebugShape(mat);
        rootNode.attachChild(physicsCharacter);
        getPhysicsSpace().add(physicsCharacter);

        // Add a physics box to the world
        PhysicsNode physicsBox = new PhysicsNode(new BoxCollisionShape(new Vector3f(1, 1, 1)), 1);
        physicsBox.setFriction(0.1f);
        physicsBox.setLocalTranslation(new Vector3f(.6f, 4, .5f));
        physicsBox.attachDebugShape(assetManager);
        physicsBox.updateGeometricState();
        rootNode.attachChild(physicsBox);
        getPhysicsSpace().add(physicsBox);

        // An obstacle mesh, does not move (mass=0)
        PhysicsNode node2 = new PhysicsNode(new MeshCollisionShape(new Sphere(16, 16, 1.2f)), 0);
        node2.setLocalTranslation(new Vector3f(2.5f, -4, 0f));
        node2.attachDebugShape(assetManager);
        rootNode.attachChild(node2);
        getPhysicsSpace().add(node2);

        // The floor, does not move (mass=0)
        PhysicsNode node3 = new PhysicsNode(new BoxCollisionShape(new Vector3f(100, 1, 100)), 0);
        node3.setLocalTranslation(new Vector3f(0f, -6, 0f));
        node3.attachDebugShape(assetManager);
        node3.updateGeometricState();
        rootNode.attachChild(node3);
        getPhysicsSpace().add(node3);
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simplePhysicsUpdate(float tpf) {
        //note that it is not necessary to set the walkDirection each frame
        //its only done here to reflect the constant mouse movements of the user
        physicsCharacter.setWalkDirection(walkDirection);
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Lefts")) {
            if (value) {
                walkDirection.addLocal(new Vector3f(-.1f, 0, 0));
            } else {
                walkDirection.addLocal(new Vector3f(.1f, 0, 0));
            }
        } else if (binding.equals("Rights")) {
            if (value) {
                walkDirection.addLocal(new Vector3f(.1f, 0, 0));
            } else {
                walkDirection.addLocal(new Vector3f(-.1f, 0, 0));
            }
        } else if (binding.equals("Ups")) {
            if (value) {
                walkDirection.addLocal(new Vector3f(0, 0, -.1f));
            } else {
                walkDirection.addLocal(new Vector3f(0, 0, .1f));
            }
        } else if (binding.equals("Downs")) {
            if (value) {
                walkDirection.addLocal(new Vector3f(0, 0, .1f));
            } else {
                walkDirection.addLocal(new Vector3f(0, 0, -.1f));
            }
        } else if (binding.equals("Space")) {
            physicsCharacter.jump();
        } else if (binding.equals("shoot") && !value) {
            Geometry bulletg = new Geometry("bullet", bullet);
            bulletg.setMaterial(mat);
            PhysicsNode bulletNode = new PhysicsNode(bulletg, bulletCollisionShape, 1);
            bulletNode.setLocalTranslation(cam.getLocation());
            bulletNode.updateGeometricState();
            bulletNode.setShadowMode(ShadowMode.CastAndRecieve);
            bulletNode.setLinearVelocity(cam.getDirection().mult(25));
            rootNode.attachChild(bulletNode);
            getPhysicsSpace().add(bulletNode);
        }
    }
}
