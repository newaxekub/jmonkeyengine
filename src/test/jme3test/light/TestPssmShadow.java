package jme3test.light;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireFrustum;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.shadow.ShadowUtil;

public class TestPssmShadow extends SimpleApplication {

    float angle;
    Spatial lightMdl;
    Spatial teapot;
    Geometry frustumMdl;
    WireFrustum frustum;

    private PssmShadowRenderer pssmRenderer;
    private Vector3f[] points;

    {
        points = new Vector3f[8];
        for (int i = 0; i < points.length; i++) points[i] = new Vector3f();
    }

    public static void main(String[] args){
        TestPssmShadow app = new TestPssmShadow();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // put the camera in a bad position
        cam.setLocation(new Vector3f(-9.919669f, 29.182112f, -31.980177f));
        cam.setRotation(new Quaternion(0.14896771f, 0.22304894f, -0.034511957f, 0.96273917f));
        //cam.setFrustumFar(1000);

        Material mat = assetManager.loadMaterial("Common/Materials/WhiteColor.j3m");
        Material matSoil = new Material(assetManager,"Common/MatDefs/Misc/SolidColor.j3md");
        matSoil.setColor("m_Color", ColorRGBA.LightGray);

       
        teapot = assetManager.loadModel("Models/Teapot/Teapot.obj");
        teapot.setLocalTranslation(0,0,10);
        
        teapot.setMaterial(mat);
        teapot.setShadowMode(ShadowMode.CastAndRecieve);
        rootNode.attachChild(teapot);

         for (int i = 0; i < 30; i++) {
            Spatial t=teapot.deepClone();
            rootNode.attachChild(t);
            teapot.setLocalTranslation((float)Math.random()*3,(float)Math.random()*3,(i+2));

        }



        Geometry soil=new Geometry("soil", new Box(new Vector3f(0, -13, 550), 800, 10, 700));
        soil.updateGeometricState();
        soil.updateModelBound();
        soil.setMaterial(matSoil);
        soil.setShadowMode(ShadowMode.CastAndRecieve);
        rootNode.attachChild(soil);

        for (int i = 0; i < 30; i++) {
            Spatial t=teapot.deepClone();
            t.setLocalScale(10.0f);
            rootNode.attachChild(t);
            teapot.setLocalTranslation((float)Math.random()*300,(float)Math.random()*30,30*(i+2));
        }


        pssmRenderer = new PssmShadowRenderer(assetManager, 1024,4);
        pssmRenderer.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        viewPort.addProcessor(pssmRenderer);
     
    }

    @Override
    public void simpleUpdate(float tpf){
     
    }

}
