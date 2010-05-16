package jme3test.effect;

import com.jme3.app.SimpleApplication;
import com.jme3.effect.EmitterSphereShape;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class TestExplosionEffect extends SimpleApplication {

    private float time = 0;
    private int state = 0;
    private ParticleEmitter flame, flash, spark, roundspark, smoketrail, debris,
                            shockwave;


    private static final int COUNT_FACTOR = 1;
    private static final float COUNT_FACTOR_F = 1f;

    public static void main(String[] args){
        TestExplosionEffect app = new TestExplosionEffect();
        app.start();
    }

    private void createFlame(){
        flame = new ParticleEmitter("Flame", Type.Triangle, 32 * COUNT_FACTOR);
        flame.setSelectRandomImage(true);
        flame.setStartColor(new ColorRGBA(1f, 0.4f, 0.05f, (float) (1f / COUNT_FACTOR_F)));
        flame.setEndColor(new ColorRGBA(.4f, .22f, .12f, 0f));
        flame.setStartSize(1.3f);
        flame.setEndSize(2f);
        flame.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        flame.setParticlesPerSec(0);
        flame.setGravity(-5f);
        flame.setLowLife(.4f);
        flame.setHighLife(.5f);
        flame.setStartVel(new Vector3f(0, 7, 0));
        flame.setVariation(1f);
        flame.setImagesX(2);
        flame.setImagesY(2);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("m_Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        flame.setMaterial(mat);
        rootNode.attachChild(flame);
    }

    private void createFlash(){
        flash = new ParticleEmitter("Flash", Type.Triangle, 24 * COUNT_FACTOR);
        flash.setSelectRandomImage(true);
        flash.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1f / COUNT_FACTOR_F)));
        flash.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
        flash.setStartSize(.1f);
        flash.setEndSize(3.0f);
        flash.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
        flash.setParticlesPerSec(0);
        flash.setGravity(0);
        flash.setLowLife(.2f);
        flash.setHighLife(.2f);
        flash.setStartVel(new Vector3f(0, 5f, 0));
        flash.setVariation(1);
        flash.setImagesX(2);
        flash.setImagesY(2);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("m_Texture", assetManager.loadTexture("Effects/Explosion/flash.png"));
        flash.setMaterial(mat);
        rootNode.attachChild(flash);
    }

    private void createRoundSpark(){
        roundspark = new ParticleEmitter("RoundSpark", Type.Triangle, 20 * COUNT_FACTOR);
        roundspark.setStartColor(new ColorRGBA(1f, 0.29f, 0.34f, (float) (1.0 / COUNT_FACTOR_F)));
        roundspark.setEndColor(new ColorRGBA(0, 0, 0, (float) (0.5f / COUNT_FACTOR_F)));
        roundspark.setStartSize(1.2f);
        roundspark.setEndSize(1.8f);
        roundspark.setShape(new EmitterSphereShape(Vector3f.ZERO, 2f));
        roundspark.setParticlesPerSec(0);
        roundspark.setGravity(-.5f);
        roundspark.setLowLife(1.8f);
        roundspark.setHighLife(2f);
        roundspark.setStartVel(new Vector3f(0, 3, 0));
        roundspark.setVariation(.5f);
        roundspark.setImagesX(1);
        roundspark.setImagesY(1);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("m_Texture", assetManager.loadTexture("Effects/Explosion/roundspark.png"));
        roundspark.setMaterial(mat);
        rootNode.attachChild(roundspark);
    }

    private void createSpark(){
        spark = new ParticleEmitter("Spark", Type.Triangle, 30 * COUNT_FACTOR);
        spark.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1.0f / COUNT_FACTOR_F)));
        spark.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
        spark.setStartSize(.5f);
        spark.setEndSize(.5f);

//        spark.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
        spark.setFacingVelocity(true);
        spark.setParticlesPerSec(0);
        spark.setGravity(5);
        spark.setLowLife(1.1f);
        spark.setHighLife(1.5f);
        spark.setStartVel(new Vector3f(0, 20, 0));
        spark.setVariation(1);
        spark.setImagesX(1);
        spark.setImagesY(1);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("m_Texture", assetManager.loadTexture("Effects/Explosion/spark.png"));
        spark.setMaterial(mat);
        rootNode.attachChild(spark);
    }

    private void createSmokeTrail(){
        smoketrail = new ParticleEmitter("SmokeTrail", Type.Triangle, 22 * COUNT_FACTOR);
        smoketrail.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1.0f / COUNT_FACTOR_F)));
        smoketrail.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
        smoketrail.setStartSize(.2f);
        smoketrail.setEndSize(1f);

