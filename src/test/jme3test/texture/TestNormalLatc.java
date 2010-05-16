package jme3test.texture;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

public class TestNormalLatc extends SimpleApplication {

    private Quad quadMesh;

    public static void main(String[] args){
        TestNormalLatc app = new TestNormalLatc();
        app.start();
    }

    public Geometry createQuad(float side, String texName, boolean latc){
        Geometry quad = new Geometry("Textured Quad", quadMesh);
        quad.updateModelBound();

        TextureKey key = new TextureKey(texName, true);
        key.setGenerateMips(false);
        Texture tex = assetManager.loadTexture(key);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        mat.setTexture("m_ColorMap", tex);
//        mat.setBoolean("m_Normalize", true);
        if (latc)
            mat.setBoolean("m_LATC", true);

        quad.setMaterial(mat);

        float aspect = tex.getImage().getWidth() / (float) tex.getImage().getHeight();
        quad.setLocalScale(new Vector3f(aspect * 5, 5, 1));
        quad.center();
        quad.setLocalTranslation(quad.getLocalTranslation().x + quad.getLocalScale().x  * side, 0, 0);

        return quad;
    }

    public void simpleInitApp() {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText txt = new BitmapText(font, false);
        txt.setText("Left: LATC, Middle: JPG, Right: DXT1nm");
        txt.setLocalTranslation(0, txt.getLineHeight() * 2, 0);
        guiNode.attachChild(txt);

        // create a simple plane/quad
        quadMesh = new Quad(1, 1);
        quadMesh.updateGeometry(1, 1, false);

        rootNode.attachChild(createQuad(-1f, "Textures/BumpMapTest/Dot3_latc.dds", true));
        rootNode.attachChild(createQuad(0f,  "Textures/BumpMapTest/Dot3.jpg", false));
        rootNode.attachChild(createQuad(1f,  "Textures/BumpMapTest/Dot3_dxt1.dds", false));
    }

}
