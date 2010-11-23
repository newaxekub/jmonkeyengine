/*
 * Copyright (c) 2009-2010 jMonkeyEngine
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
package com.jme3.bullet.nodes;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.nodes.infos.PhysicsNodeState;
import com.jme3.bullet.util.Converter;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Matrix3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>PhysicsNode - Basic physics object</p>
 * @author normenhansen
 */
public class PhysicsNode extends PhysicsCollisionObject {

    protected RigidBodyConstructionInfo constructionInfo;
    protected RigidBody rBody;
    protected PhysicsNodeState motionState = new PhysicsNodeState();
    protected boolean rebuildBody = true;
    protected float mass = 1.0f;
    protected boolean kinematic = false;
    protected javax.vecmath.Vector3f tempVec = new javax.vecmath.Vector3f();
    protected javax.vecmath.Vector3f tempVec2 = new javax.vecmath.Vector3f();
    protected Transform tempTrans = new Transform(new javax.vecmath.Matrix3f());
    protected javax.vecmath.Matrix3f tempMatrix = new javax.vecmath.Matrix3f();
    //jme-specific
    protected Vector3f continuousForce = new Vector3f();
    protected Vector3f continuousForceLocation = new Vector3f();
    protected Vector3f continuousTorque = new Vector3f();
    //TEMP VARIABLES
    private javax.vecmath.Vector3f localInertia = new javax.vecmath.Vector3f();
    protected boolean applyForce = false;
    protected boolean applyTorque = false;
    private ArrayList<PhysicsJoint> joints = new ArrayList<PhysicsJoint>();

    public PhysicsNode() {
    }

    /**
     * Creates a new PhysicsNode with the supplied collision shape
     * @param child
     * @param shape
     */
    public PhysicsNode(CollisionShape shape) {
        collisionShape = shape;
        rebuildRigidBody();
    }

    public PhysicsNode(CollisionShape shape, float mass) {
        collisionShape = shape;
        this.mass = mass;
        rebuildRigidBody();
    }

    /**
     * Creates a new PhysicsNode with the supplied child node or geometry and
     * sets the supplied collision shape to the PhysicsNode
     * @param child
     * @param shape
     */
    public PhysicsNode(Spatial child, CollisionShape shape) {
        this(child, shape, 1.0f);
    }

    /**
     * Creates a new PhysicsNode with the supplied child node or geometry and
     * uses the supplied collision shape for that PhysicsNode<br>
     * @param child
     * @param shape
     */
    public PhysicsNode(Spatial child, CollisionShape shape, float mass) {
        if (child != null) {
            this.attachChild(child);
        }
        this.mass = mass;
        this.collisionShape = shape;
        rebuildRigidBody();
    }

    /**
     * Builds/rebuilds the phyiscs body when parameters have changed
     */
    protected void rebuildRigidBody() {
        boolean removed = false;
        if (rBody != null) {
            if (rBody.isInWorld()) {
                PhysicsSpace.getPhysicsSpace().remove(this);
                removed = true;
            }
            rBody.destroy();
        }
        preRebuild();
        rBody = new RigidBody(constructionInfo);
        postRebuild();
        if (removed) {
            PhysicsSpace.getPhysicsSpace().add(this);
        }
        rebuildBody = false;
    }

    protected void preRebuild() {
        collisionShape.calculateLocalInertia(mass, localInertia);
        if (constructionInfo == null) {
            constructionInfo = new RigidBodyConstructionInfo(mass, motionState, collisionShape.getCShape(), localInertia);
        } else {
            constructionInfo.mass = mass;
            constructionInfo.collisionShape = collisionShape.getCShape();
            constructionInfo.motionState = motionState;
        }
    }

    protected void postRebuild() {
        rBody.setUserPointer(this);
        if (mass == 0.0f) {
            rBody.setCollisionFlags(rBody.getCollisionFlags() | CollisionFlags.STATIC_OBJECT);
        } else {
            rBody.setCollisionFlags(rBody.getCollisionFlags() & ~CollisionFlags.STATIC_OBJECT);
        }
    }

