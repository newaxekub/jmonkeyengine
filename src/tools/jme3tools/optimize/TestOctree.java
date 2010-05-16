package jme3tools.optimize;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.plugins.ogre.MeshLoader;
import com.jme3.texture.FrameBuffer;
import java.util.HashSet;
import java.util.Set;


public class TestOctree extends SimpleApplication implements SceneProcessor {

    private Octree tree;
    private Set<Geometry> renderSet = new HashSet<Geometry>();
    private Material mat, mat2;
    private WireBox box = new WireBox(1,1,1);

    public static void main(String[] args){
        TestOctree app = new TestOctree();
        app.start();
    }

    public void simpleInitApp() {
        this.flyCam.setMoveSpeed(2000);
        this.cam.setFrustumFar(10000);
        MeshLoader.AUTO_INTERLEAVE = false;

        mat = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.White);

        mat2 = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(-1, -1, -1).normalize());
        rootNode.addLight(dl);

        dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(1, -1, 1).normalize());
        rootNode.addLight(dl);

        assetManager.registerLocator("quake3level.zip", "com.jme3.asset.plugins.ZipLocator");
        Spatial scene = MeshLoader.loadModel(assetManager, "main.meshxml","Scene.material");
        scene.setMaterial(mat2);
//        scene.setLocalScale(0.2f);
//        Spatial scene = manager.loadModel("models/teapot.obj");
//        Material mat = new Material(manager, "Common/MatDefs/Misc/ShowNormals.j3md");
//        scene.setMaterial(mat);

        // generate octree
//        tree = new Octree(scene, 20000);
        tree = new Octree(scene, 50);
        tree.construct();

        viewPort.addProcessor(this);
    }

    public void initialize(RenderManager rm, ViewPort vp) {
    }

    public void reshape(ViewPort vp, int w, int h) {
    }

    public boolean isInitialized() {
        return true;
    }

    public void preFrame(float tpf) {
    }

    public void postQueue(RenderQueue rq) {
        renderSet.clear();
        tree.generateRenderSet(renderSet, cam);
//        System.out.println("Geoms: "+renderSet.size());
        int tris = 0;

        for (Geometry geom : renderSet){
            tris += geom.getTriangleCount();
//            geom.setMaterial(mat2);
            rq.addToQueue(geom, geom.getQueueBucket());
        }

        Matrix4f transform = new Matrix4f();
//        transform.setScale(0.2f, 0.2f, 0.2f);
//        System.out.println("Tris: "+tris);
        
        tree.renderBounds(rq, transform, box, mat);

//        renderManager.flushQueue(viewPort);
    }

    public void postFrame(FrameBuffer out) {
    }

    public void cleanup() {
    }
}
