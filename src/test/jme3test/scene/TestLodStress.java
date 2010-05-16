package jme3test.scene;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.binding.BindingListener;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.LodControl;

public class TestLodStress extends SimpleApplication implements BindingListener {

    private boolean lod = false;

    public static void main(String[] args){
        TestLodStress app = new TestLodStress();
        app.start();
    }

    public void onBinding(String binding, float value) {
        if (binding.equals("USELOD") && value >= 0f){
            lod = !lod;
        }
    }

    public void onPreUpdate(float tpf) {
    }

    public void onPostUpdate(float tpf) {
    }

    public void simpleInitApp() {
        inputManager.registerKeyBinding("USELOD", KeyInput.KEY_L);

        Node teapotNode = (Node) assetManager.loadModel("Models/Teapot/Teapot.meshxml");
        Geometry teapot = (Geometry) teapotNode.getChild(0);

        // show normals as material
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");

        for (int y = -10; y < 10; y++){
            for (int x = -10; x < 10; x++){
                Geometry clonePot = teapot.clone();
                
                clonePot.setMaterial(mat);
                clonePot.setLocalTranslation(x * .5f, 0, y * .5f);
                clonePot.setLocalScale(.15f);
                
                LodControl control = new LodControl(clonePot);
                clonePot.addControl(control);
                rootNode.attachChild(clonePot);
            }
        }

        cam.setLocation(new Vector3f(8.378951f, 5.4324f, 8.795956f));
        cam.setRotation(new Quaternion(-0.083419204f, 0.90370524f, -0.20599906f, -0.36595422f));
    }

}
