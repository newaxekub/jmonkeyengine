package jme3test.texture;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

public class TestDdsLoading extends SimpleApplication {

    public static void main(String[] args){
        TestDdsLoading app = new TestDdsLoading();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // create a simple plane/quad
        Quad quadMesh = new Quad(1, 1);

        Geometry quad = new Geometry("Textured Quad", quadMesh);
        quad.updateModelBound();
        
        Texture tex = assetManager.loadTexture("Textures/Sky/Night/Night_dxt1.dds");

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        mat.setTexture("m_ColorMap", tex);
        quad.setMaterial(mat);

        float aspect = tex.getImage().getWidth() / (float) tex.getImage().getHeight();
        quad.setLocalScale(new Vector3f(aspect * 5, 5, 1));
        quad.center();

        rootNode.attachChild(quad);
    }

}
