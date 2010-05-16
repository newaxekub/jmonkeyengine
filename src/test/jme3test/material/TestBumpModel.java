package jme3test.material;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.ogre.OgreMeshKey;
import com.jme3.scene.shape.Sphere;

public class TestBumpModel extends SimpleApplication {

    float angle;
    PointLight pl;
    Spatial lightMdl;

    public static void main(String[] args){
        TestBumpModel app = new TestBumpModel();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Spatial signpost = (Spatial) assetManager.loadAsset(new OgreMeshKey("Models/Sign Post/Sign Post.meshxml", null));
        signpost.setMaterial( (Material) assetManager.loadAsset(new AssetKey("Models/Sign Post/Sign Post.j3m")));
        rootNode.attachChild(signpost);

        lightMdl = new Geometry("Light", new Sphere(10, 10, 0.1f));
        lightMdl.setMaterial( (Material) assetManager.loadAsset(new AssetKey("Common/Materials/RedColor.j3m")));
        rootNode.attachChild(lightMdl);

        // flourescent main light
        pl = new PointLight();
        pl.setColor(new ColorRGBA(0.88f, 0.92f, 0.95f, 1.0f));
        rootNode.addLight(pl);

        // sunset light
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f,-0.7f,1).normalizeLocal());
        dl.setColor(new ColorRGBA(0.44f, 0.30f, 0.20f, 1.0f));
        rootNode.addLight(dl);

        // skylight
        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.6f,-1,-0.6f).normalizeLocal());
        dl.setColor(new ColorRGBA(0.10f, 0.22f, 0.44f, 1.0f));
        rootNode.addLight(dl);

        // white ambient light
        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(1, -0.5f,-0.1f).normalizeLocal());
        dl.setColor(new ColorRGBA(0.50f, 0.40f, 0.50f, 1.0f));
        rootNode.addLight(dl);
    }

    @Override
    public void simpleUpdate(float tpf){
        angle += tpf * 0.25f;
        angle %= FastMath.TWO_PI;

        pl.setPosition(new Vector3f(FastMath.cos(angle) * 6f, 3f, FastMath.sin(angle) * 6f));
        lightMdl.setLocalTranslation(pl.getPosition());
    }

}
