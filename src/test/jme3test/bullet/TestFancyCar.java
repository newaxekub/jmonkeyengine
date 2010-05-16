package jme3test.bullet;

import com.jme3.app.SimpleBulletApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.GImpactCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.bullet.nodes.PhysicsVehicleNode;
import com.jme3.bullet.nodes.PhysicsVehicleWheel;
import com.jme3.input.KeyInput;
import com.jme3.input.binding.BindingListener;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.LodControl;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture.WrapMode;

public class TestFancyCar extends SimpleBulletApplication implements BindingListener {
    
    private PhysicsVehicleNode player;
    private PhysicsVehicleWheel fr, fl, br, bl;
    private Node node_fr, node_fl, node_br, node_bl;
    private float wheelRadius;

    public static void main(String[] args) {
        TestFancyCar app = new TestFancyCar();
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
        if (binding.equals("Lefts")) {
            player.steer(.5f);
        } else if (binding.equals("Rights")) {
            player.steer(-.5f);
        } else if (binding.equals("Ups")) {
            player.accelerate(300f * value);
        } else if (binding.equals("Downs")) {
            player.brake(60f * value);
        }
    }

    public void onPreUpdate(float tpf) {
        player.accelerate(0);
        player.brake(0);
        player.steer(0);
        
        //XXX: hack alert: physics wheels do not rotate atm, force them
        float carSpeed = player.getLinearVelocity().length() / wheelRadius;
        node_bl.rotate(-carSpeed * tpf, 0, 0);
        node_br.rotate(-carSpeed * tpf, 0, 0);
        node_fl.rotate(-carSpeed * tpf, 0, 0);
        node_fr.rotate(-carSpeed * tpf, 0, 0);
    }

    public void onPostUpdate(float tpf) {
    }

