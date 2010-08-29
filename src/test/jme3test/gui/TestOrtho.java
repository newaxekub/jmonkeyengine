package jme3test.gui;

import com.jme3.app.SimpleApplication;
import com.jme3.ui.Picture;

public class TestOrtho extends SimpleApplication {

    public static void main(String[] args){
        TestOrtho app = new TestOrtho();
        app.start();
    }

    public void simpleInitApp() {
        Picture p = new Picture("Picture");
        p.setPosition(0, 0);
        p.setWidth(settings.getWidth());
        p.setHeight(settings.getHeight());
        p.setImage(assetManager, "Interface/Logo/Monkey.png", false);

        // attach geometry to orthoNode
        guiNode.attachChild(p);
    }

}
