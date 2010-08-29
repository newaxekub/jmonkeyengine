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
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;

/**
 *
 * @author normenhansen
 */
public class TestCcd extends SimpleBulletApplication implements ActionListener{

    private Material mat;
    private Material mat2;
    private static final Sphere bullet;
    private static final SphereCollisionShape bulletCollisionShape;

    static {
        bullet = new Sphere(32, 32, 0.4f, true, false);
        bullet.setTextureMode(TextureMode.Projected);
        bulletCollisionShape = new SphereCollisionShape(0.1f);
    }

    public static void main(String[] args) {
        TestCcd app = new TestCcd();
        app.start();
    }

    private void setupKeys() {
        inputManager.addMapping("shoot", new MouseButtonTrigger(0));
        inputManager.addMapping("shoot2", new MouseButtonTrigger(1));
        inputManager.addListener(this, "shoot");
        inputManager.addListener(this, "shoot2");
    }

    @Override
    public void simpleInitApp() {

        setupKeys();

        mat = new Material(getAssetManager(), "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Green);

        mat2 = new Material(getAssetManager(), "Common/MatDefs/Misc/WireColor.j3md");
        mat2.setColor("m_Color", ColorRGBA.Red);

        // An obstacle mesh, does not move (mass=0)
        PhysicsNode node2 = new PhysicsNode(new MeshCollisionShape(new Box(Vector3f.ZERO,4,4,0.1f)), 0);
        node2.setName("mesh");
        node2.setLocalTranslation(new Vector3f(2.5f, 0, 0f));
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
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("shoot") && !value) {
            Geometry bulletg = new Geometry("bullet", bullet);
            bulletg.setMaterial(mat);
            PhysicsNode bulletNode = new PhysicsNode(bulletg, bulletCollisionShape, 1);
            bulletNode.setCcdMotionThreshold(0.1f);
            bulletNode.setName("bullet");
            bulletNode.setLocalTranslation(cam.getLocation());
            bulletNode.updateGeometricState();
            bulletNode.setShadowMode(ShadowMode.CastAndRecieve);
            bulletNode.setLinearVelocity(cam.getDirection().mult(40));
            rootNode.attachChild(bulletNode);
            getPhysicsSpace().add(bulletNode);
        }
        else if(binding.equals("shoot2") && !value) {
            Geometry bulletg = new Geometry("bullet", bullet);
            bulletg.setMaterial(mat2);
            PhysicsNode bulletNode = new PhysicsNode(bulletg, bulletCollisionShape, 1);
            bulletNode.setName("bullet");
            bulletNode.setLocalTranslation(cam.getLocation());
            bulletNode.updateGeometricState();
            bulletNode.setShadowMode(ShadowMode.CastAndRecieve);
            bulletNode.setLinearVelocity(cam.getDirection().mult(40));
            rootNode.attachChild(bulletNode);
            getPhysicsSpace().add(bulletNode);
        }
    }

}
