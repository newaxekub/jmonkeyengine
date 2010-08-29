/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jme3test.bullet;

import com.jme3.app.SimpleBulletApplication;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author normenhansen
 */
public class TestCollisionGroups extends SimpleBulletApplication{

    public static void main(String[] args){
        TestCollisionGroups app = new TestCollisionGroups();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Red);
        Material mat2 = new Material(getAssetManager(), "Common/MatDefs/Misc/WireColor.j3md");
        mat2.setColor("m_Color", ColorRGBA.Magenta);

        // Add a physics sphere to the world
        PhysicsNode physicsSphere=new PhysicsNode(new SphereCollisionShape(1),1);
        physicsSphere.setLocalTranslation(new Vector3f(3,6,0));
        physicsSphere.attachDebugShape(assetManager);
        physicsSphere.updateGeometricState();
        rootNode.attachChild(physicsSphere);
        getPhysicsSpace().add(physicsSphere);

        // Add a physics sphere to the world using the collision shape from sphere one
        PhysicsNode physicsSphere2=new PhysicsNode(physicsSphere.getCollisionShape(),1);
        physicsSphere2.setLocalTranslation(new Vector3f(4,8,0));
        physicsSphere2.attachDebugShape(mat2);
        physicsSphere2.updateGeometricState();
        physicsSphere2.addCollideWithGroup(PhysicsNode.COLLISION_GROUP_02);
        rootNode.attachChild(physicsSphere2);
        getPhysicsSpace().add(physicsSphere2);

        // an obstacle mesh, does not move (mass=0)
        PhysicsNode node2=new PhysicsNode(new MeshCollisionShape(new Sphere(16,16,1.2f)),0);
        node2.setLocalTranslation(new Vector3f(2.5f,-4,0f));
        node2.attachDebugShape(mat);
        node2.setCollisionGroup(PhysicsNode.COLLISION_GROUP_02);
        node2.setCollideWithGroups(PhysicsNode.COLLISION_GROUP_02);
        rootNode.attachChild(node2);
        getPhysicsSpace().add(node2);

        // the floor, does not move (mass=0)
        PhysicsNode node3=new PhysicsNode(new MeshCollisionShape(new Box(Vector3f.ZERO,100f,0.2f,100f)),0);
        node3.setLocalTranslation(new Vector3f(0f,-6,0f));
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
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
