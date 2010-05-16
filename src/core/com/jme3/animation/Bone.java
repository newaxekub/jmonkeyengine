/*
 * Copyright (c) 2003-2009 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jme3.animation;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.util.TempVars;
import java.io.IOException;
import java.util.ArrayList;

/**
 * TODO: Add better documentation to this class.
 * @author Kirill Vainer
 */
public final class Bone implements Savable {

    private String name;

    private Bone parent;
    private final ArrayList<Bone> children = new ArrayList<Bone>();

    /**
     * If enabled, user can control bone transform with setUserTransforms.
     * Animation transforms are not applied to this bone when enabled.
     */
    private boolean userControl = false;

    /**
     * The attachment node.
     */
    private Node attachNode;

    /**
     * Initial transform is the local bind transform of this bone.
     * PARENT SPACE -> BONE SPACE
     */
    private Vector3f initialPos;
    private Quaternion initialRot;

    /**
     * The inverse world bind transform.
     * BONE SPACE -> MODEL SPACE
     */
    private Vector3f worldBindInversePos;
    private Quaternion worldBindInverseRot;

    /**
     * The local animated transform combined with the local bind transform and parent world transform
     */
    private Vector3f localPos = new Vector3f();
    private Quaternion localRot = new Quaternion();

    /**
     * MODEL SPACE -> BONE SPACE (in animated state)
     */
    private Vector3f worldPos = new Vector3f();
    private Quaternion worldRot = new Quaternion();

    /**
     * Creates a new bone
     * @param name Name to give to this bone
     */
    public Bone(String name){
        this.name = name;

        initialPos = new Vector3f();
        initialRot = new Quaternion();

        worldBindInversePos = new Vector3f();
        worldBindInverseRot = new Quaternion();
    }

    /**
     * Copy constructor. local bind and world inverse bind transforms shallow copied.
     * @param source
     */
    Bone(Bone source){
        this.name = source.name;

        userControl = source.userControl;

        initialPos = source.initialPos;
        initialRot = source.initialRot;

        worldBindInversePos = source.worldBindInversePos;
        worldBindInverseRot = source.worldBindInverseRot;

        // parent and children will be assigned manually..
    }

    /**
     * Used for binary loading as a Savable; the object must be constructed,
     * then the parameters usually present in the constructor for this class are
     * restored from the file the object was saved to.
     */
    public Bone() {
    }

    public String getName(){
        return name;
    }

    public Bone getParent(){
        return parent;
    }

    public ArrayList<Bone> getChildren(){
        return children;
    }

    public Vector3f getLocalPosition() {
        return localPos;
    }

    public Quaternion getLocalRotation() {
        return localRot;
    }

    public Vector3f getWorldPosition() {
        return worldPos;
    }

    public Quaternion getWorldRotation() {
        return worldRot;
    }

    /**
     * If enabled, user can control bone transform with setUserTransforms.
     * Animation transforms are not applied to this bone when enabled.
     */
    public void setUserControl(boolean enable){
        userControl = enable;
    }

    public void addChild(Bone bone) {
        children.add(bone);
        bone.parent = this;
    }

    /**
     * Updates the world transforms for this bone, and, possibly the attach node if not null.
     */
    final void updateWorldVectors(){
        if (parent != null){
            // worldRot = localRot * parentWorldRot

            parent.worldRot.mult(localRot, worldRot);
//            worldRot = parent.worldRot.mult(localRot);
//            worldRot = parent.worldRot.mult(localRot, worldRot);


            // worldPos = parentWorldPos + (parentWorldRot * localPos)
            parent.worldRot.mult(localPos, worldPos);
//            worldPos = parent.worldRot.mult(localPos);
            worldPos.addLocal(parent.worldPos);
        }else{
            worldRot.set(localRot);
            worldPos.set(localPos);
        }

        if (attachNode != null){
            attachNode.setLocalTranslation(worldPos);
            attachNode.setLocalRotation(worldRot);
        }
    }

    /**
     * Updates world transforms for this bone and it's children.
     */
    final void update(){
        updateWorldVectors();

        for (int i = children.size() - 1; i >= 0; i--)
            children.get(i).update();
    }

    /**
     * Saves the current bone state as it's binding pose, including it's children.
     */
    void setBindingPose(){
        initialPos.set(localPos);
        initialRot.set(localRot);

        if (worldBindInversePos == null){
            worldBindInversePos = new Vector3f();
            worldBindInverseRot = new Quaternion();
        }
        
        // Save inverse derived position/scale/orientation, used for calculate offset transform later
        worldBindInversePos.set(worldPos);
        worldBindInversePos.negateLocal();

        worldBindInverseRot.set(worldRot);
        worldBindInverseRot.inverseLocal();

        for (Bone b : children)
            b.setBindingPose();
    }

