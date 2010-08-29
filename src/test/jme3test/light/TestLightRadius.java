package jme3test.light;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;

public class TestLightRadius extends SimpleApplication {

    float angle;
    PointLight pl;
    Geometry lightMdl;

    public static void main(String[] args){
        TestLightRadius app = new TestLightRadius();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Torus torus = new Torus(10, 6, 1, 3);
//        Torus torus = new Torus(50, 30, 1, 3);
        Geometry g = new Geometry("Torus Geom", torus);
        g.rotate(-FastMath.HALF_PI, 0, 0);
        g.center();
//        g.move(0, 1, 0);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setFloat("m_Shininess", 32f);
        mat.setBoolean("m_UseMaterialColors", true);
        mat.setColor("m_Ambient",  ColorRGBA.Black);
        mat.setColor("m_Diffuse",  ColorRGBA.White);
        mat.setColor("m_Specular", ColorRGBA.White);
//        mat.setBoolean("m_VertexLighting", true);
//        mat.setBoolean("m_LowQuality", true);
        g.setMaterial(mat);

        rootNode.attachChild(g);

        lightMdl = new Geometry("Light", new Sphere(10, 10, 0.1f));
        lightMdl.setMaterial(assetManager.loadMaterial("Common/Materials/RedColor.j3m"));
        rootNode.attachChild(lightMdl);

        pl = new PointLight();
        pl.setColor(ColorRGBA.Green);
        pl.setRadius(4f);
        rootNode.addLight(pl);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.Red);
        dl.setDirection(new Vector3f(0, 1, 0));
        rootNode.addLight(dl);
    }

    @Override
    public void simpleUpdate(float tpf){
//        cam.setLocation(new Vector3f(5.0347548f, 6.6481347f, 3.74853f));
//        cam.setRotation(new Quaternion(-0.19183293f, 0.80776674f, -0.37974006f, -0.40805697f));

        angle += tpf;
        angle %= FastMath.TWO_PI;

        pl.setPosition(new Vector3f(FastMath.cos(angle) * 3f, 2, FastMath.sin(angle) * 3f));
        lightMdl.setLocalTranslation(pl.getPosition());
    }

}
