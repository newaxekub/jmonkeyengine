package jme3test.model.shape;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;

public class TestSphere extends SimpleApplication  {

    public static void main(String[] args){
        TestSphere app = new TestSphere();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Sphere sphMesh = new Sphere(14, 14, 1);
        Material solidColor = (Material) assetManager.loadAsset("Common/Materials/RedColor.j3m");

        for (int y = -5; y < 5; y++){
            for (int x = -5; x < 5; x++){
                Geometry sphere = new Geometry("sphere", sphMesh);
                sphere.updateModelBound();

                sphere.setMaterial(solidColor);
                sphere.setLocalTranslation(x * 2, 0, y * 2);
                rootNode.attachChild(sphere);
            }
        }
        cam.setLocation(new Vector3f(0, 5, 0));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

}
