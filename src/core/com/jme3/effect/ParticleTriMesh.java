package com.jme3.effect;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Usage;
import com.jme3.util.BufferUtils;
import com.jme3.util.SortUtil;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Comparator;

public class ParticleTriMesh extends ParticleMesh {

    private int imagesX;
    private int imagesY;
    private boolean uniqueTexCoords = false;
    private ParticleComparator comparator = new ParticleComparator();
    private ParticleEmitter emitter;
    private Particle[] particlesCopy;

    private class ParticleComparator implements Comparator<Particle> {

        private Camera cam;

        public void setCamera(Camera cam){
            this.cam = cam;
        }

        public int compare(Particle p1, Particle p2) {
            if (p1.life <= 0)
                return 1;
            else if (p2.life <= 0)
                return -1;

            float d1 = cam.distanceToNearPlane(p1.position);
            float d2 = cam.distanceToNearPlane(p2.position);
            if (d1 < d2)
                return 1;
            else if (d1 > d2)
                return -1;
            else
                return 0;
        }
    }

    @Override
    public void initParticleData(ParticleEmitter emitter, int numParticles, int imagesX, int imagesY) {
        setMode(Mode.Triangles);
        setVertexCount(numParticles * 4);
        setTriangleCount(numParticles * 2);

        this.emitter = emitter;
        this.imagesX = imagesX;
        this.imagesY = imagesY;

        particlesCopy = new Particle[numParticles];

        // set positions
        FloatBuffer pb = BufferUtils.createVector3Buffer(numParticles * 4);
        VertexBuffer pvb = new VertexBuffer(VertexBuffer.Type.Position);
        pvb.setupData(Usage.Stream, 3, Format.Float, pb);
        setBuffer(pvb);

        // set colors
        ByteBuffer cb = BufferUtils.createByteBuffer(numParticles * 4 * 4);
        VertexBuffer cvb = new VertexBuffer(VertexBuffer.Type.Color);
        cvb.setupData(Usage.Stream, 4, Format.UnsignedByte, cb);
        cvb.setNormalized(true);
        setBuffer(cvb);

        // set texcoords
        VertexBuffer tvb = new VertexBuffer(VertexBuffer.Type.TexCoord);
        FloatBuffer tb = BufferUtils.createVector2Buffer(numParticles * 4);
        if (imagesX == 1 && imagesY == 1){
            uniqueTexCoords = false;
            for (int i = 0; i < numParticles; i++){
                tb.put(0f).put(1f);
                tb.put(1f).put(1f);
                tb.put(0f).put(0f);
                tb.put(1f).put(0f);
            }
            tb.flip();
            tvb.setupData(Usage.Static, 2, Format.Float, tb);
        }else{
            uniqueTexCoords = true;
            tvb.setupData(Usage.Stream, 2, Format.Float, tb);
        }
        setBuffer(tvb);

        // set indices
        ShortBuffer ib = BufferUtils.createShortBuffer(numParticles * 6);
        for (int i = 0; i < numParticles; i++){
            int startIdx = (i * 4);

            // triangle 1
            ib.put((short)(startIdx + 1))
              .put((short)(startIdx + 0))
              .put((short)(startIdx + 2));

            // triangle 2
            ib.put((short)(startIdx + 1))
              .put((short)(startIdx + 2))
              .put((short)(startIdx + 3));
        }
        ib.flip();
        
        VertexBuffer ivb = new VertexBuffer(VertexBuffer.Type.Index);
        ivb.setupData(Usage.Static, 3, Format.UnsignedShort, ib);
        setBuffer(ivb);
    }

