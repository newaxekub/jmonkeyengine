package jme3test.bullet;

import com.jme3.animation.AnimControl;
import com.jme3.app.SimpleBulletApplication;
import com.jme3.asset.TextureKey;
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
public class TestPhysicsRagdoll  extends SimpleBulletApplication {

    public static void main(String[] args){
        TestPhysicsRagdoll app = new TestPhysicsRagdoll();
        app.start();
    }

    public void simpleInitApp() {

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

        Node model = (Node) assetManager.loadModel("Models/Oto/Oto.meshxml");
//        Node model = (Node)MeshLoader.loadModel(manager, "ninja.meshxml", "ninja.material");

        //debug view
        AnimControl control= model.getControl(AnimControl.class);
        SkeletonDebugger skeletonDebug = new SkeletonDebugger("skeleton", control.getSkeleton());
        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
        mat2.setColor("m_Color", ColorRGBA.Green);
        mat2.getAdditionalRenderState().setDepthTest(false);
        skeletonDebug.setMaterial(mat2);
        

        //Note: PhysicsRagdollControl is still TODO, constructor will change
        PhysicsRagdollControl ragdoll = new PhysicsRagdollControl(getPhysicsSpace());
        ragdoll.setSpatial(model);
        model.addControl(ragdoll);
//        model.setLocalScale(0.2f);

        speed = 0.4f;

//        model.setCullHint(Spatial.CullHint.Always);

        rootNode.attachChild(model);
        rootNode.attachChild(skeletonDebug);
    }

    public Spatial createCylinder(float radius, float height){
//        Box b = new Box(Vector3f.ZERO, radius, height/2.0f,  radius);
        Cylinder b=new Cylinder(8,8,radius,height);
        Geometry geom = new Geometry("Box", b);
        geom.updateModelBound();

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
