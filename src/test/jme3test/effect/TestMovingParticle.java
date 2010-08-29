package jme3test.effect;

import com.jme3.app.SimpleApplication;
import com.jme3.effect.EmitterSphereShape;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

/**
 * Particle that moves in a circle.
 *
 * @author Kirill Vainer
 */
public class TestMovingParticle  extends SimpleApplication {

    private ParticleEmitter emit;
    private float angle = 0;

    public static void main(String[] args){
        TestMovingParticle app = new TestMovingParticle();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        emit = new ParticleEmitter("Emitter", Type.Triangle, 200);
        emit.setGravity(0);
        emit.setVariation(1);
        emit.setLowLife(1);
        emit.setHighLife(1);
        emit.setStartVel(new Vector3f(0, .5f, 0));
        emit.setImagesX(15);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("m_Texture", assetManager.loadTexture("Effects/Smoke/Smoke.png"));
        emit.setMaterial(mat);

        rootNode.attachChild(emit);
    }

    @Override
    public void simpleUpdate(float tpf){
        angle += tpf;
        angle %= FastMath.TWO_PI;
        float x = FastMath.cos(angle) * 2;
        float y = FastMath.sin(angle) * 2;
        emit.setLocalTranslation(x, 0, y);
    }

}
