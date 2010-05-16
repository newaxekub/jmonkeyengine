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
package com.jme3.bullet.joints;

import com.bulletphysics.dynamics.constraintsolver.Generic6DofConstraint;
import com.bulletphysics.linearmath.Transform;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.bullet.joints.motors.RotationalLimitMotor;
import com.jme3.bullet.joints.motors.TranslationalLimitMotor;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.bullet.util.Converter;
import java.util.LinkedList;

/**
 * <i>From bullet manual:</i><br>
 * This generic constraint can emulate a variety of standard constraints,
 * by configuring each of the 6 degrees of freedom (dof).
 * The first 3 dof axis are linear axis, which represent translation of rigidbodies,
 * and the latter 3 dof axis represent the angular motion. Each axis can be either locked,
 * free or limited. On construction of a new btGeneric6DofConstraint, all axis are locked.
 * Afterwards the axis can be reconfigured. Note that several combinations that
 * include free and/or limited angular degrees of freedom are undefined.
 * @author normenhansen
 */
public class Physics6DofJoint extends PhysicsJoint{
    private LinkedList<RotationalLimitMotor> rotationalMotors=new LinkedList<RotationalLimitMotor>();
    private TranslationalLimitMotor translationalMotor;

    public Physics6DofJoint(PhysicsNode nodeA, PhysicsNode nodeB, Vector3f pivotA, Vector3f pivotB, Matrix3f rotA, Matrix3f rotB, boolean useLinearReferenceFrameA) {
        super(nodeA, nodeB, pivotA, pivotB);

        Transform transA=new Transform(Converter.convert(rotA));
        Converter.convert(pivotA,transA.origin);
        Converter.convert(rotA,transA.basis);

        Transform transB=new Transform(Converter.convert(rotB));
        Converter.convert(pivotB,transB.origin);
        Converter.convert(rotB,transB.basis);

        constraint=new Generic6DofConstraint(nodeA.getRigidBody(), nodeB.getRigidBody(), transA, transB, useLinearReferenceFrameA);
        gatherMotors();
    }

    public Physics6DofJoint(PhysicsNode nodeA, PhysicsNode nodeB, Vector3f pivotA, Vector3f pivotB, boolean useLinearReferenceFrameA) {
        super(nodeA, nodeB, pivotA, pivotB);

        Transform transA=new Transform(Converter.convert(new Matrix3f()));
        Converter.convert(pivotA,transA.origin);

        Transform transB=new Transform(Converter.convert(new Matrix3f()));
        Converter.convert(pivotB,transB.origin);

        constraint=new Generic6DofConstraint(nodeA.getRigidBody(), nodeB.getRigidBody(), transA, transB, useLinearReferenceFrameA);
        gatherMotors();
    }

    private void gatherMotors(){
        for (int i = 0; i < 3; i++) {
            RotationalLimitMotor rmot=new RotationalLimitMotor(((Generic6DofConstraint)constraint).getRotationalLimitMotor(i));
            rotationalMotors.add(rmot);
        }
        translationalMotor=new TranslationalLimitMotor(((Generic6DofConstraint)constraint).getTranslationalLimitMotor());
    }

    /**
     * returns the TranslationalLimitMotor of this 6DofJoint which allows
     * manipulating the translational axis
     * @return the TranslationalLimitMotor
     */
    public TranslationalLimitMotor getTranslationalLimitMotor() {
        return translationalMotor;
    }

    /**
     * returns one of the three RotationalLimitMotors of this 6DofJoint which
     * allow manipulating the rotational axes
     * @param index the index of the RotationalLimitMotor
     * @return the RotationalLimitMotor at the given index
     */
    public RotationalLimitMotor getRotationalLimitMotor(int index){
        return rotationalMotors.get(index);
    }

    public void setLinearUpperLimit(Vector3f vector){
        ((Generic6DofConstraint)constraint).setLinearUpperLimit(Converter.convert(vector));
    }

    public void setLinearLowerLimit(Vector3f vector){
        ((Generic6DofConstraint)constraint).setLinearLowerLimit(Converter.convert(vector));
    }

    public void setAngularUpperLimit(Vector3f vector){
        ((Generic6DofConstraint)constraint).setAngularUpperLimit(Converter.convert(vector));
    }

    public void setAngularLowerLimit(Vector3f vector){
        ((Generic6DofConstraint)constraint).setAngularLowerLimit(Converter.convert(vector));
    }
    
}
