package jme3test.bullet;

import com.jme3.app.SimpleBulletApplication;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.input.controls.ActionListener;
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
public class TestCollisionListener extends SimpleBulletApplication implements ActionListener, PhysicsCollisionListener {

    private Material mat;
    private static final Sphere bullet;
    private static final SphereCollisionShape bulletCollisionShape;

    static {
        bullet = new Sphere(32, 32, 0.4f, true, false);
        bullet.setTextureMode(TextureMode.Projected);
        bulletCollisionShape = new SphereCollisionShape(0.4f);
    }

    public static void main(String[] args) {
        TestCollisionListener app = new TestCollisionListener();
        app.start();
    }

    private void setupKeys() {
        inputManager.addMapping("shoot", new MouseButtonTrigger(0));
        inputManager.addListener(this, "shoot");
    }

    @Override
    public void simpleInitApp() {

        setupKeys();

        mat = new Material(getAssetManager(), "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Red);

        // Add a physics box to the world
        PhysicsNode physicsBox = new PhysicsNode(new BoxCollisionShape(new Vector3f(1, 1, 1)), 1);
        physicsBox.setName("box");
        physicsBox.setFriction(0.1f);
        physicsBox.setLocalTranslation(new Vector3f(.6f, 4, .5f));
        physicsBox.attachDebugShape(assetManager);
        physicsBox.updateGeometricState();
        rootNode.attachChild(physicsBox);
        getPhysicsSpace().add(physicsBox);

        // An obstacle mesh, does not move (mass=0)
        PhysicsNode node2 = new PhysicsNode(new MeshCollisionShape(new Sphere(16, 16, 1.2f)), 0);
        node2.setName("mesh");
        node2.setLocalTranslation(new Vector3f(2.5f, -4, 0f));
        node2.attachDebugShape(assetManager);
        //setting collision group to group 2, collide with groups is still 1!
        node2.setCollisionGroup(PhysicsNode.COLLISION_GROUP_02);
        rootNode.attachChild(node2);
        getPhysicsSpace().add(node2);

        // The floor, does not move (mass=0)
        PhysicsNode node3 = new PhysicsNode(new BoxCollisionShape(new Vector3f(100, 1, 100)), 0);
        node3.setLocalTranslation(new Vector3f(0f, -6, 0f));
        node3.attachDebugShape(assetManager);
        node3.updateGeometricState();
        rootNode.attachChild(node3);
        getPhysicsSpace().add(node3);

        // add ourselves as collision listener
        getPhysicsSpace().addCollisionListener(this);
        // add ourselves as group collision listener for group 2
        getPhysicsSpace().addCollisionGroupListener(this, PhysicsNode.COLLISION_GROUP_02);
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simplePhysicsUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    public void collision(PhysicsCollisionEvent event) {
        if ("box".equals(event.getNodeA().getName()) || "box".equals(event.getNodeB().getName())) {
            if ("bullet".equals(event.getNodeA().getName()) || "bullet".equals(event.getNodeB().getName())) {
                fpsText.setText("You hit the box!");
            }
        }
        if ("mesh".equals(event.getNodeA().getName()) || "mesh".equals(event.getNodeB().getName())) {
            if ("bullet".equals(event.getNodeA().getName()) || "bullet".equals(event.getNodeB().getName())) {
                fpsText.setText("You hit the mesh!");
            }
        }
    }

    public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB) {
        //group 2 only randomly collides
        if (Math.random() < 0.5f) {
            return true;
        } else {
            return false;
        }
    }
    
    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("shoot") && !value) {
            Geometry bulletg = new Geometry("bullet", bullet);
            bulletg.setMaterial(mat);
            PhysicsNode bulletNode = new PhysicsNode(bulletg, bulletCollisionShape, 1);
            bulletNode.setName("bullet");
            bulletNode.setLocalTranslation(cam.getLocation());
            bulletNode.updateGeometricState();
            bulletNode.setShadowMode(ShadowMode.CastAndRecieve);
            bulletNode.setLinearVelocity(cam.getDirection().mult(25));
            rootNode.attachChild(bulletNode);
            getPhysicsSpace().add(bulletNode);
        }
    }

}
