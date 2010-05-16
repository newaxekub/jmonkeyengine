package jme3test.light;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.TangentBinormalGenerator;

public class TestTangentGenBadUV extends SimpleApplication {

    float angle;
    PointLight pl;
    Geometry lightMdl;

    public static void main(String[] args){
        TestTangentGenBadUV app = new TestTangentGenBadUV();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Spatial teapot = assetManager.loadModel("Models/Teapot/Teapot.obj");
        if (teapot instanceof Geometry){
            Geometry g = (Geometry) teapot;
            TangentBinormalGenerator.generate(g.getMesh());
        }else{
            throw new RuntimeException();
        }
        teapot.setLocalScale(2f);
        Material mat = assetManager.loadMaterial("Textures/BumpMapTest/Tangent.j3m");
        teapot.setMaterial(mat);
        rootNode.attachChild(teapot);

        Geometry debug = new Geometry(
                "Debug Teapot",
                TangentBinormalGenerator.genTbnLines(((Geometry) teapot).getMesh(), 0.03f)
        );
        Material debugMat = assetManager.loadMaterial("Common/Materials/VertexColor.j3m");
        debug.setMaterial(debugMat);
        debug.setCullHint(Spatial.CullHint.Never);
        debug.getLocalTranslation().set(teapot.getLocalTranslation());
        debug.getLocalScale().set(teapot.getLocalScale());
        rootNode.attachChild(debug);


        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(1,-1,-1).normalizeLocal());
        dl.setColor(ColorRGBA.White);
        rootNode.addLight(dl);

        lightMdl = new Geometry("Light", new Sphere(10, 10, 0.1f));
        lightMdl.setMaterial(assetManager.loadMaterial("Common/Materials/RedColor.j3m"));
        lightMdl.getMesh().setStatic();
        rootNode.attachChild(lightMdl);

        pl = new PointLight();
        pl.setColor(ColorRGBA.White);
        //pl.setRadius(3f);
        rootNode.addLight(pl);
    }

    @Override
    public void simpleUpdate(float tpf){
        angle += tpf;
        angle %= FastMath.TWO_PI;
        
        pl.setPosition(new Vector3f(FastMath.cos(angle) * 2f, 0.5f, FastMath.sin(angle) * 2f));
        lightMdl.setLocalTranslation(pl.getPosition());
    }

}
