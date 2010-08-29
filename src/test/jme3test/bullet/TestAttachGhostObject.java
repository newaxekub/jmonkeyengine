package jme3test.bullet;

import com.jme3.app.SimpleBulletApplication;
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
public class TestAttachGhostObject extends SimpleBulletApplication implements AnalogListener {

    private PhysicsHingeJoint joint;
    private PhysicsGhostNode gNode;
    private PhysicsNode collisionNode;

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
        setupKeys();
        setupJoint();
    }

    public void setupJoint() {

        Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Gray);

        PhysicsNode holderNode = new PhysicsNode(new BoxCollisionShape(new Vector3f(.1f, .1f, .1f)), 0);
        holderNode.setLocalTranslation(new Vector3f(0f, 0, 0f));
        holderNode.attachDebugShape(mat);
        holderNode.updateGeometricState();
        rootNode.attachChild(holderNode);
        getPhysicsSpace().add(holderNode);

        //movable
        PhysicsNode hammerNode = new PhysicsNode(new BoxCollisionShape(new Vector3f(.3f, .3f, .3f)), 1);
        hammerNode.setLocalTranslation(new Vector3f(0f, -1, 0f));
        hammerNode.attachDebugShape(assetManager);
        hammerNode.updateGeometricState();
        rootNode.attachChild(hammerNode);
        getPhysicsSpace().add(hammerNode);

        //immovable
        collisionNode = new PhysicsNode(new BoxCollisionShape(new Vector3f(.3f, .3f, .3f)), 0);
        collisionNode.setLocalTranslation(new Vector3f(1.8f, 0, 0f));
        collisionNode.attachDebugShape(assetManager);
        collisionNode.updateGeometricState();
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
