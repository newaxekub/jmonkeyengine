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
package com.jme3.bullet.collision.shapes;

import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.linearmath.Transform;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.bullet.util.Converter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A CompoundCollisionShape allows combining multiple base shapes
 * to generate a more sophisticated shape.
 * @author normenhansen
 */
public class CompoundCollisionShape extends CollisionShape {

    private ArrayList<ChildCollisionShape> children = new ArrayList<ChildCollisionShape>();

    public CompoundCollisionShape() {
        cShape = new CompoundShape();
    }

    /**
     * adds a child shape at the given local translation
     * @param shape the child shape to add
     * @param location the local location of the child shape
     */
    public void addChildShape(CollisionShape shape, Vector3f location) {
        Transform transA = new Transform(Converter.convert(new Matrix3f()));
        Converter.convert(location, transA.origin);
        children.add(new ChildCollisionShape(location, new Matrix3f(), shape));
        ((CompoundShape) cShape).addChildShape(transA, shape.getCShape());
    }

    /**
     * adds a child shape at the given local translation
     * @param shape the child shape to add
     * @param location the local location of the child shape
     */
    public void addChildShape(CollisionShape shape, Vector3f location, Matrix3f rotation) {
        Transform transA = new Transform(Converter.convert(rotation));
        Converter.convert(location, transA.origin);
        children.add(new ChildCollisionShape(location, rotation, shape));
        ((CompoundShape) cShape).addChildShape(transA, shape.getCShape());
    }

    /**
     * removes a child shape
     * @param shape the child shape to remove
     */
    public void removeChildShape(CollisionShape shape) {
        ((CompoundShape) cShape).removeChildShape(shape.getCShape());
        for (Iterator<ChildCollisionShape> it = children.iterator(); it.hasNext();) {
            ChildCollisionShape childCollisionShape = it.next();
            if (childCollisionShape.shape == shape) {
                it.remove();
            }
        }
    }

    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.writeSavableArrayList(children, "children", new ArrayList<ChildCollisionShape>());
    }

    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule capsule = im.getCapsule(this);
        children = capsule.readSavableArrayList("children", new ArrayList<ChildCollisionShape>());
        cShape.setLocalScaling(Converter.convert(scale));
        loadChildren();
    }

    private void loadChildren() {
        for (Iterator<ChildCollisionShape> it = children.iterator(); it.hasNext();) {
            ChildCollisionShape child = it.next();
            addChildShape(child.shape, child.location, child.rotation);
        }
    }

    public class ChildCollisionShape implements Savable {

        public Vector3f location;
        public Matrix3f rotation;
        public CollisionShape shape;

        public ChildCollisionShape() {
        }

        public ChildCollisionShape(Vector3f location, Matrix3f rotation, CollisionShape shape) {
            this.location = location;
            this.rotation = rotation;
            this.shape = shape;
        }

        public void write(JmeExporter ex) throws IOException {
            OutputCapsule capsule = ex.getCapsule(this);
            capsule.write(location, "location", new Vector3f());
            capsule.write(rotation, "rotation", new Matrix3f());
            capsule.write(shape, "shape", new BoxCollisionShape(new Vector3f(1, 1, 1)));
        }

        public void read(JmeImporter im) throws IOException {
            InputCapsule capsule = im.getCapsule(this);
            location = (Vector3f) capsule.readSavable("location", new Vector3f());
            rotation = (Matrix3f) capsule.readSavable("rotation", new Matrix3f());
            shape = (CollisionShape) capsule.readSavable("shape", new BoxCollisionShape(new Vector3f(1, 1, 1)));
        }
    }
}
