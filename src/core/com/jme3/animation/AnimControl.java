package com.jme3.animation;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.util.TempVars;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class AnimControl extends AbstractControl implements Savable, Cloneable {

    /**
     * List of targets which this controller effects.
     */
    Mesh[] targets;

    /**
     * Skeleton object must contain corresponding data for the targets' weight buffers.
     */
    Skeleton skeleton;

    /**
     * List of animations
     */
    HashMap<String, BoneAnimation> animationMap;

    /**
     * Animation channels
     */
    transient ArrayList<AnimChannel> channels
            = new ArrayList<AnimChannel>();

    transient ArrayList<AnimEventListener> listeners
            = new ArrayList<AnimEventListener>();

    public AnimControl(Node model, Mesh[] meshes, Skeleton skeleton){
        super(model);
        this.skeleton = skeleton;
        this.targets = meshes;
        reset();
    }

    /**
     * Used only for Saving/Loading models (all parameters of the non-default
     * constructor are restored from the saved model, but the object must be
     * constructed beforehand)
     */
    public AnimControl() {
    }

    public Control cloneForSpatial(Spatial spatial){
        try {
            Node clonedNode = (Node) spatial;
            AnimControl clone = (AnimControl) super.clone();
            clone.skeleton = new Skeleton(skeleton);
            Mesh[] meshes = new Mesh[targets.length];
            for (int i = 0; i < meshes.length; i++) {
                meshes[i] = ((Geometry) clonedNode.getChild(i)).getMesh();
            }
            clone.targets = meshes;
            clone.channels = new ArrayList<AnimChannel>();
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public void setAnimations(HashMap<String, BoneAnimation> animations){
        animationMap = animations;
    }

    public BoneAnimation getAnim(String name){
        return animationMap.get(name);
    }

    public void addAnim(BoneAnimation anim){
        animationMap.put(anim.getName(), anim);
    }

    public void removeAnim(BoneAnimation anim){
        animationMap.remove(anim.getName());
    }

    public AnimChannel createChannel(){
        AnimChannel channel = new AnimChannel(this);
        channels.add(channel);
        return channel;
    }

    public AnimChannel getChannel(int index){
        return channels.get(index);
    }

    public void clearChannels(){
        channels.clear();
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public Mesh[] getTargets() {
        return targets;
    }

    public void addListener(AnimEventListener listener){
        listeners.add(listener);
    }

    public void removeListener(AnimEventListener listener){
        listeners.remove(listener);
    }

    public void clearListeners(){
        listeners.clear();
    }

    void notifyAnimChange(AnimChannel channel, String name){
        for (int i = 0; i < listeners.size(); i++){
            listeners.get(i).onAnimChange(this, channel, name);
        }
    }

    void notifyAnimCycleDone(AnimChannel channel, String name){
        for (int i = 0; i < listeners.size(); i++){
            listeners.get(i).onAnimCycleDone(this, channel, name);
        }
    }

    void reset(){
        resetToBind();
        if (skeleton != null){
            skeleton.resetAndUpdate();
        }
    }

    void resetToBind(){
        for (int i = 0; i < targets.length; i++){
            Mesh mesh = targets[i];
            if (targets[i].getBuffer(Type.BindPosePosition) != null){
                VertexBuffer bi = mesh.getBuffer(Type.BoneIndex);
                if (!bi.getData().hasArray())
                    mesh.prepareForAnim(true);
                    
                VertexBuffer bindPos = mesh.getBuffer(Type.BindPosePosition);
                VertexBuffer bindNorm = mesh.getBuffer(Type.BindPoseNormal);
                VertexBuffer pos = mesh.getBuffer(Type.Position);
                VertexBuffer norm = mesh.getBuffer(Type.Normal);
                FloatBuffer pb = (FloatBuffer) pos.getData();
                FloatBuffer nb = (FloatBuffer) norm.getData();
                FloatBuffer bpb = (FloatBuffer) bindPos.getData();
                FloatBuffer bnb = (FloatBuffer) bindNorm.getData();
                pb.clear();
                nb.clear();
                bpb.clear();
                bnb.clear();
                pb.put(bpb).clear();
                nb.put(bnb).clear();
            }
        }
    }

    public Collection<String> getAnimationNames(){
        return animationMap.keySet();
    }

    public float getAnimationLength(String name){
        BoneAnimation a = animationMap.get(name);
        if (a == null)
            return -1;

        return a.getLength();
    }

    @Override
    protected void controlUpdate(float tpf) {
        resetToBind(); // reset morph meshes to bind pose
        skeleton.reset(); // reset skeleton to bind pose

        for (int i = 0; i < channels.size(); i++){
            channels.get(i).update(tpf);
        }

        skeleton.updateWorldVectors();
        // here update the targets verticles if no hardware skinning supported

        Matrix4f[] offsetMatrices = skeleton.computeSkinningMatrices();

        // if hardware skinning is supported, the matrices and weight buffer
        // will be sent by the SkinningShaderLogic object assigned to the shader
        for (int i = 0; i < targets.length; i++){
            softwareSkinUpdate(targets[i], offsetMatrices);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    private void softwareSkinUpdate(Mesh mesh, Matrix4f[] offsetMatrices){
        int maxWeightsPerVert = mesh.getMaxNumWeights();
        int fourMinusMaxWeights = 4 - maxWeightsPerVert;

        // NOTE: This code assumes the vertex buffer is in bind pose
        // resetToBind() has been called this frame
        VertexBuffer vb = mesh.getBuffer(Type.Position);
        FloatBuffer fvb = (FloatBuffer) vb.getData();
        fvb.rewind();

        VertexBuffer nb = mesh.getBuffer(Type.Normal);
        FloatBuffer fnb = (FloatBuffer) nb.getData();
        fnb.rewind();

        // get boneIndexes and weights for mesh
        ByteBuffer ib = (ByteBuffer) mesh.getBuffer(Type.BoneIndex).getData();
        FloatBuffer wb = (FloatBuffer) mesh.getBuffer(Type.BoneWeight).getData();

        ib.rewind();
        wb.rewind();

        float[] weights = wb.array();
        byte[] indices = ib.array();
        int idxWeights = 0;

        TempVars vars = TempVars.get();
        float[] posBuf = vars.skinPositions;
        float[] normBuf = vars.skinNormals;

        int iterations = (int) FastMath.ceil(fvb.capacity() / ((float)posBuf.length));
        int bufLength = posBuf.length * 3;
        for (int i = iterations-1; i >= 0; i--){
            // read next set of positions and normals from native buffer
            bufLength = Math.min(posBuf.length, fvb.remaining());
            fvb.get(posBuf, 0, bufLength);
            fnb.get(normBuf, 0, bufLength);
            int verts = bufLength / 3;
            int idxPositions = 0;

            // iterate vertices and apply skinning transform for each effecting bone
            for (int vert = verts - 1; vert >= 0; vert--){
                float nmx = normBuf[idxPositions];
                float vtx = posBuf[idxPositions++];
                float nmy = normBuf[idxPositions];
                float vty = posBuf[idxPositions++];
                float nmz = normBuf[idxPositions];
                float vtz = posBuf[idxPositions++];

                float rx=0, ry=0, rz=0, rnx=0, rny=0, rnz=0;

    //            float vtx = fvb.get();
    //            float vty = fvb.get();
    //            float vtz = fvb.get();
    //            float nmx = fnb.get();
    //            float nmy = fnb.get();
    //            float nmz = fnb.get();
    //            float rx=0, ry=0, rz=0, rnx=0, rny=0, rnz=0;

                for (int w = maxWeightsPerVert - 1; w >= 0; w--){
                    float weight = weights[idxWeights];//wb.get();
                    Matrix4f mat = offsetMatrices[indices[idxWeights++]];//offsetMatrices[ib.get()];

                    rx += (mat.m00 * vtx + mat.m01 * vty + mat.m02 * vtz + mat.m03) * weight;
                    ry += (mat.m10 * vtx + mat.m11 * vty + mat.m12 * vtz + mat.m13) * weight;
                    rz += (mat.m20 * vtx + mat.m21 * vty + mat.m22 * vtz + mat.m23) * weight;

                    rnx += (nmx * mat.m00 + nmy * mat.m01 + nmz * mat.m02) * weight;
                    rny += (nmx * mat.m10 + nmy * mat.m11 + nmz * mat.m12) * weight;
                    rnz += (nmx * mat.m20 + nmy * mat.m21 + nmz * mat.m22) * weight;
                }

                idxWeights += fourMinusMaxWeights;
    //            ib.position(ib.position()+fourMinusMaxWeights);
    //            wb.position(wb.position()+fourMinusMaxWeights);

                idxPositions -= 3;
                normBuf[idxPositions] = rnx;
                posBuf[idxPositions++] = rx;
                normBuf[idxPositions] = rny;
                posBuf[idxPositions++] = ry;
                normBuf[idxPositions] = rnz;
                posBuf[idxPositions++] = rz;

                // overwrite vertex with transformed pos
    //            fvb.position(fvb.position()-3);
    //            fvb.put(rx).put(ry).put(rz);
    //
    //            fnb.position(fnb.position()-3);
    //            fnb.put(rnx).put(rny).put(rnz);
            }


            fvb.position(fvb.position()-bufLength);
            fvb.put(posBuf, 0, bufLength);
            fnb.position(fnb.position()-bufLength);
            fnb.put(normBuf, 0, bufLength);
        }

        vb.updateData(fvb);
        nb.updateData(fnb);

//        mesh.updateBound();
    }

    @Override
    public void write(JmeExporter ex) throws IOException{
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(targets, "targets", null);
        oc.write(skeleton, "skeleton", null);
        oc.writeStringSavableMap(animationMap, "animations", null);
    }

    @Override
    public void read(JmeImporter im) throws IOException{
        super.read(im);
        InputCapsule in = im.getCapsule(this);
        Savable[] sav = in.readSavableArray("targets", null);
        if (sav != null){
            targets = new Mesh[sav.length];
            System.arraycopy(sav, 0, targets, 0, sav.length);
        }
        skeleton = (Skeleton) in.readSavable("skeleton", null);
        animationMap = (HashMap<String, BoneAnimation>) in.readStringSavableMap("animations", null);
    }

}