    @Override
    public void simpleInitApp() {
        if (settings.getRenderer().startsWith("LWJGL")){
            BasicShadowRenderer bsr = new BasicShadowRenderer(assetManager, 512);
            bsr.setDirection(new Vector3f(-0.5f, -0.3f, -0.3f).normalizeLocal());
            viewPort.addProcessor(bsr);
        }
        cam.setFrustumFar(50f);

        setupKeys();
        setupFloor();
//        setupGImpact();
        buildPlayer();

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
        rootNode.addLight(dl);

        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(0.5f, -0.1f, 0.3f).normalizeLocal());
        rootNode.addLight(dl);
    }


    public void setupFloor() {
        Material mat = assetManager.loadMaterial("Textures/Terrain/BrickWall/BrickWall.j3m");
        mat.getTextureParam("m_DiffuseMap").getTextureValue().setWrap(WrapMode.Repeat);
        mat.getTextureParam("m_NormalMap").getTextureValue().setWrap(WrapMode.Repeat);
        mat.getTextureParam("m_ParallaxMap").getTextureValue().setWrap(WrapMode.Repeat);
        
        Box floor = new Box(Vector3f.ZERO, 40, 1f, 40);
        floor.scaleTextureCoordinates(new Vector2f(12.0f, 12.0f));
        Geometry floorGeom = new Geometry("Floor", floor);
        floorGeom.setShadowMode(ShadowMode.Recieve);
        floorGeom.setMaterial(mat);
        floorGeom.updateModelBound();

        PhysicsNode tb=new PhysicsNode(floorGeom,new MeshCollisionShape(floorGeom.getMesh()),0);
        rootNode.attachChild(tb);
        tb.setLocalTranslation(new Vector3f(0f,-6,0f));
        tb.updateModelBound();
        tb.updateGeometricState();
        getPhysicsSpace().add(tb);
    }

    public void setupGImpact() {
        Node gimpact = (Node) assetManager.loadModel("Models/MonkeyHead/MonkeyHead.mesh.xml");
        

        Geometry geom = (Geometry) gimpact.getChild(0);
        geom.setShadowMode(ShadowMode.CastAndRecieve);
        
//        rootNode.attachChild(geom);

        PhysicsNode tb=new PhysicsNode(geom, new GImpactCollisionShape(geom.getMesh()), 0.4f);
        tb.setLocalTranslation(new Vector3f(4,6,0f));
        rootNode.attachChild(tb);
        getPhysicsSpace().add(tb);
    }

    private Geometry findGeom(Spatial spatial, String name){
        if (spatial instanceof Node){
            Node node = (Node) spatial;
            for (int i = 0; i < node.getQuantity(); i++){
                Spatial child = node.getChild(i);
                Geometry result = findGeom(child, name);
                if (result != null)
                    return result;
            }
        }else if (spatial instanceof Geometry){
            if (spatial.getName().startsWith(name))
                return (Geometry) spatial;
        }
        return null;
    }

    private void buildPlayer() {
        float stiffness=90.0f;//200=f1 car
        float compValue=0.4f; //(lower than damp!)
        float dampValue=0.8f;
        final float mass = 1;

        Spatial car = assetManager.loadModel("Models/Ferrari/Car.scene");
        Node carNode = (Node) car;
        final Geometry chasis = findGeom(carNode, "Car");
        BoundingBox box = (BoundingBox) chasis.getModelBound();

        final Vector3f extent = box.getExtent(null);

        // put chasis in center, so that physics box matches up with it
        // also remove from parent to avoid transform issues
        chasis.removeFromParent();
        chasis.center();
        chasis.setShadowMode(ShadowMode.Cast);

        //HINT: for now, vehicles have to be created in the physics thread when multithreading!
        player = new PhysicsVehicleNode(chasis, new BoxCollisionShape(extent), mass);

        //setting default values for wheels
        player.setSuspensionCompression(compValue*2.0f*FastMath.sqrt(stiffness));
        player.setSuspensionDamping(dampValue*2.0f*FastMath.sqrt(stiffness));
        player.setSuspensionStiffness(stiffness);
        player.setFrictionSlip(.8f);

        //Create four wheels and add them at their locations
        Vector3f wheelDirection = new Vector3f(0,-1,0);
        Vector3f wheelAxle = new Vector3f(1,0,0);

        Geometry wheel_fr = findGeom(carNode, "WheelFrontRight");
        wheel_fr.removeFromParent();
        wheel_fr.center();
        node_fr = new Node("wheel_node");
        node_fr.setShadowMode(ShadowMode.Cast);
        node_fr.attachChild(wheel_fr);
        Node primaryNode = new Node("primary_wheel_node");
        primaryNode.attachChild(node_fr);
        box = (BoundingBox) wheel_fr.getModelBound();
        wheelRadius = box.getYExtent();
        float back_wheel_h = wheelRadius * 1.5f;
        float front_wheel_h = wheelRadius * 1.7f;
        player.addWheel(primaryNode, box.getCenter().add(0, -front_wheel_h, 0),
                wheelDirection, wheelAxle, 0.2f, wheelRadius, true);


        Geometry wheel_fl = findGeom(carNode, "WheelFrontLeft");
        wheel_fl.removeFromParent();
        wheel_fl.center();
        node_fl = new Node("wheel_node");
        node_fl.setShadowMode(ShadowMode.Cast);
        node_fl.attachChild(wheel_fl);
        primaryNode = new Node("primary_wheel_node");
        primaryNode.attachChild(node_fl);
        box = (BoundingBox) wheel_fl.getModelBound();
        player.addWheel(primaryNode, box.getCenter().add(0, -front_wheel_h, 0),
                        wheelDirection, wheelAxle, 0.2f, wheelRadius, true);

        Geometry wheel_br = findGeom(carNode, "WheelBackRight");
        wheel_br.removeFromParent();
        wheel_br.center();
        node_br = new Node("wheel_node");
        node_br.setShadowMode(ShadowMode.Cast);
        node_br.attachChild(wheel_br);
        primaryNode = new Node("primary_wheel_node");
        primaryNode.attachChild(node_br);
        box = (BoundingBox) wheel_br.getModelBound();
        player.addWheel(primaryNode, box.getCenter().add(0, -back_wheel_h, 0),
                        wheelDirection, wheelAxle, 0.2f, wheelRadius, false);

        Geometry wheel_bl = findGeom(carNode, "WheelBackLeft");
        wheel_bl.removeFromParent();
        wheel_bl.center();
        node_bl = new Node("wheel_node");
        node_bl.setShadowMode(ShadowMode.Cast);
        node_bl.attachChild(wheel_bl);
        primaryNode = new Node("primary_wheel_node");
        primaryNode.attachChild(node_bl);
        box = (BoundingBox) wheel_bl.getModelBound();
        player.addWheel(primaryNode, box.getCenter().add(0, -back_wheel_h, 0),
                        wheelDirection, wheelAxle, 0.2f, wheelRadius, false);

        rootNode.attachChild(player);
        getPhysicsSpace().add(player);
    }

}