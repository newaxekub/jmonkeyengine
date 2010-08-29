package jme3test.light;

import com.jme3.app.SimpleApplication;
import com.jme3.scene.Node;

public class TestManyLights extends SimpleApplication {

    public static void main(String[] args){
        TestManyLights app = new TestManyLights();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(10);

        Node scene = (Node) assetManager.loadModel("Scenes/ManyLights/Main.scene");
        rootNode.attachChild(scene);
//        guiNode.setCullHint(CullHint.Always);
    }

}