    @Override
    protected void setTransformRefresh() {
        super.setTransformRefresh();
        refreshFlags |= RF_PHYSICS;
    }

    @Override
    public void updateGeometricState() {
        if ((refreshFlags & RF_LIGHTLIST) != 0) {
            updateWorldLightList();
        }

        if ((refreshFlags & RF_TRANSFORM) != 0) {
            // combine with parent transforms- same for all spatial
            // subclasses.
            updateWorldTransforms();
            motionState.setWorldTransform(getWorldTranslation(), getWorldRotation());
            refreshFlags &= ~RF_PHYSICS;
        } else if ((refreshFlags & RF_PHYSICS) != 0) {
            motionState.setWorldTransform(getWorldTranslation(), getWorldRotation());
            refreshFlags &= ~RF_PHYSICS;
        } else if (motionState.applyTransform(this)) {
            updateWorldTransforms();
            refreshFlags &= ~RF_PHYSICS;
        }

        // the important part- make sure child geometric state is refreshed
        // first before updating own world bound. This saves
        // a round-trip later on.
        for (int i = 0, cSize = children.size(); i < cSize; i++) {
            Spatial child = children.get(i);
            child.updateGeometricState();
        }

        if ((refreshFlags & RF_BOUND) != 0) {
            updateWorldBound();
        }

        //only called to sync debug shapes in CollisionObject
        super.updateGeometricState();
    }

    /**
     * Only to be called from physics thread!!
     */
    @Override
    public void updatePhysicsState() {
        if (rebuildBody) {
            rebuildRigidBody();
        }
        motionState.applyTransform(rBody);
        synchronized (this) {
            if (applyForce) {
                rBody.applyForce(Converter.convert(continuousForce, tempVec), Converter.convert(continuousForceLocation, tempVec2));
            }
            if (applyTorque) {
                rBody.applyTorque(Converter.convert(continuousTorque, tempVec));
            }
        }
    }

    /**
     * This is normally only needed when using detached physics
     * @param location the location of the actual physics object
     */
    public void setPhysicsLocation(Vector3f location) {
        rBody.getWorldTransform(tempTrans);
        Converter.convert(location, tempTrans.origin);
        rBody.setWorldTransform(tempTrans);
    }

    /**
     * This is normally only needed when using detached physics
     * @param rotation the rotation of the actual physics object
     */
    public void setPhysicsRotation(Matrix3f rotation) {
        rBody.getWorldTransform(tempTrans);
        Converter.convert(rotation, tempTrans.basis);
        rBody.setWorldTransform(tempTrans);
    }

    /**
     * This is normally only needed when using detached physics
     * @param location the location of the actual physics object is stored in this Vector3f
     */
    public void getPhysicsLocation(Vector3f location) {
        rBody.getWorldTransform(tempTrans);
        Converter.convert(tempTrans.origin, location);
    }

    /**
     * This is normally only needed when using detached physics
     * @param rotation the rotation of the actual physics object is stored in this Matrix3f
     */
    public void getPhysicsRotation(Matrix3f rotation) {
        rBody.getWorldTransform(tempTrans);
        Converter.convert(tempTrans.basis, rotation);
    }

    /**
     * Sets the node to kinematic mode. in this mode the node is not affected by physics
     * but affects other physics objects. Iits kinetic force is calculated by the amount
     * of movement it is exposed to and its weight.
     * @param kinematic
     */
    public void setKinematic(boolean kinematic) {
        this.kinematic = kinematic;
        if (kinematic) {
            rBody.setCollisionFlags(rBody.getCollisionFlags() | CollisionFlags.KINEMATIC_OBJECT);
            rBody.setActivationState(com.bulletphysics.collision.dispatch.CollisionObject.DISABLE_DEACTIVATION);
        } else {
            rBody.setCollisionFlags(rBody.getCollisionFlags() & ~CollisionFlags.KINEMATIC_OBJECT);
            rBody.setActivationState(com.bulletphysics.collision.dispatch.CollisionObject.ACTIVE_TAG);
        }
    }

    public boolean isKinematic() {
        return kinematic;
    }

