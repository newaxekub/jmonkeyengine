package jme3test.post;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.post.HDRRenderer;
import com.jme3.ui.Picture;

public class TestHDR extends SimpleApplication {

    private HDRRenderer hdrRender;
    private Picture dispQuad;

    public static void main(String[] args){
        TestHDR app = new TestHDR();
        app.start();
    }

    public Geometry createHDRBox(){
        Box boxMesh = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry box = new Geometry("Box", boxMesh);

//        Material mat = assetManager.loadMaterial("Textures/HdrTest/Memorial.j3m");
//        box.setMaterial(mat);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        mat.setTexture("m_ColorMap", assetManager.loadTexture("Textures/HdrTest/Memorial.hdr"));
        box.setMaterial(mat);

        return box;
    }

//    private Material disp;
    
    @Override
    public void simpleInitApp() {
        hdrRender = new HDRRenderer(assetManager, renderer);
        hdrRender.setSamples(0);
        hdrRender.setMaxIterations(20);
        hdrRender.setExposure(0.87f);
        hdrRender.setThrottle(0.33f);

        viewPort.addProcessor(hdrRender);
        
//        config.setVisible(true);

        rootNode.attachChild(createHDRBox());
    }

    public void simpleUpdate(float tpf){
        if (hdrRender.isInitialized() && dispQuad == null){
            dispQuad = hdrRender.createDisplayQuad();
            dispQuad.setWidth(128);
            dispQuad.setHeight(128);
            dispQuad.setPosition(30, cam.getHeight() - 128 - 30);
            guiNode.attachChild(dispQuad);
        }
    }

//    public void displayAvg(Renderer r){
//        r.setFrameBuffer(null);
//        disp = prepare(-1, -1, settings.getWidth(), settings.getHeight(), 3, -1, scene64, disp);
//        r.clearBuffers(true, true, true);
//        r.renderGeometry(pic);
//    }

}
