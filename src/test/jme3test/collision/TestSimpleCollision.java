package jme3test.collision;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;

/**
 * Test simple collision with a plane.
 */
public class TestSimpleCollision extends SimpleApplication {

    public static void main(String[] args){
        TestSimpleCollision app = new TestSimpleCollision();
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(60);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
//        Sphere q = new Sphere(32, 32, 2);
        
        Quad q = new Quad(5, 5);
        q.createCollisionData();

        Geometry g = new Geometry("Quad Geom", q);      
        g.rotate(FastMath.HALF_PI, 0, FastMath.PI);        
        g.setMaterial( (Material) assetManager.loadAsset(new AssetKey("Interface/Logo/Logo.j3m")));
        rootNode.attachChild(g);
        
        flyCam.setMotionAllowedListener(new SphereMotionAllowedListener(rootNode, new Vector3f(7f, 7f, 7f)));
    }

}