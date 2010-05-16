package jme3tools.preview;

import com.jme3.app.Application;
import com.jme3.input.binding.BindingListener;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.HDRRenderer;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.BasicShadowRenderer;

public class PreviewDisplay extends Application implements BindingListener {

    private Node rootNode = new Node("Root Node");
    private Spatial model;
    private RenderState wireFrame;

    public static final int MODE_WIREFRAME = 0x1,
                            MODE_SOLID     = 0x2,
                            MODE_TEXTURED  = 0x3,
                            MODE_MATERIAL  = 0x4;

    private boolean leftMouse, rightMouse, middleMouse;
    private float deltaX, deltaY, deltaWheel;
    private PointLight camLight;
    
    {
        wireFrame = new RenderState();
        wireFrame.setWireframe(true);
    }

    public void initialize(){
        super.initialize();
        setPauseOnLostFocus(false);

        viewPort.attachScene(rootNode);
//        HDRRenderer r = new HDRRenderer(manager, renderer);
//        r.setSamples(4);
//        r.setWhiteLevel(2);
//        r.setExposure(.60f);
//        r.setMaxIterations(20);
//        viewPort.addProcessor(r);
//        BasicShadowRenderer bsr = new BasicShadowRenderer(manager, 512);
//        bsr.setDirection(new Vector3f(0,-1, -1).normalizeLocal());
//        viewPort.addProcessor(bsr);

        Sphere s = new Sphere(30, 20, 10);
        Geometry geom = new Geometry("sphere", s);
        geom.setMaterial(assetManager.loadMaterial("Interface/Logo/Logo.j3m"));
        model = geom;
        model.getLocalRotation().fromAngles(-FastMath.HALF_PI, 0, 0);
        rootNode.attachChild(model);

        cam.setLocation(new Vector3f(0, 0, -50));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-.5f,-1, -.5f).normalizeLocal());
        dl.setColor(new ColorRGBA(1f, 1f, .9f, 1f));
        rootNode.addLight(dl);

        DirectionalLight dl2 = new DirectionalLight();
        dl.setDirection(new Vector3f(-1 ,-0.2f,1).normalizeLocal());
        dl.setColor(new ColorRGBA(0.2f, 0.2f, 0.4f, 1.0f));
        rootNode.addLight(dl2);
        
        camLight = new PointLight();
        camLight.setColor(ColorRGBA.Gray);
        rootNode.addLight(camLight);

        renderer.applyRenderState(RenderState.DEFAULT);
        inputManager.addBindingListener(this);
        inputManager.registerMouseAxisBinding("MOUSE_X+", 0, false);
        inputManager.registerMouseAxisBinding("MOUSE_X-", 0, true);
        inputManager.registerMouseAxisBinding("MOUSE_Y+", 1, false);
        inputManager.registerMouseAxisBinding("MOUSE_Y-", 1, true);
        inputManager.registerMouseAxisBinding("MOUSE_W+", 2, false);
        inputManager.registerMouseAxisBinding("MOUSE_W-", 2, true);

        inputManager.registerMouseButtonBinding("MOUSE_LEFT", 0);
        inputManager.registerMouseButtonBinding("MOUSE_RIGHT", 1);
        inputManager.registerMouseButtonBinding("MOUSE_MIDDLE", 2);
    }

    private Quaternion rot = new Quaternion();
    private Vector3f vector = new Vector3f();
    private Vector3f focus = new Vector3f();

    private void rotateCamera(Vector3f axis, float amount) {
        if (axis.equals(cam.getLeft())) {
            float elevation = -FastMath.asin(cam.getDirection().y);
            amount = Math.min(Math.max(elevation + amount,
                    -FastMath.HALF_PI), FastMath.HALF_PI)
                    - elevation;
        }
        rot.fromAngleAxis(amount, axis);
        cam.getLocation().subtract(focus, vector);
        rot.mult(vector, vector);
        focus.add(vector, cam.getLocation());

        Quaternion curRot = cam.getRotation().clone();
        cam.setRotation(rot.mult(curRot));
    }

    private void panCamera(float left, float up) {
        cam.getLeft().mult(left, vector);
        vector.scaleAdd(up, cam.getUp(), vector);
        cam.setLocation(cam.getLocation().add(vector));
        focus.addLocal(vector);
    }

    private void zoomCamera(float amount) {
        float dist = cam.getLocation().distance(focus);
        amount = dist - Math.max(0f, dist - amount);
        Vector3f loc = cam.getLocation().clone();
        loc.scaleAdd(amount, cam.getDirection(), loc);
        cam.setLocation(loc);
    }

    public void onBinding(String binding, float value){
        if (binding.equals("MOUSE_LEFT"))
            leftMouse = value > 0f;
        else if (binding.equals("MOUSE_RIGHT"))
            rightMouse = value > 0f;
        else if (binding.equals("MOUSE_MIDDLE"))
            middleMouse = value > 0f;
        else if (binding.equals("MOUSE_X+")){
            deltaX = value;
        }else if (binding.equals("MOUSE_X-")){
            deltaX = -value;
        }else if (binding.equals("MOUSE_Y+")){
            deltaY = value;
        }else if (binding.equals("MOUSE_Y-")){
            deltaY = -value;
        }else if (binding.equals("MOUSE_W+")){
            deltaWheel = value;
        }else if (binding.equals("MOUSE_W-")){
            deltaWheel = -value;
        }
    }

    public void onPreUpdate(float tpf) {
    }

    public void onPostUpdate(float tpf) {
        if (leftMouse){
            rotateCamera(Vector3f.UNIT_Y, -deltaX * 5);
            rotateCamera(cam.getLeft(), -deltaY * 5);
        }
        if (deltaWheel != 0){
            zoomCamera(deltaWheel * 10);
        }
        if (rightMouse){
            panCamera(deltaX * 10, -deltaY * 10);
        }

        leftMouse = false;
        rightMouse = false;
        middleMouse = false;
        deltaX = 0;
        deltaY = 0;
        deltaWheel = 0;
    }

    private void setMode(int mode, Spatial spatial){
        if (spatial instanceof Node){
            Node node = (Node) spatial;
            for (Spatial child : node.getChildren()){
                setMode(mode, child);
            }
        }else if (spatial instanceof Geometry){
            Geometry geom = (Geometry) spatial;
            Material mat = geom.getMaterial();
            if (mat == null)
                return;
            
            mat.getAdditionalRenderState().setWireframe(false);
            switch (mode){
                case MODE_WIREFRAME:
                    mat.getAdditionalRenderState().setWireframe(true);
                    break;
                case MODE_SOLID:
                    // disable lighting and texturing
                    break;
//                case MODE
            }
        }
    }

    public void setMode(int mode){
        setMode(mode, model);
    }

    public Spatial getModel(){
        return model;
    }

    public void setModel(Spatial model){
        rootNode.detachAllChildren();
        rootNode.attachChild(model);
        this.model = model;
    }

    @Override
    public void update() {
        if (speed == 0)
            return;

        super.update();
        float tpf = timer.getTimePerFrame();

        Vector3f temp = camLight.getPosition();
        temp.set(cam.getLeft()).multLocal(5.0f);
        temp.addLocal(cam.getLocation());
        camLight.setPosition(temp);

        rootNode.updateLogicalState(tpf);
        rootNode.updateGeometricState();

        renderManager.render(tpf);
    }
}