    public void setCcdSweptSphereRadius(float radius) {
        rBody.setCcdSweptSphereRadius(radius);
    }

    /**
     * Sets the amount of motion that has to happen in one physics tick to trigger the continuous motion detection<br/>
     * Set to zero to disable (default)
     * @param threshold
     */
    public void setCcdMotionThreshold(float threshold) {
        rBody.setCcdMotionThreshold(threshold);
    }

    public float getCcdSweptSphereRadius() {
        return rBody.getCcdSweptSphereRadius();
    }

    public float getCcdMotionThreshold() {
        return rBody.getCcdMotionThreshold();
    }

    public float getCcdSquareMotionThreshold() {
        return rBody.getCcdSquareMotionThreshold();
    }

    public float getMass() {
        return mass;
    }

    /**
     * Sets the mass of this PhysicsNode, objects with mass=0 are static.
     * @param mass
     */
    public void setMass(float mass) {
        this.mass = mass;
        if (collisionShape != null) {
            collisionShape.calculateLocalInertia(mass, localInertia);
        }
        if (rBody != null) {
            rBody.setMassProps(mass, localInertia);
            if (mass == 0.0f) {
                rBody.setCollisionFlags(rBody.getCollisionFlags() | CollisionFlags.STATIC_OBJECT);
            } else {
                rBody.setCollisionFlags(rBody.getCollisionFlags() & ~CollisionFlags.STATIC_OBJECT);
            }
        }
    }

    public Vector3f getGravity() {
        return getGravity(null);
    }

    public Vector3f getGravity(Vector3f gravity) {
        if (gravity == null) {
            gravity = new Vector3f();
        }
        rBody.getGravity(tempVec);
        return Converter.convert(tempVec, gravity);
    }

    /**
     * Set the local gravity of this PhysicsNode<br/>
     * Set this after adding the node to the PhysicsSpace,
     * the PhysicsSpace assigns its current gravity to the physics node when its added.
     * @param gravity the gravity vector to set
     */
    public void setGravity(Vector3f gravity) {
        rBody.setGravity(Converter.convert(gravity, tempVec));
    }

    public float getFriction() {
        return rBody.getFriction();
    }

    /**
     * Sets the friction of this physics object
     * @param friction the friction of this physics object
     */
    public void setFriction(float friction) {
        constructionInfo.friction = friction;
        rBody.setFriction(friction);
    }

    public void setDamping(float linearDamping, float angularDamping) {
        constructionInfo.linearDamping = linearDamping;
        constructionInfo.angularDamping = angularDamping;
        rBody.setDamping(linearDamping, angularDamping);
    }

    public void setLinearDamping(float linearDamping) {
        constructionInfo.linearDamping = linearDamping;
        rBody.setDamping(linearDamping, constructionInfo.angularDamping);
    }

    public void setAngularDamping(float angularDamping) {
        constructionInfo.angularDamping = angularDamping;
        rBody.setDamping(constructionInfo.linearDamping, angularDamping);
    }

    public float getLinearDamping() {
        return constructionInfo.linearDamping;
    }

    public float getAngularDamping() {
        return constructionInfo.angularDamping;
    }

    public float getRestitution() {
        return rBody.getRestitution();
    }

    /**
     * The "bouncyness" of the PhysicsNode, best performance if restitution=0
     * @param restitution
     */
    public void setRestitution(float restitution) {
        constructionInfo.restitution = restitution;
        rBody.setRestitution(restitution);
    }

    /**
     * Get the current angular velocity of this PhysicsNode
     * @return the current linear velocity
     */
    public Vector3f getAngularVelocity() {
        return Converter.convert(rBody.getAngularVelocity(tempVec));
    }

    /**
     * Get the current angular velocity of this PhysicsNode
     * @param vec the vector to store the velocity in
     */
    public void getAngularVelocity(Vector3f vec) {
        Converter.convert(rBody.getAngularVelocity(tempVec), vec);
    }

