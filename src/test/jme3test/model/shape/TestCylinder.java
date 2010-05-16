package jme3test.model.shape;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Cylinder;
import com.jme3.texture.Texture;

public class TestCylinder extends SimpleApplication {

    public static void main(String[] args){
        TestCylinder app = new TestCylinder();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Cylinder t = new Cylinder(20, 50, 1, 2, true);
        Geometry geom = new Geometry("Cylinder", t);
        geom.updateModelBound();

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        TextureKey key = new TextureKey("Interface/Logo/Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        mat.setTexture("m_ColorMap", tex);

        geom.setMaterial(mat);
        
        rootNode.attachChild(geom);
    }

}
