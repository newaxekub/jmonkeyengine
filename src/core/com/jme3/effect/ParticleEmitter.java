package com.jme3.effect;

import com.jme3.effect.ParticleMesh.Type;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.util.TempVars;
import java.io.IOException;

public class ParticleEmitter extends Geometry implements Control {

    private static final EmitterShape DEFAULT_SHAPE = new EmitterPointShape(Vector3f.ZERO);

    private EmitterShape shape = DEFAULT_SHAPE;
    private ParticleMesh particleMesh;
    private ParticleMesh.Type meshType;
    private Particle[] particles;

    private int firstUnUsed;
    private int lastUsed;

//    private int next = 0;
//    private ArrayList<Integer> unusedIndices = new ArrayList<Integer>();

    private boolean randomAngle = false;
    private boolean selectRandomImage = false;
    private boolean facingVelocity = false;
    private float particlesPerSec = 20;
    private float emitCarry = 0f;
    private float lowLife  = 3f;
    private float highLife = 7f;
    private float gravity = 0.1f;
    private float variation = 0.2f;
    private float rotateSpeed = 0;
    private Vector3f startVel = new Vector3f();
    private Vector3f faceNormal = new Vector3f(Vector3f.NAN);

    private int imagesX = 1;
    private int imagesY = 1;

    private boolean enabled = true;
    private ColorRGBA startColor = new ColorRGBA(0.4f,0.4f,0.4f,0.5f);
    private ColorRGBA endColor = new ColorRGBA(0.1f,0.1f,0.1f,0.0f);
    private float startSize = 0.2f;
    private float endSize = 2f;
    private boolean worldSpace = true;

    public ParticleEmitter(String name, Type type, int numParticles){
        super(name);

        // ignore world transform, unless user sets inLocalSpace
        setIgnoreTransform(true);

        // particles neither recieve nor cast shadows
        setShadowMode(ShadowMode.Off);

        // particles are usually transparent
        setQueueBucket(Bucket.Transparent);

        meshType = type;

        setNumParticles(numParticles);

        controls.add(this);
    }

    public ParticleEmitter(){
        super();
    }

    public Control cloneForSpatial(Spatial spatial){
        return (Control) spatial;
    }

    public void setShape(EmitterShape shape) {
        this.shape = shape;
    }

    public EmitterShape getShape(){
        return shape;
    }

    public boolean isInWorldSpace() {
        return worldSpace;
    }

    public void setInWorldSpace(boolean worldSpace) {
        setIgnoreTransform(worldSpace);
        this.worldSpace = worldSpace;
    }

    public int getNumVisibleParticles(){
//        return unusedIndices.size() + next;
        return lastUsed + 1;
    }

    /**
     * @param numParticles The maximum amount of particles that
     * can exist at the same time with this emitter.
     * Calling this method many times is not recommended.
     */
    public final void setNumParticles(int numParticles){
        particles = new Particle[numParticles];
        for (int i = 0; i < numParticles; i++){
            particles[i] = new Particle();
        }
        firstUnUsed = 0;
        lastUsed = -1;
    }

    public Vector3f getFaceNormal() {
        if (Vector3f.isValidVector(faceNormal))
            return faceNormal;
        else
            return null;
    }

    public void setFaceNormal(Vector3f faceNormal) {
        if (faceNormal == null || !Vector3f.isValidVector(faceNormal))
            faceNormal.set(Vector3f.NAN);
        else
            this.faceNormal = faceNormal;
    }

    public float getRotateSpeed() {
        return rotateSpeed;
    }

    public void setRotateSpeed(float rotateSpeed) {
        this.rotateSpeed = rotateSpeed;
    }

    public boolean isRandomAngle() {
        return randomAngle;
    }

    public void setRandomAngle(boolean randomAngle) {
        this.randomAngle = randomAngle;
    }

    public boolean isSelectRandomImage() {
        return selectRandomImage;
    }

    public void setSelectRandomImage(boolean selectRandomImage) {
        this.selectRandomImage = selectRandomImage;
    }

    public boolean isFacingVelocity() {
        return facingVelocity;
    }

    public void setFacingVelocity(boolean followVelocity) {
        this.facingVelocity = followVelocity;
    }

    public ColorRGBA getEndColor() {
        return endColor;
    }

    public void setEndColor(ColorRGBA endColor) {
        this.endColor.set(endColor);
    }

    public float getEndSize() {
        return endSize;
    }

    public void setEndSize(float endSize) {
        this.endSize = endSize;
    }