    @Override
    public void updateParticleData(Particle[] particles, Camera cam) {
        System.arraycopy(particles, 0, particlesCopy, 0, particlesCopy.length);
        comparator.setCamera(cam);
//        Arrays.sort(particlesCopy, comparator);
//        SortUtil.qsort(particlesCopy, comparator);
        SortUtil.msort(particles, particlesCopy, comparator);
        particles = particlesCopy;

        VertexBuffer pvb = getBuffer(VertexBuffer.Type.Position);
        FloatBuffer positions = (FloatBuffer) pvb.getData();

        VertexBuffer cvb = getBuffer(VertexBuffer.Type.Color);
        ByteBuffer colors = (ByteBuffer) cvb.getData();

        VertexBuffer tvb = getBuffer(VertexBuffer.Type.TexCoord);
        FloatBuffer texcoords = (FloatBuffer) tvb.getData();

        Vector3f camUp   = cam.getUp();
        Vector3f camLeft = cam.getLeft();
        Vector3f camDir  = cam.getDirection();

        boolean facingVelocity = emitter.isFacingVelocity();

        Vector3f up = new Vector3f(),
                 left = new Vector3f();

        if (!facingVelocity){
            up.set(camUp);
            left.set(camLeft);
        }

        // update data in vertex buffers
        positions.clear();
        colors.clear();
        texcoords.clear();
        Vector3f faceNormal = emitter.getFaceNormal();
        
        for (int i = 0; i < particles.length; i++){
            Particle p = particles[i];
            boolean dead = p.life == 0;
            if (dead){
                positions.put(0).put(0).put(0);
                positions.put(0).put(0).put(0);
                positions.put(0).put(0).put(0);
                positions.put(0).put(0).put(0);
                continue;
//                break;
            }
            
            if (facingVelocity){
                left.set(p.velocity).normalizeLocal().multLocal(p.size);
                camDir.cross(left, up);
                up.multLocal(p.size);
            }else if (faceNormal != null){
                up.set(faceNormal).crossLocal(Vector3f.UNIT_X);
                faceNormal.cross(up, left);
                up.multLocal(p.size);
                left.multLocal(p.size);
            }else if (p.angle != 0){
                float cos = FastMath.cos(p.angle) * p.size;
                float sin = FastMath.sin(p.angle) * p.size;

                left.x = camLeft.x * cos + camUp.x * sin;
                left.y = camLeft.y * cos + camUp.y * sin;
                left.z = camLeft.z * cos + camUp.z * sin;

                up.x = camLeft.x * -sin + camUp.x * cos;
                up.y = camLeft.y * -sin + camUp.y * cos;
                up.z = camLeft.z * -sin + camUp.z * cos;
            }else{
                up.set(camUp);
                left.set(camLeft);
                up.multLocal(p.size);
                left.multLocal(p.size);
            }

            positions.put(p.position.x + left.x + up.x)
                     .put(p.position.y + left.y + up.y)
                     .put(p.position.z + left.z + up.z);

            positions.put(p.position.x - left.x + up.x)
                     .put(p.position.y - left.y + up.y)
                     .put(p.position.z - left.z + up.z);

            positions.put(p.position.x + left.x - up.x)
                     .put(p.position.y + left.y - up.y)
                     .put(p.position.z + left.z - up.z);

            positions.put(p.position.x - left.x - up.x)
                     .put(p.position.y - left.y - up.y)
                     .put(p.position.z - left.z - up.z);

            if (uniqueTexCoords){
                int imgX = p.imageIndex % imagesX;
                int imgY = (p.imageIndex - imgX) / imagesY;

                float startX = ((float) imgX) / imagesX;
                float startY = ((float) imgY) / imagesY;
                float endX   = startX + (1f / imagesX);
                float endY   = startY + (1f / imagesY);

                texcoords.put(startX).put(endY);
                texcoords.put(endX).put(endY);
                texcoords.put(startX).put(startY);
                texcoords.put(endX).put(startY);
            }

            int abgr = p.color.asIntABGR();
            colors.putInt(abgr);
            colors.putInt(abgr);
            colors.putInt(abgr);
            colors.putInt(abgr);
        }

        positions.clear();
        colors.clear();
        if (!uniqueTexCoords)
            texcoords.clear();
        else{
            texcoords.clear();
            tvb.updateData(texcoords);
        }

        // force renderer to re-send data to GPU
        pvb.updateData(positions);
        cvb.updateData(colors);
    }

}
