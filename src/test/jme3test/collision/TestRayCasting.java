package jme3test.collision;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.bounding.BoundingSphere;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer.Type;

public class TestRayCasting extends SimpleApplication {

    private RayTrace tracer;
    private Spatial teapot;

    public static void main(String[] args){
        TestRayCasting app = new TestRayCasting();
        app.setPauseOnLostFocus(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
//        flyCam.setEnabled(false);

        // load material
        Material mat = (Material) assetManager.loadAsset(new AssetKey("Interface/Logo/Logo.j3m"));

        Mesh q = new Mesh();
        q.setBuffer(Type.Position, 3, new float[]
        {
            1, 0, 0,
            0, 1.5f, 0,
            -1, 0, 0
        }
        );
        q.setBuffer(Type.Index, 3, new int[]{ 0, 1, 2 });
        q.setBound(new BoundingSphere());
        q.updateBound();
//        Geometry teapot = new Geometry("MyGeom", q);

        teapot = assetManager.loadModel("Models/Teapot/Teapot.mesh.xml");
//        teapot.scale(2f, 2f, 2f);
//        teapot.move(2f, 2f, -.5f);
        teapot.rotate(FastMath.HALF_PI, FastMath.HALF_PI, FastMath.HALF_PI);
        teapot.setMaterial(mat);
        rootNode.attachChild(teapot);
        rootNode.updateGeometricState();

//        cam.setLocation(cam.getLocation().add(0,1,0));
//        cam.lookAt(teapot.getWorldBound().getCenter(), Vector3f.UNIT_Y);

        tracer = new RayTrace(rootNode, cam, 160, 128);
        tracer.show();
        tracer.update();
    }

    @Override
    public void simpleUpdate(float tpf){
        teapot.rotate(0,tpf,0);
        rootNode.updateGeometricState();

        tracer.update();
    }

}