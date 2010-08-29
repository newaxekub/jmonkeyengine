package jme3test.asset;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.HttpZipLocator;
import com.jme3.material.Material;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.texture.plugins.AWTLoader;

/**
 * This tests loading a file from a jar stored online.
 * @author Kirill Vainer
 */
public class TestOnlineJar extends SimpleApplication {

    public static void main(String[] args){
        TestOnlineJar app = new TestOnlineJar();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // create a simple plane/quad
        Quad quadMesh = new Quad(1, 1);
        quadMesh.updateGeometry(1, 1, true);

        Geometry quad = new Geometry("Textured Quad", quadMesh);

        AssetManager am = new DesktopAssetManager();
        am.registerLocator("http://www.jmonkeyengine.com/applet/jme3testdata.jar",
                           HttpZipLocator.class.getName());
        am.registerLoader(AWTLoader.class.getName(), "png");

        TextureKey key = new TextureKey("textures/pond.png", false);
        key.setGenerateMips(true);
        Texture tex = am.loadTexture(key);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        mat.setTexture("m_ColorMap", tex);
        quad.setMaterial(mat);

        float aspect = tex.getImage().getWidth() / (float) tex.getImage().getHeight();
        quad.setLocalScale(new Vector3f(aspect * 1.5f, 1.5f, 1));
        quad.center();

        rootNode.attachChild(quad);
    }

}
