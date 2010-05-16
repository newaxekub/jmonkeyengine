package jme3test.conversion;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import jme3tools.converters.model.ModelConverter;

public class TestTriangleStrip extends SimpleApplication {


    public static void main(String[] args){
        TestTriangleStrip app = new TestTriangleStrip();
        app.start();
    }

    public void simpleInitApp() {
        Geometry teaGeom = (Geometry) assetManager.loadModel("Models/Teapot/Teapot.obj");
        Mesh teaMesh = teaGeom.getMesh();
        ModelConverter.generateStrips(teaMesh, true, false, 24, 0);

        // show normals as material
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");

        for (int y = -10; y < 10; y++){
            for (int x = -10; x < 10; x++){
                Geometry teaClone = new Geometry("teapot", teaMesh);
                teaClone.setMaterial(mat);

                teaClone.setLocalTranslation(x * .5f, 0, y * .5f);
                teaClone.setLocalScale(.5f);

                rootNode.attachChild(teaClone);
            }
        }

        cam.setLocation(new Vector3f(8.378951f, 5.4324f, 8.795956f));
        cam.setRotation(new Quaternion(-0.083419204f, 0.90370524f, -0.20599906f, -0.36595422f));
    }

}
