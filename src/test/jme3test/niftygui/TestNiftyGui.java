package jme3test.niftygui;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import de.lessvoid.nifty.Nifty;

public class TestNiftyGui extends SimpleApplication {

    private Nifty nifty;

    public static void main(String[] args){
        TestNiftyGui app = new TestNiftyGui();
        app.start();
    }

    public void simpleInitApp() {
        Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        mat.setTexture("m_ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));
        geom.setMaterial(mat);
        rootNode.attachChild(geom);

        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager,
                                                          inputManager,
                                                          audioRenderer,
                                                          guiViewPort);
        nifty = niftyDisplay.getNifty();

//        nifty.fromXml("all/intro.xml", "start");
        nifty.fromXml("jme3test/niftygui/hellojme.xml", "start");

        // attach the nifty display to the gui view port as a processor
        guiViewPort.addProcessor(niftyDisplay);

        // disable the fly cam
//        flyCam.setEnabled(false);
        flyCam.setDragToRotate(true);
    }

}