//        smoketrail.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        smoketrail.setFacingVelocity(true);
        smoketrail.setParticlesPerSec(0);
        smoketrail.setGravity(1);
        smoketrail.setLowLife(.4f);
        smoketrail.setHighLife(.5f);
        smoketrail.setStartVel(new Vector3f(0, 12, 0));
        smoketrail.setVariation(1);
        smoketrail.setImagesX(1);
        smoketrail.setImagesY(3);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("m_Texture", assetManager.loadTexture("Effects/Explosion/smoketrail.png"));
        smoketrail.setMaterial(mat);
        rootNode.attachChild(smoketrail);
    }

    private void createDebris(){
        debris = new ParticleEmitter("Debris", Type.Triangle, 15 * COUNT_FACTOR);
        debris.setSelectRandomImage(true);
        debris.setRandomAngle(true);
        debris.setRotateSpeed(FastMath.TWO_PI * 4);
        debris.setStartColor(new ColorRGBA(1f, 0.59f, 0.28f, (float) (1.0f / COUNT_FACTOR_F)));
        debris.setEndColor(new ColorRGBA(.5f, 0.5f, 0.5f, 0f));
        debris.setStartSize(.2f);
        debris.setEndSize(.2f);

//        debris.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
        debris.setParticlesPerSec(0);
        debris.setGravity(12f);
        debris.setLowLife(1.4f);
        debris.setHighLife(1.5f);
        debris.setStartVel(new Vector3f(0, 15, 0));
        debris.setVariation(.60f);
        debris.setImagesX(3);
        debris.setImagesY(3);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("m_Texture", assetManager.loadTexture("Effects/Explosion/Debris.png"));
        debris.setMaterial(mat);
        rootNode.attachChild(debris);
    }

    private void createShockwave(){
        shockwave = new ParticleEmitter("Shockwave", Type.Triangle, 1 * COUNT_FACTOR);
//        shockwave.setRandomAngle(true);
        shockwave.setFaceNormal(Vector3f.UNIT_Y);
        shockwave.setStartColor(new ColorRGBA(.48f, 0.17f, 0.01f, (float) (.8f / COUNT_FACTOR_F)));
        shockwave.setEndColor(new ColorRGBA(.48f, 0.17f, 0.01f, 0f));

        shockwave.setStartSize(0f);
        shockwave.setEndSize(7f);

        shockwave.setParticlesPerSec(0);
        shockwave.setGravity(0);
        shockwave.setLowLife(0.5f);
        shockwave.setHighLife(0.5f);
        shockwave.setStartVel(new Vector3f(0, 0, 0));
        shockwave.setVariation(0f);
        shockwave.setImagesX(1);
        shockwave.setImagesY(1);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("m_Texture", assetManager.loadTexture("Effects/Explosion/shockwave.png"));
        shockwave.setMaterial(mat);
        rootNode.attachChild(shockwave);
    }

    @Override
    public void simpleInitApp() {
        createFlame();
        createFlash();
        createSpark();
        createRoundSpark();
        createSmokeTrail();
        createDebris();
        createShockwave();
        rootNode.setLocalScale(0.5f);

        cam.setLocation(new Vector3f(0, 3.5135868f, 10));
        cam.setRotation(new Quaternion(1.5714673E-4f, 0.98696727f, -0.16091813f, 9.6381607E-4f));
    }

    @Override
    public void simpleUpdate(float tpf){
        time += tpf / speed;
//        speed = 0.02f;
        if (time > 1f && state == 0){
            flash.emitAllParticles();
            spark.emitAllParticles();
            smoketrail.emitAllParticles();
            debris.emitAllParticles();
            shockwave.emitAllParticles();
            state++;
        }
        if (time > 1f + .05f / speed && state == 1){
            flame.emitAllParticles();
            roundspark.emitAllParticles();
            state++;
        }
        
        // rewind the effect
        if (time > 5 / speed && state == 2){
            state = 0;
            time = 0;

            flash.killAllParticles();
            spark.killAllParticles();
            smoketrail.killAllParticles();
            debris.killAllParticles();
            flame.killAllParticles();
            roundspark.killAllParticles();
            shockwave.killAllParticles();
        }
    }

}
