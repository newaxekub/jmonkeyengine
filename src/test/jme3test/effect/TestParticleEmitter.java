package jme3test.effect;

import com.jme3.app.SimpleApplication;
import com.jme3.effect.EmitterSphereShape;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;

public class TestParticleEmitter extends SimpleApplication {

    public static void main(String[] args){
        TestParticleEmitter app = new TestParticleEmitter();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        ParticleEmitter emit = new ParticleEmitter("Emitter", Type.Triangle, 200);
        emit.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        emit.setGravity(0);
        emit.setLowLife(5);
        emit.setHighLife(10);
        emit.setStartVel(new Vector3f(0, 0, 0));
        emit.setImagesX(15);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("m_Texture", assetManager.loadTexture("Effects/Smoke/Smoke.png"));
        emit.setMaterial(mat);

        rootNode.attachChild(emit);

//        Camera cam2 = cam.clone();
//        cam.setViewPortTop(0.5f);
//        cam2.setViewPortBottom(0.5f);
//        ViewPort vp = renderManager.createMainView("SecondView", cam2);
//        viewPort.setClearEnabled(false);
//        vp.attachScene(rootNode);

    }

}
