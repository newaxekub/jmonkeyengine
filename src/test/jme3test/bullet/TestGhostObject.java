package jme3test.bullet;

import com.jme3.app.Application;
import com.jme3.app.SimpleBulletApplication;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.nodes.PhysicsGhostNode;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.shape.Box;
import java.util.logging.Logger;

/**
 *
 * @author tim8dev [at] gmail [dot com]
 */
public class TestGhostObject extends SimpleBulletApplication {

    private PhysicsGhostNode ghostNode;

    public static void main(String[] args) {
        Application app = new TestGhostObject();
        app.start();
    }

    private void initGhostObject() {
        Vector3f halfExtents = new Vector3f(3, 4.2f, 1);
        Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Red);
        ghostNode = new PhysicsGhostNode(new BoxCollisionShape(halfExtents));
        ghostNode.attachDebugShape(mat);
        rootNode.attachChild(ghostNode);
        getPhysicsSpace().add(ghostNode);
    }

    @Override
    public void simpleInitApp() {

        // Mesh to be shared across several boxes.
        Box boxGeom = new Box(Vector3f.ZERO, 1f, 1f, 1f);
        // CollisionShape to be shared across several boxes.
        CollisionShape shape = new BoxCollisionShape(new Vector3f(1, 1, 1));

        // Add some phyisics boxes higher up, to fall down and be tracked.
        PhysicsNode physicsBox = new PhysicsNode(new BoxCollisionShape(new Vector3f(1, 1, 1)), 1);
        physicsBox.setName("box0");
        physicsBox.setFriction(0.1f);
        physicsBox.setLocalTranslation(new Vector3f(.6f, 4, .5f));
        physicsBox.attachDebugShape(assetManager);
        physicsBox.updateGeometricState();
        rootNode.attachChild(physicsBox);
        getPhysicsSpace().add(physicsBox);

        PhysicsNode physicsBox1 = new PhysicsNode(shape, 1);
        physicsBox1.setName("box1");
        physicsBox1.setFriction(0.1f);
        physicsBox1.setLocalTranslation(new Vector3f(0, 40, 0));
        physicsBox1.attachDebugShape(assetManager);
        physicsBox1.updateGeometricState();
        rootNode.attachChild(physicsBox1);
        getPhysicsSpace().add(physicsBox1);

        PhysicsNode physicsBox2 = new PhysicsNode(new BoxCollisionShape(new Vector3f(1, 1, 1)), 1);
        physicsBox2.setName("box0");
        physicsBox2.setFriction(0.1f);
        physicsBox2.setLocalTranslation(new Vector3f(.5f, 80, -.8f));
        physicsBox2.attachDebugShape(assetManager);
        physicsBox2.updateGeometricState();
        rootNode.attachChild(physicsBox2);
        getPhysicsSpace().add(physicsBox2);

        // the floor, does not move (mass=0)
        PhysicsNode node = new PhysicsNode(new BoxCollisionShape(new Vector3f(100, 1, 100)), 0);
        node.setName("floor");
        node.setLocalTranslation(new Vector3f(0f, -6, 0f));
        node.attachDebugShape(assetManager);
        rootNode.attachChild(node);
        node.updateGeometricState();
        getPhysicsSpace().add(node);

        initGhostObject();
    }

    @Override
    public void simpleUpdate(float tpf) {
        fpsText.setText("Overlapping objects: " + ghostNode.getOverlappingObjects().toString());
    }
}
