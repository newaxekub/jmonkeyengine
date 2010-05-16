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
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.dynamics.character.KinematicCharacterController;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.util.Converter;

/**
 *
 * @author normenhansen
 */
public class PhysicsCharacterNode extends PhysicsGhostNode {

    private KinematicCharacterController character;
    private float stepHeight;
    private Vector3f walkDirection = new Vector3f();

    public PhysicsCharacterNode(CollisionShape shape, float stepHeight) {
        super(shape);
        if (!(shape instanceof SphereCollisionShape)) {
            throw (new UnsupportedOperationException("Kinematic character nodes can only have sphere collision shapes"));
        }
        this.stepHeight = stepHeight;
        character = new KinematicCharacterController(gObject, (ConvexShape) cShape.getCShape(), stepHeight);
    }

    public PhysicsCharacterNode(Spatial spat, CollisionShape shape, float stepHeight) {
        super(spat, shape);
        if (!(shape instanceof SphereCollisionShape)) {
            throw (new UnsupportedOperationException("Kinematic character nodes can only have sphere collision shapes"));
        }
        this.stepHeight = stepHeight;
        character = new KinematicCharacterController(gObject, (ConvexShape) cShape.getCShape(), stepHeight);
    }

    @Override
    protected void buildObject() {
        super.buildObject();
        gObject.setCollisionFlags(gObject.getCollisionFlags() & ~CollisionFlags.NO_CONTACT_RESPONSE);
        gObject.setCollisionFlags(gObject.getCollisionFlags() | CollisionFlags.CHARACTER_OBJECT);
    }

    public void warp(Vector3f location) {
        character.warp(Converter.convert(location));
    }

    /**
     * set the walk direction, works continuously
     * @param vec the walk direction to set
     */
    public void setWalkDirection(Vector3f vec) {
        walkDirection.set(vec);
        character.setWalkDirection(Converter.convert(walkDirection));
    }

    public void setUpAxis(int axis) {
        character.setUpAxis(axis);
    }

    public void setFallSpeed(float fallSpeed) {
        character.setFallSpeed(fallSpeed);
    }

    public void setJumpSpeed(float jumpSpeed) {
        character.setJumpSpeed(jumpSpeed);
    }

    public void setMaxJumpHeight(float height) {
        character.setMaxJumpHeight(height);
    }

    public void setGravity(float value) {
        character.setGravity(value);
    }

    public float getGravity() {
        return character.getGravity();
    }

    public void setMaxSlope(float slopeRadians) {
        character.setMaxSlope(slopeRadians);
    }

    public float getMaxSlope() {
        return character.getMaxSlope();
    }

    public boolean onGround() {
        return character.onGround();
    }

    public void jump() {
        character.jump();
    }

    /**
     * used internally
     */
    public KinematicCharacterController getCharacterController() {
        return character;
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