    /**
     * Sets the angular velocity of this PhysicsNode
     * @param vec the angular velocity of this PhysicsNode
     */
    public void setAngularVelocity(Vector3f vec) {
        rBody.setAngularVelocity(Converter.convert(vec, tempVec));
        rBody.activate();
    }

    /**
     * Get the current linear velocity of this PhysicsNode
     * @return the current linear velocity
     */
    public Vector3f getLinearVelocity() {
        return Converter.convert(rBody.getLinearVelocity(tempVec));
    }

    /**
     * Get the current linear velocity of this PhysicsNode
     * @param vec the vector to store the velocity in
     */
    public void getLinearVelocity(Vector3f vec) {
        Converter.convert(rBody.getLinearVelocity(tempVec), vec);
    }

    /**
     * Sets the linear velocity of this PhysicsNode
     * @param vec the linear velocity of this PhysicsNode
     */
    public void setLinearVelocity(Vector3f vec) {
        rBody.setLinearVelocity(Converter.convert(vec, tempVec));
        rBody.activate();
    }

    /**
     * Get the currently applied continuous force
     * @param vec the vector to store the continuous force in
     * @return null if no force is applied
     */
    public synchronized Vector3f getContinuousForce(Vector3f vec) {
        if (applyForce) {
            return vec.set(continuousForce);
        } else {
            return null;
        }
    }

    /**
     * get the currently applied continuous force
     * @return null if no force is applied
     */
    public synchronized Vector3f getContinuousForce() {
        if (applyForce) {
            return continuousForce;
        } else {
            return null;
        }
    }

    /**
     * Get the currently applied continuous force location
     * @return null if no force is applied
     */
    public synchronized Vector3f getContinuousForceLocation() {
        if (applyForce) {
            return continuousForceLocation;
        } else {
            return null;
        }
    }

    /**
     * Apply a continuous force to this PhysicsNode, the force is updated automatically each
     * tick so you only need to set it once and then set it to false to stop applying
     * the force.
     * @param apply true if the force should be applied each physics tick
     * @param force the vector of the force to apply
     */
    public synchronized void applyContinuousForce(boolean apply, Vector3f force) {
        if (force != null) {
            continuousForce.set(force);
        }
        continuousForceLocation.set(0, 0, 0);
        applyForce = apply;

    }

    /**
     * Apply a continuous force to this PhysicsNode, the force is updated automatically each
     * tick so you only need to set it once and then set it to false to stop applying
     * the force.
     * @param apply true if the force should be applied each physics tick
     * @param force the offset of the force
     */
    public synchronized void applyContinuousForce(boolean apply, Vector3f force, Vector3f location) {
        if (force != null) {
            continuousForce.set(force);
        }
        if (location != null) {
            continuousForceLocation.set(location);
        }
        applyForce = apply;

    }

    /**
     * Use to enable/disable continuous force
     * @param apply set to false to disable
     */
    public synchronized void applyContinuousForce(boolean apply) {
        applyForce = apply;
    }

    /**
     * Get the currently applied continuous torque
     * @return null if no torque is applied
     */
    public synchronized Vector3f getContinuousTorque() {
        if (applyTorque) {
            return continuousTorque;
        } else {
            return null;
        }
    }

    /**
     * Get the currently applied continuous torque
     * @param vec the vector to store the continuous torque in
     * @return null if no torque is applied
     */
    public synchronized Vector3f getContinuousTorque(Vector3f vec) {
        if (applyTorque) {
            return vec.set(continuousTorque);
        } else {
            return null;
        }
    }

    /**
     * Apply a continuous torque to this PhysicsNode. The torque is updated automatically each
     * tick so you only need to set it once and then set it to false to stop applying
     * the torque.
     * @param apply true if the force should be applied each physics tick
     * @param vec the vector of the force to apply
     */
    public synchronized void applyContinuousTorque(boolean apply, Vector3f vec) {
        if (vec != null) {
            continuousTorque.set(vec);
        }
        applyTorque = apply;
    }

    /**
     * Use to enable/disable continuous torque
     * @param apply set to false to disable
     */
    public synchronized void applyContinuousTorque(boolean apply) {
        applyTorque = apply;
    }

