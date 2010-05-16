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
package com.jme3.bullet.util;

import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author normenhansen
 */
public class CollisionShapeFactory {

    private static CompoundCollisionShape createMeshCompoundShape(Node rootNode, CompoundCollisionShape shape) {
        List<Spatial> children = rootNode.getChildren();
        for (Iterator<Spatial> it = children.iterator(); it.hasNext();) {
            Spatial spatial = it.next();
            if (spatial instanceof Node) {
                createMeshCompoundShape((Node) spatial, shape);
            } else if (spatial instanceof Geometry) {
                Geometry geom = (Geometry) spatial;
                Mesh mesh = geom.getMesh();
                if (mesh != null) {
                    MeshCollisionShape mColl = new MeshCollisionShape(mesh);
                    mColl.setScale(geom.getWorldScale());
                    shape.addChildShape(mColl, geom.getWorldTranslation(),geom.getWorldRotation().toRotationMatrix());
                }
            }
        }
        return shape;
    }

    public static CompoundCollisionShape createMeshCompoundShape(Node rootNode){
        rootNode.updateGeometricState();
        return createMeshCompoundShape(rootNode, new CompoundCollisionShape());
    }
}
