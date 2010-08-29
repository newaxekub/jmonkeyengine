package jme3test.material;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;

// phong cutoff for light to normal angle > 90?
public class TestSimpleBumps extends SimpleApplication {

    float angle;
    PointLight pl;
    Spatial lightMdl;

    public static void main(String[] args){
        TestSimpleBumps app = new TestSimpleBumps();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Quad quadMesh = new Quad(1, 1);

        Geometry sphere = new Geometry("Rock Ball", quadMesh);
        Material mat = assetManager.loadMaterial("Textures/BumpMapTest/SimpleBump.j3m");
        sphere.setMaterial(mat);
        rootNode.attachChild(sphere);

        lightMdl = new Geometry("Light", new Sphere(10, 10, 0.1f));
        lightMdl.setMaterial(assetManager.loadMaterial("Common/Materials/RedColor.j3m"));
        rootNode.attachChild(lightMdl);

        pl = new PointLight();
        pl.setColor(ColorRGBA.White);
        pl.setPosition(new Vector3f(0f, 0f, 4f));
        rootNode.addLight(pl);

//        DirectionalLight dl = new DirectionalLight();
//        dl.setDirection(new Vector3f(1, -1, -1).normalizeLocal());
//        dl.setColor(new ColorRGBA(0.22f, 0.15f, 0.1f, 1.0f));
//        rootNode.addLight(dl);
    }

    @Override
    public void simpleUpdate(float tpf){
        angle += tpf * 0.25f;
        angle %= FastMath.TWO_PI;

        pl.setPosition(new Vector3f(FastMath.cos(angle) * 4f, 0.5f, FastMath.sin(angle) * 4f));
        lightMdl.setLocalTranslation(pl.getPosition());
    }

}
