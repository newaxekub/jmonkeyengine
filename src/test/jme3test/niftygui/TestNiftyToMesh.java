package jme3test.niftygui;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import de.lessvoid.nifty.Nifty;

public class TestNiftyToMesh extends SimpleApplication{

    private Nifty nifty;

    public static void main(String[] args){
        TestNiftyToMesh app = new TestNiftyToMesh();
        app.start();
    }

    public void simpleInitApp() {
       ViewPort niftyView = renderManager.createPreView("NiftyView", new Camera(1024, 768));
       niftyView.setClearEnabled(true);
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager,
                                                          inputManager,
                                                          audioRenderer,
                                                          niftyView);
        nifty = niftyDisplay.getNifty();
        nifty.fromXml("all/intro.xml", "start");
        niftyView.addProcessor(niftyDisplay);

        FrameBuffer fb = new FrameBuffer(1024, 768, 0);
        fb.setDepthBuffer(Format.Depth);
        Texture2D tex = new Texture2D(1024, 768, Format.RGB8);
        fb.setColorTexture(tex);
        niftyView.setClearEnabled(true);
        niftyView.setOutputFrameBuffer(fb);

        Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        mat.setTexture("m_ColorMap", tex);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
    }
}