    /**
     * Reset the bone and it's children to bind pose.
     */
    final void reset(){
        if (!userControl){
            localPos.set(initialPos);
            localRot.set(initialRot);
        }

        for (int i = children.size() - 1; i >= 0; i--)
            children.get(i).reset();
    }

    /**
     * Stores the skinning transform in the specified Matrix4f.
     * The skinning transform applies the animation of the bone to a vertex.
     * @param m
     */
    void getOffsetTransform(Matrix4f m, Quaternion tmp1, Vector3f tmp2){
        Quaternion rotate = worldRot.mult(worldBindInverseRot, tmp1);
        Vector3f translate = worldPos.add(rotate.mult(worldBindInversePos, tmp2), tmp2);

        m.loadIdentity();
        m.setTranslation(translate);
        m.setRotationQuaternion(rotate);
    }

    /**
     * Set user transform.
     * @see setUserControl
     */
    public void setUserTransforms(Vector3f translation, Quaternion rotation, Vector3f scale){
        if (!userControl)
            throw new IllegalStateException("User control must be on bone to allow user transforms");

        localPos.set(initialPos);
        localRot.set(initialRot);
        localPos.addLocal(translation);
        localRot = localRot.mult(rotation);
    }

    /**
     * Must update all bones in skeleton for this to work.
     * @param translation
     * @param rotation
     */
    public void setUserTransformsWorld(Vector3f translation, Quaternion rotation){
        if (!userControl)
            throw new IllegalStateException("User control must be on bone to allow user transforms");

        worldPos.set(translation);
        worldRot.set(rotation);
    }

    /**
     * Returns the attachment node.
     * Attach models and effects to this node to make
     * them follow this bone's motions.
     */
    public Node getAttachmentsNode(){
        if (attachNode == null){
            attachNode = new Node(name+"_attachnode");
        }
        return attachNode;
    }

    /**
     * Sets the local animation transform of this bone.
     * Bone is assumed to be in bind pose when this is called.
     */
    void setAnimTransforms(Vector3f translation, Quaternion rotation, Vector3f scale){
        if (userControl)
            return;

//        localPos.addLocal(translation);
//        localRot.multLocal(rotation);
        //localRot = localRot.mult(rotation);

        localPos.set(initialPos).addLocal(translation);
        localRot.set(initialRot).multLocal(rotation);
    }

    void blendAnimTransforms(Vector3f translation, Quaternion rotation, float weight){
        if (userControl)
            return;

        TempVars vars = TempVars.get();
        assert vars.lock();

        Vector3f tmpV = vars.vect1;
        Quaternion tmpQ = vars.quat1;

        tmpV.set(initialPos).addLocal(translation);
        localPos.interpolate(tmpV, weight);

        tmpQ.set(initialRot).multLocal(rotation);
        localRot.slerp(tmpQ, weight);
        //localRot = localRot.mult(rotation);

        assert vars.unlock();
    }

    /**
     * Sets local bind transform for bone.
     * Call setBindingPose() after all of the skeleton bones' bind transforms are set to save them.
     */
    public void setBindTransforms(Vector3f translation, Quaternion rotation, Vector3f scale){
        initialPos.set(translation);
        initialRot.set(rotation);

        localPos.set(translation);
        localRot.set(rotation);
    }

    void setAnimTransforms(Vector3f translation, Quaternion rotation){
        setAnimTransforms(translation, rotation, Vector3f.UNIT_XYZ);
    }

    public void setBindTransforms(Vector3f translation, Quaternion rotation){
        setBindTransforms(translation, rotation, Vector3f.UNIT_XYZ);
    }

    private String toString(int depth){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++)
            sb.append('-');

        sb.append(name).append(" bone\n");
        for (Bone child : children){
            sb.append(child.toString(depth+1));
        }
        return sb.toString();
    }

    public String toString(){
        return toString(0);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule input = im.getCapsule(this);

        name = input.readString("name", null);
        initialPos = (Vector3f) input.readSavable("initialPos", null);
        initialRot = (Quaternion) input.readSavable("initialRot", null);
        attachNode = (Node) input.readSavable("attachNode", null);

        localPos.set(initialPos);
        localRot.set(initialRot);

        ArrayList<Bone> childList = input.readSavableArrayList("children", null);
        for (int i = childList.size() - 1; i >= 0; i--){
            addChild(childList.get(i));
        }

        // NOTE: Parent skeleton will call update() then setBindingPose()
        // after Skeleton has been de-serialized.
        // Therefore, worldBindInversePos and worldBindInverseRot
        // will be reconstructed based on that information.
    }

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule output = ex.getCapsule(this);

        output.write(name, "name", null);
        output.write(attachNode, "attachNode", null);
        output.write(initialPos, "initialPos", null);
        output.write(initialRot, "initialRot", null);
        output.writeSavableArrayList(children, "children", null);
    }

}
