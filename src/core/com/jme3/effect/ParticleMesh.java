package com.jme3.effect;

import com.jme3.renderer.Camera;
import com.jme3.scene.Mesh;

public abstract class ParticleMesh extends Mesh {

    public static enum Type {
        Point,
        Line,
        Triangle
    }

    public abstract void initParticleData(ParticleEmitter emitter, int numParticles, int imagesX, int imagesY);
    public abstract void updateParticleData(Particle[] particles, Camera cam);

}
