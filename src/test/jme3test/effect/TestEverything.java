package jme3test.effect;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.HDRRenderer;
import com.jme3.renderer.Caps;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

public class TestEverything extends SimpleApplication {

    private BasicShadowRenderer bsr;
    private HDRRenderer hdrRender;
    private Vector3f lightDir = new Vector3f(-1, -1, .5f).normalizeLocal();

    public static void main(String[] args){
        TestEverything app = new TestEverything();
        app.start();
    }

    public void setupHdr(){
        if (renderer.getCaps().contains(Caps.GLSL100)){
            hdrRender = new HDRRenderer(assetManager, renderer);
            hdrRender.setMaxIterations(40);
            hdrRender.setSamples(settings.getSamples());

            hdrRender.setWhiteLevel(3);
            hdrRender.setExposure(0.72f);
            hdrRender.setThrottle(1);

    //        setPauseOnLostFocus(false);
    //        new HDRConfig(hdrRender).setVisible(true);

            viewPort.addProcessor(hdrRender);
        }
    }

    public void setupBasicShadow(){
        if (renderer.getCaps().contains(Caps.GLSL100)){
            bsr = new BasicShadowRenderer(assetManager, 1024);
            bsr.setDirection(lightDir);
            viewPort.addProcessor(bsr);
        }
    }

    public void setupSkyBox(){
        Sphere sky = new Sphere(32, 32, 10f);
        Geometry skyGeom = new Geometry("Sky", sky);
        skyGeom.setQueueBucket(Bucket.Sky);
        skyGeom.setShadowMode(ShadowMode.Off);
        skyGeom.updateModelBound();
        skyGeom.setCullHint(CullHint.Never);

        Texture envMap;
        if (renderer.getCaps().contains(Caps.FloatTexture)){
            envMap = assetManager.loadTexture("Textures/Sky/St Peters/StPeters.hdr");
        }else{
            envMap = assetManager.loadTexture("Textures/Sky/St Peters/StPeters.jpg");
        }
           
        Material skyMat = new Material(assetManager, "Common/MatDefs/Misc/Sky.j3md");
        skyMat.setBoolean("m_SphereMap", true);
        skyMat.setTexture("m_Texture", envMap);
        skyMat.setVector3("m_NormalScale", new Vector3f(-1, 1, -1));
        skyGeom.setMaterial(skyMat);

        rootNode.attachChild(skyGeom);
    }

    public void setupLighting(){
        boolean hdr = false;
        if (hdrRender != null){
            hdr = hdrRender.isEnabled();
        }

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(lightDir);
        if (hdr){
            dl.setColor(new ColorRGBA(3, 3, 3, 1));
        }else{
            dl.setColor(new ColorRGBA(.9f, .9f, .9f, 1));
        }
        rootNode.addLight(dl);

        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(1, 0, -1).normalizeLocal());
        if (hdr){
            dl.setColor(new ColorRGBA(1, 1, 1, 1));
        }else{
            dl.setColor(new ColorRGBA(.4f, .4f, .4f, 1));
        }
        rootNode.addLight(dl);
    }

    public void setupFloor(){
        Material mat = assetManager.loadMaterial("Textures/Terrain/BrickWall/BrickWall.j3m");
        mat.getTextureParam("m_DiffuseMap").getTextureValue().setWrap(WrapMode.Repeat);
        mat.getTextureParam("m_NormalMap").getTextureValue().setWrap(WrapMode.Repeat);
        mat.getTextureParam("m_ParallaxMap").getTextureValue().setWrap(WrapMode.Repeat);
        Box floor = new Box(Vector3f.ZERO, 50, 1f, 50);
        floor.scaleTextureCoordinates(new Vector2f(5, 5));
        Geometry floorGeom = new Geometry("Floor", floor);
        floorGeom.setMaterial(mat);
        floorGeom.updateModelBound();
        floorGeom.setShadowMode(ShadowMode.Recieve);
        rootNode.attachChild(floorGeom);
    }

//    public void setupTerrain(){
//        Material mat = manager.loadMaterial("Textures/Terrain/Rock/Rock.j3m");
//        mat.getTextureParam("m_DiffuseMap").getValue().setWrap(WrapMode.Repeat);
//        mat.getTextureParam("m_NormalMap").getValue().setWrap(WrapMode.Repeat);
//        try{
//            Geomap map = GeomapLoader.fromImage(TestEverything.class.getResource("/textures/heightmap.png"));
//            Mesh m = map.createMesh(new Vector3f(0.35f, 0.0005f, 0.35f), new Vector2f(10, 10), true);
//            Logger.getLogger(TangentBinormalGenerator.class.getName()).setLevel(Level.SEVERE);
//            TangentBinormalGenerator.generate(m);
//            Geometry t = new Geometry("Terrain", m);
//            t.setLocalTranslation(85, -15, 0);
//            t.setMaterial(mat);
//            t.updateModelBound();
//            t.setShadowMode(ShadowMode.Recieve);
//            rootNode.attachChild(t);
//        }catch (IOException ex){
//            ex.printStackTrace();
//        }
//
//    }

    public void setupRobotGuy(){
        Node model = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        Material mat = assetManager.loadMaterial("Models/Oto/Oto.j3m");
        model.getChild(0).setMaterial(mat);
//        model.setAnimation("Walk");
        model.setLocalTranslation(30, 10.5f, 30);
        model.setLocalScale(2);
        model.setShadowMode(ShadowMode.CastAndRecieve);
        rootNode.attachChild(model);
    }

    public void setupSignpost(){
        Spatial signpost = assetManager.loadModel("Models/Sign Post/Sign Post.mesh.xml");
        Material mat = assetManager.loadMaterial("Models/Sign Post/Sign Post.j3m");
        signpost.setMaterial(mat);
        signpost.rotate(0, FastMath.HALF_PI, 0);
        signpost.setLocalTranslation(12, 3.5f, 30);
        signpost.setLocalScale(4);
        signpost.setShadowMode(ShadowMode.CastAndRecieve);
        rootNode.attachChild(signpost);
    }

    @Override
    public void simpleInitApp() {
        cam.setLocation(new Vector3f(-32.295086f, 54.80136f, 79.59805f));
        cam.setRotation(new Quaternion(0.074364014f, 0.92519957f, -0.24794696f, 0.27748522f));
        cam.update();

        cam.setFrustumFar(300);
        flyCam.setMoveSpeed(30);

        rootNode.setCullHint(CullHint.Never);

        setupBasicShadow();
        setupHdr();

        setupLighting();
        setupSkyBox();

//        setupTerrain();
        setupFloor();
//        setupRobotGuy();
        setupSignpost();

        
    }

}
