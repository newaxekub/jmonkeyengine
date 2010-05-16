package jme3test.conversion;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import jme3tools.converters.MipMapGenerator;

public class TestMipMapGen extends SimpleApplication {

    public static void main(String[] args){
        TestMipMapGen app = new TestMipMapGen();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        BitmapText txt = guiFont.createLabel("Left: HW Mips");
        txt.setLocalTranslation(0, txt.getLineHeight() * 4, 0);
        guiNode.attachChild(txt);

        txt = guiFont.createLabel("Right: AWT Mips");
        txt.setLocalTranslation(0, txt.getLineHeight() * 3, 0);
        guiNode.attachChild(txt);

        // create a simple plane/quad
        Quad quadMesh = new Quad(1, 1);
        quadMesh.updateGeometry(1, 1, false);
        quadMesh.updateBound();

        Geometry quad1 = new Geometry("Textured Quad", quadMesh);
        Geometry quad2 = new Geometry("Textured Quad 2", quadMesh);

        Texture tex = assetManager.loadTexture("Interface/Logo/Monkey.png");
        tex.setMinFilter(Texture.MinFilter.Trilinear);

        Texture texCustomMip = tex.clone();
        Image imageCustomMip = texCustomMip.getImage().clone();
        MipMapGenerator.generateMipMaps(imageCustomMip);
        texCustomMip.setImage(imageCustomMip);

        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        mat1.setTexture("m_ColorMap", tex);

        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        mat2.setTexture("m_ColorMap", texCustomMip);

        quad1.setMaterial(mat1);
//        quad1.setLocalTranslation(1, 0, 0);

        quad2.setMaterial(mat2);
        quad2.setLocalTranslation(1, 0, 0);

        rootNode.attachChild(quad1);
        rootNode.attachChild(quad2);
    }

}
