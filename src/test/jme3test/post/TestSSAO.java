package jme3test.post;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.ssao.SSAOConfig;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireFrustum;
import com.jme3.scene.shape.Box;

public class TestSSAO extends SimpleApplication {

    float angle;
    Spatial lightMdl;
    Spatial teapot;
    Geometry frustumMdl;
    WireFrustum frustum;

    private FilterPostProcessor fpp;
    private Vector3f[] points;

    {
        points = new Vector3f[8];
        for (int i = 0; i < points.length; i++) points[i] = new Vector3f();
    }

    public static void main(String[] args){
        TestSSAO app = new TestSSAO();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // put the camera in a bad position
        cam.setLocation(new Vector3f(-2.336393f, 11.91392f, -7.139601f));
        cam.setRotation(new Quaternion(0.23602544f, 0.11321983f, -0.027698677f, 0.96473104f));
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


        FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
        SSAOFilter ssaoFilter= new SSAOFilter(viewPort, new SSAOConfig(0.36f,1.8f,0.84f,0.16f,false,true));
        fpp.addFilter(ssaoFilter);
        SSAOUI ui=new SSAOUI(inputManager, ssaoFilter.getConfig());

        viewPort.addProcessor(fpp);

    }

    @Override
    public void simpleUpdate(float tpf){

    }

}
