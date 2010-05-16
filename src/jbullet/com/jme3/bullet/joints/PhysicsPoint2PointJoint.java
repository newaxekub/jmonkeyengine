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

import com.bulletphysics.dynamics.constraintsolver.Point2PointConstraint;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Vector3f;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.bullet.util.Converter;
import java.io.IOException;

/**
 * <i>From bullet manual:</i><br>
 * Point to point constraint, also known as ball socket joint limits the translation
 * so that the local pivot points of 2 rigidbodies match in worldspace.
 * A chain of rigidbodies can be connected using this constraint.
 * @author normenhansen
 */
public class PhysicsPoint2PointJoint extends PhysicsJoint{

    public PhysicsPoint2PointJoint() {
    }

    public PhysicsPoint2PointJoint(PhysicsNode nodeA, PhysicsNode nodeB, Vector3f pivotA, Vector3f pivotB) {
        super(nodeA, nodeB, pivotA, pivotB);
        createJoint();
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        createJoint();
    }

    protected void createJoint(){
        constraint=new Point2PointConstraint(nodeA.getRigidBody(), nodeB.getRigidBody(), Converter.convert(pivotA), Converter.convert(pivotB));
    }
}
