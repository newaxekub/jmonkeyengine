package jme3test.scene;

import com.jme3.app.SimpleBulletApplication;
import com.jme3.asset.plugins.HttpZipLocator;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.nodes.PhysicsCharacterNode;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.FirstPersonCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.binding.BindingListener;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.ogre.OgreMaterialList;
import com.jme3.scene.plugins.ogre.OgreMeshKey;
import com.jme3.scene.shape.Sphere;
import java.io.File;

public class TestQ3 extends SimpleBulletApplication implements BindingListener {

    private Sphere sphereMesh = new Sphere(32, 32, 10f, false, true);
    private Geometry sphere = new Geometry("Sky", sphereMesh);
    private Spatial gameLevel;
    private PhysicsCharacterNode player;
    private Vector3f walkDirection = new Vector3f();
    private static boolean useHttp = false;

    public static void main(String[] args) {
        File file = new File("quake3level.zip");
        if (!file.exists()) {
            useHttp = true;
        }
        TestQ3 app = new TestQ3();
        app.start();
    }

    public void simpleInitApp() {
        inputManager.removeBindingListener(flyCam);
        FirstPersonCamera fps = new FirstPersonCamera(cam, new Vector3f(0, -10, 0));
        fps.registerWithDispatcher(inputManager);
        fps.setMoveSpeed(100);
        setupKeys();

        this.cam.setFrustumFar(2000);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White.clone().multLocal(2));
        dl.setDirection(new Vector3f(-1, -1, -1).normalize());
        rootNode.addLight(dl);

        dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(1, -1, 1).normalize());
        rootNode.addLight(dl);

        // load the level from zip or http zip
        if (useHttp) {
            assetManager.registerLocator("http://jmonkeyengine.googlecode.com/files/quake3level.zip", HttpZipLocator.class.getName());
        } else {
            assetManager.registerLocator("quake3level.zip", ZipLocator.class.getName());
        }

        // create the geometry and attach it
        OgreMaterialList matList = (OgreMaterialList) assetManager.loadAsset("Scene.material");
        OgreMeshKey key = new OgreMeshKey("main.meshxml", matList);
        gameLevel = (Spatial) assetManager.loadAsset(key);
        gameLevel.setLocalScale(0.1f);

        CompoundCollisionShape levelShape = CollisionShapeFactory.createMeshCompoundShape((Node) gameLevel);

        PhysicsNode levelNode = new PhysicsNode(gameLevel, levelShape, 0);
        player = new PhysicsCharacterNode(new SphereCollisionShape(5), .01f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);
        player.setGravity(30);

        player.setLocalTranslation(new Vector3f(60, 10, -60));
        player.updateGeometricState();

        rootNode.attachChild(levelNode);
        rootNode.attachChild(player);
        rootNode.updateGeometricState();

        getPhysicsSpace().add(levelNode);
        getPhysicsSpace().add(player);
    }

    @Override
    public void simpleUpdate(float tpf) {
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getLocalTranslation());
    }

    private void setupKeys() {
        inputManager.registerKeyBinding("Lefts", KeyInput.KEY_A);
        inputManager.registerKeyBinding("Rights", KeyInput.KEY_D);
        inputManager.registerKeyBinding("Ups", KeyInput.KEY_W);
        inputManager.registerKeyBinding("Downs", KeyInput.KEY_S);
        inputManager.registerKeyBinding("Space", KeyInput.KEY_SPACE);
        //used with method onBinding in BindingListener interface
        //in order to add function to keys
        inputManager.addBindingListener(this);
    }

    public void onBinding(String binding, float value) {
        Vector3f camDir = cam.getDirection().clone().multLocal(0.6f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);

        if (binding.equals("Lefts")) {
            walkDirection.addLocal(camLeft);
        } else if (binding.equals("Rights")) {
            camLeft.negateLocal();
            walkDirection.addLocal(camLeft);

        } else if (binding.equals("Ups")) {
            walkDirection.addLocal(camDir);
        } else if (binding.equals("Downs")) {
            camDir.negateLocal();
            walkDirection.addLocal(camDir);
        } else if (binding.equals("Space")) {
            if (player.onGround()) {
                player.jump();
            }
        }
    }

    public void onPreUpdate(float tpf) {
        //TODO! the walkdirection of bullet characters is actually
        //constant w/o (re)setting the direction vector each frame,
        //better use keypress/release listener!
        walkDirection.set(0, 0, 0);
    }

    public void onPostUpdate(float tpf) {
    }
}