    /**
     * Apply a force to the PhysicsNode, only applies force if the next physics update call
     * updates the physics space.<br>
     * To apply an impulse, use applyImpulse, use applyContinuousForce to apply continuous force.
     * @param force the force
     * @param location the location of the force
     */
    public void applyForce(final Vector3f force, final Vector3f location) {
        rBody.applyForce(Converter.convert(force, tempVec), Converter.convert(location, tempVec2));
        rBody.activate();
    }

    /**
     * Apply a force to the PhysicsNode, only applies force if the next physics update call
     * updates the physics space.<br>
     * To apply an impulse, use applyImpulse, use applyContinuousForce to apply continuous force.
     * 
     * @param force the force
     */
    public void applyCentralForce(final Vector3f force) {
        rBody.applyCentralForce(Converter.convert(force, tempVec));
        rBody.activate();
    }

    /**
     * Apply a force to the PhysicsNode, only applies force if the next physics update call
     * updates the physics space.<br>
     * To apply an impulse, use applyImpulse, use applyContinuousForce to apply continuous force.
     * 
     * @param torque the torque
     */
    public void applyTorque(final Vector3f torque) {
        rBody.applyTorque(Converter.convert(torque, tempVec));
        rBody.activate();
    }

    /**
     * Apply an impulse to the PhysicsNode in the next physics update.
     * @param impulse applied impulse
     * @param rel_pos location relative to object
     */
    public void applyImpulse(final Vector3f impulse, final Vector3f rel_pos) {
        rBody.applyImpulse(Converter.convert(impulse, tempVec), Converter.convert(rel_pos, tempVec2));
        rBody.activate();
    }

    /**
     * Apply a torque impulse to the PhysicsNode in the next physics update.
     * @param vec
     */
    public void applyTorqueImpulse(final Vector3f vec) {
        rBody.applyTorqueImpulse(Converter.convert(vec, tempVec));
        rBody.activate();
    }

    /**
     * Clear all forces from the PhysicsNode
     * 
     */
    public void clearForces() {
        rBody.clearForces();
    }

    public void setCollisionShape(CollisionShape collisionShape) {
        super.setCollisionShape(collisionShape);
        rebuildRigidBody();
    }

    /**
     * reactivates this PhysicsNode when it has been deactivated because it was not moving
     */
    public void activate() {
        rBody.activate();
    }

    public boolean isActive() {
        return rBody.isActive();
    }

    /**
     * sets the sleeping thresholds, these define when the object gets deactivated
     * to save ressources. Low values keep the object active when it barely moves
     * @param linear the linear sleeping threshold
     * @param angular the angular sleeping threshold
     */
    public void setSleepingThresholds(float linear, float angular) {
        constructionInfo.linearSleepingThreshold = linear;
        constructionInfo.angularSleepingThreshold = angular;
        rBody.setSleepingThresholds(linear, angular);
    }

    public void setLinearSleepingThreshold(float linearSleepingThreshold) {
        constructionInfo.linearSleepingThreshold = linearSleepingThreshold;
        rBody.setSleepingThresholds(linearSleepingThreshold, constructionInfo.angularSleepingThreshold);
    }

    public void setAngularSleepingThreshold(float angularSleepingThreshold) {
        constructionInfo.angularSleepingThreshold = angularSleepingThreshold;
        rBody.setSleepingThresholds(constructionInfo.linearSleepingThreshold, angularSleepingThreshold);
    }

    public float getLinearSleepingThreshold() {
        return constructionInfo.linearSleepingThreshold;
    }

    public float getAngularSleepingThreshold() {
        return constructionInfo.angularSleepingThreshold;
    }

    /**
     * do not use manually, joints are added automatically
     */
    public void addJoint(PhysicsJoint joint) {
        if (!joints.contains(joint)) {
            joints.add(joint);
        }
        updateDebugShape();
    }

    /**
     * 
     */
    public void removeJoint(PhysicsJoint joint) {
        joints.remove(joint);
    }

    /**
     * Returns a list of connected joints. This list is only filled when
     * the PhysicsNode is actually added to the physics space or loaded from disk.
     * @return list of active joints connected to this physicsnode
     */
    public List<PhysicsJoint> getJoints() {
        return joints;
    }