    public float getGravity() {
        return gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public float getHighLife() {
        return highLife;
    }

    public void setHighLife(float highLife) {
        this.highLife = highLife;
    }

    public int getImagesX() {
        return imagesX;
    }

    public void setImagesX(int imagesX) {
        this.imagesX = imagesX;
    }

    public int getImagesY() {
        return imagesY;
    }

    public void setImagesY(int imagesY) {
        this.imagesY = imagesY;
    }

    public float getLowLife() {
        return lowLife;
    }

    public void setLowLife(float lowLife) {
        this.lowLife = lowLife;
    }

    public float getParticlesPerSec() {
        return particlesPerSec;
    }

    public void setParticlesPerSec(float particlesPerSec) {
        this.particlesPerSec = particlesPerSec;
    }

    public ColorRGBA getStartColor() {
        return startColor;
    }

    public void setStartColor(ColorRGBA startColor) {
        this.startColor.set(startColor);
    }

    public float getStartSize() {
        return startSize;
    }

    public void setStartSize(float startSize) {
        this.startSize = startSize;
    }

    public Vector3f getStartVel() {
        return startVel;
    }

    public void setStartVel(Vector3f startVel) {
        this.startVel.set(startVel);
    }

    public float getVariation() {
        return variation;
    }

    public void setVariation(float variation) {
        this.variation = variation;
    }

//    private int newIndex(){
//        liveParticles ++;
//        return unusedIndices.remove(0);
//        if (unusedIndices.size() > 0){
//            liveParticles++;
//            return unusedIndices.remove(0);
//        }else if (next < particles.length){
//            liveParticles++;
//            return next++;
//        }else{
//            return -1;
//        }
//    }

//    private void freeIndex(int index){
//        liveParticles--;
//        if (index == next-1)
//            next--;
//        else
//        assert !unusedIndices.contains(index);
//        unusedIndices.add(index);
//    }

    private boolean emitParticle(){
//        int idx = newIndex();
//        if (idx == -1)
//            return false;
        int idx = lastUsed + 1;
        if (idx >= particles.length) {
            return false;
        }

        Particle p = particles[idx];
        if (selectRandomImage)
            p.imageIndex = (FastMath.nextRandomInt(0, imagesY-1) * imagesX) + FastMath.nextRandomInt(0, imagesX-1);

        p.startlife = lowLife + FastMath.nextRandomFloat() * (highLife - lowLife);
        p.life = p.startlife;
        p.color.set(startColor);
        p.size = startSize;
        shape.getRandomPoint(p.position);
        if (worldSpace){
            p.position.addLocal(worldTransform.getTranslation());
        }
        p.velocity.set(startVel);
        if (randomAngle)
            p.angle = FastMath.nextRandomFloat() * FastMath.TWO_PI;
        if (rotateSpeed != 0)
            p.rotateSpeed = rotateSpeed * (0.2f + (FastMath.nextRandomFloat() * 2f - 1f) * .8f);

        assert TempVars.get().lock();
        Vector3f temp = TempVars.get().vect1;
        temp.set(FastMath.nextRandomFloat(),FastMath.nextRandomFloat(),FastMath.nextRandomFloat());
        temp.multLocal(2f);
        temp.subtractLocal(1f,1f,1f);
        temp.multLocal(startVel.length());
        p.velocity.interpolate(temp, variation);
        assert TempVars.get().unlock();

        lastUsed++;
        firstUnUsed = idx + 1;

        return true;
    }

    @SuppressWarnings("empty-statement")
    public void emitAllParticles(){
        while (emitParticle());
    }

    public void killAllParticles(){
        for (int i = 0; i < particles.length; i++){
            if (particles[i].life > 0)
                freeParticle(i);
        }
    }

    private void freeParticle(int idx){
        Particle p = particles[idx];
        p.life = 0;
        p.size = 0f;
        p.color.set(0,0,0,0);
        p.imageIndex = 0;
        p.angle = 0;
        p.rotateSpeed = 0;

//        freeIndex(idx);

        if (idx == lastUsed) {
            while (lastUsed >= 0 && particles[lastUsed].life == 0) {
                lastUsed--;
            }
        }
        if (idx < firstUnUsed) {
            firstUnUsed = idx;
        }
    }

     private void swap(int idx1, int idx2) {
        Particle p1 = particles[idx1];
        particles[idx1] = particles[idx2];
        particles[idx2] = p1;
    }

    private void updateParticleState(float tpf){
        assert TempVars.get().lock();
        Vector3f temp = TempVars.get().vect1;
        for (int i = 0; i < particles.length; i++){
            Particle p = particles[i];
            if (p.life == 0){ // particle is dead
//                assert i <= firstUnUsed;
                continue;
            }

            p.life -= tpf;
            if (p.life <= 0){
                freeParticle(i);
                continue;
            }

            // position += velocity * tpf
            float g = gravity * tpf;
            p.velocity.y -= g;
            temp.set(p.velocity).multLocal(tpf);
            p.position.addLocal(temp);

            float b = (p.startlife - p.life) / p.startlife;
            p.color.interpolate(startColor, endColor, b);
            p.size = FastMath.interpolateLinear(b, startSize, endSize);
            p.angle += p.rotateSpeed * tpf;

            if (!selectRandomImage) // use animated effect
                p.imageIndex = (int) (b * imagesX * imagesY);
            
             if (firstUnUsed < i) {
                swap(firstUnUsed, i);
                if (i == lastUsed) {
                    lastUsed = firstUnUsed;
                }
                firstUnUsed++;
            }
        }

        assert TempVars.get().unlock();

        float particlesToEmitF = particlesPerSec * tpf;
        int particlesToEmit = (int) (particlesToEmitF);
        emitCarry += particlesToEmitF - particlesToEmit;

        while (emitCarry > 1f){
            particlesToEmit ++;
            emitCarry -= 1f;
        }

//        if (emitCarry > 1f){
//            particlesToEmit ++;
//            emitCarry = 0f;
//        }

        for (int i = 0; i < particlesToEmit; i++){
            emitParticle();
        }
    }

    public void setSpatial(Spatial spatial) {
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void update(float tpf) {
        if (!enabled)
            return;

        if (particleMesh == null){
            switch (meshType){
                case Point:
                    particleMesh = new ParticlePointMesh();
                    setMesh(particleMesh);
                    break;
                case Triangle:
                    particleMesh = new ParticleTriMesh();
                    setMesh(particleMesh);
                    break;
                default:
                    throw new IllegalStateException("Unrecognized particle type: "+meshType);
            }
            // create it
            particleMesh.initParticleData(this, particles.length, imagesX, imagesY);
        }

        updateParticleState(tpf);
    }

    public void render(RenderManager rm, ViewPort vp) {
        Camera cam = vp.getCamera();
        particleMesh.updateParticleData(particles, cam);
        updateModelBound();
    }

    @Override
    public void write(JmeExporter ex) throws IOException{
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(shape, "shape", DEFAULT_SHAPE);
        oc.write(meshType, "meshType", ParticleMesh.Type.Triangle);
        oc.write(enabled, "enabled", true);
        oc.write(particles.length, "numParticles", 0);
        oc.write(particlesPerSec, "particlesPerSec", 0);
        oc.write(lowLife, "lowLife", 0);
        oc.write(highLife, "highLife", 0);
        oc.write(gravity, "gravity", 0);
        oc.write(variation, "variation", 0);
        oc.write(imagesX, "imagesX", 1);
        oc.write(imagesY, "imagesY", 1);

        oc.write(startVel, "startVel", null);
        oc.write(startColor, "startColor", null);
        oc.write(endColor, "endColor", null);
        oc.write(startSize, "startSize", 0);
        oc.write(endSize, "endSize", 0);
        oc.write(worldSpace, "worldSpace", false);
        oc.write(facingVelocity, "facingVelocity", false);
        oc.write(selectRandomImage, "selectRandomImage", false);
        oc.write(randomAngle, "randomAngle", false);
        oc.write(rotateSpeed, "rotateSpeed", 0);
    }

    @Override
    public void read(JmeImporter im) throws IOException{
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        shape = (EmitterShape) ic.readSavable("shape", DEFAULT_SHAPE);
        meshType = ic.readEnum("meshType", ParticleMesh.Type.class, ParticleMesh.Type.Triangle);
        int numParticles = ic.readInt("numParticles", 0);
        setNumParticles(numParticles);

        enabled = ic.readBoolean("enabled", true);
        particlesPerSec = ic.readFloat("particlesPerSec", 0);
        lowLife = ic.readFloat("lowLife", 0);
        highLife = ic.readFloat("highLife", 0);
        gravity = ic.readFloat("gravity", 0);
        variation = ic.readFloat("variation", 0);
        imagesX = ic.readInt("imagesX", 1);
        imagesY = ic.readInt("imagesY", 1);

        startVel = (Vector3f) ic.readSavable("startVel", null);
        startColor = (ColorRGBA) ic.readSavable("startColor", null);
        endColor = (ColorRGBA) ic.readSavable("endColor", null);
        startSize = ic.readFloat("startSiz", 0);
        endSize = ic.readFloat("endSize", 0);
        worldSpace = ic.readBoolean("worldSpace", false);
        facingVelocity = ic.readBoolean("facingVelocity", false);
        selectRandomImage = ic.readBoolean("selectRandomImage", false);
        randomAngle = ic.readBoolean("randomAngle", false);
        rotateSpeed = ic.readFloat("rotateSpeed", 0);
    }

}