    /**
     * used internally
     */
    public RigidBody getRigidBody() {
        return rBody;
    }

    /**
     * destroys this PhysicsNode and removes it from memory
     */
    public void destroy() {
        rBody.destroy();
    }

    @Override
    protected Spatial getDebugShape() {
        //add joints
        Spatial shape = super.getDebugShape();
        Node node = null;
        if (shape instanceof Node) {
            node = (Node) shape;
        } else {
            node = new Node("DebugShapeNode");
            node.attachChild(shape);
        }
        for (Iterator<PhysicsJoint> it = joints.iterator(); it.hasNext();) {
            PhysicsJoint physicsJoint = it.next();
            Vector3f pivot=null;
            if(physicsJoint.getNodeA()==this){
                pivot=physicsJoint.getPivotA();
            }else{
                pivot=physicsJoint.getPivotB();
            }
            Arrow arrow=new Arrow(pivot);
            Geometry geom=new Geometry("DebugBone",arrow);
            geom.setMaterial(debugMaterialGreen);
            node.attachChild(geom);
        }
        return node;
    }

    @Override
    public void write(JmeExporter e) throws IOException {
        super.write(e);
        OutputCapsule capsule = e.getCapsule(this);

        capsule.write(getMass(), "mass", 1.0f);

        capsule.write(collisionShape, "collisionShape", null);

        capsule.write(getGravity(), "gravity", Vector3f.ZERO);
        capsule.write(getFriction(), "friction", 0.5f);
        capsule.write(getRestitution(), "restitution", 0);
        capsule.write(kinematic, "kinematic", false);

        capsule.write(constructionInfo.linearDamping, "linearDamping", 0);
        capsule.write(constructionInfo.angularDamping, "angularDamping", 0);
        capsule.write(constructionInfo.linearSleepingThreshold, "linearSleepingThreshold", 0.8f);
        capsule.write(constructionInfo.angularSleepingThreshold, "angularSleepingThreshold", 1.0f);

        capsule.write(continuousForce, "continuousForce", Vector3f.ZERO);
        capsule.write(continuousForceLocation, "continuousForceLocation", Vector3f.ZERO);
        capsule.write(continuousTorque, "continuousTorque", Vector3f.ZERO);
        capsule.write(applyForce, "applyForce", false);
        capsule.write(applyTorque, "applyTorque", false);

        capsule.writeSavableArrayList(joints, "joints", null);
    }

    @Override
    public void read(JmeImporter e) throws IOException {
        super.read(e);

        InputCapsule capsule = e.getCapsule(this);
        float mass = capsule.readFloat("mass", 1.0f);
        this.mass = mass;
        CollisionShape shape = (CollisionShape) capsule.readSavable("collisionShape", new BoxCollisionShape(Vector3f.UNIT_XYZ));
        collisionShape = shape;
        rebuildRigidBody();
        setGravity((Vector3f) capsule.readSavable("gravity", Vector3f.ZERO.clone()));
        setFriction(capsule.readFloat("friction", 0.5f));
        setKinematic(capsule.readBoolean("kinematic", false));

        setRestitution(capsule.readFloat("restitution", 0));
        setDamping(capsule.readFloat("linearDamping", 0), capsule.readFloat("angularDamping", 0));
        setSleepingThresholds(capsule.readFloat("linearSleepingThreshold", 0.8f), capsule.readFloat("angularSleepingThreshold", 1.0f));


        continuousForce = (Vector3f) capsule.readSavable("continuousForce", Vector3f.ZERO.clone());
        continuousForceLocation = (Vector3f) capsule.readSavable("continuousForceLocation", Vector3f.ZERO.clone());
        applyForce = capsule.readBoolean("applyForce", false);

        continuousTorque = (Vector3f) capsule.readSavable("continuousTorque", Vector3f.ZERO.clone());
        applyTorque = capsule.readBoolean("applyTorque", false);

        joints = capsule.readSavableArrayList("joints", null);
    }
}
