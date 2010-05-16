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

import com.bulletphysics.collision.shapes.IndexedMesh;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.extras.gimpact.GImpactMeshShape;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.bullet.util.Converter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Basic mesh collision shape
 * @author normenhansen
 */
public class GImpactCollisionShape extends CollisionShape{

    protected Vector3f worldScale;
    protected int numVertices, numTriangles, vertexStride, triangleIndexStride;
    protected ByteBuffer triangleIndexBase, vertexBase;

    public GImpactCollisionShape() {
    }

    /**
     * creates a collision shape from the given Mesh
     * @param mesh the Mesh to use
     */
    public GImpactCollisionShape(Mesh mesh) {
        createCollisionMesh(mesh, new Vector3f(1,1,1));
    }


    private void createCollisionMesh(Mesh mesh, Vector3f worldScale) {
        this.worldScale = worldScale;
        IndexedMesh imesh = Converter.convert(mesh);
        this.numVertices = imesh.numVertices;
        this.numTriangles = imesh.numTriangles;
        this.vertexStride = imesh.vertexStride;
        this.triangleIndexStride = imesh.triangleIndexStride;
        this.triangleIndexBase = imesh.triangleIndexBase;
        this.vertexBase = imesh.vertexBase;
        createShape();
    }

    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(worldScale, "worldScale", new Vector3f(1, 1, 1));
        capsule.write(numVertices, "numVertices", 0);
        capsule.write(numTriangles, "numTriangles", 0);
        capsule.write(vertexStride, "vertexStride", 0);
        capsule.write(triangleIndexStride, "triangleIndexStride", 0);

        capsule.write(triangleIndexBase.array(), "triangleIndexBase", new byte[0]);
        capsule.write(vertexBase.array(), "vertexBase", new byte[0]);
    }

    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule capsule = im.getCapsule(this);
        worldScale = (Vector3f) capsule.readSavable("worldScale", new Vector3f(1, 1, 1));
        numVertices = capsule.readInt("numVertices", 0);
        numTriangles = capsule.readInt("numTriangles", 0);
        vertexStride = capsule.readInt("vertexStride", 0);
        triangleIndexStride = capsule.readInt("triangleIndexStride", 0);

        triangleIndexBase = ByteBuffer.wrap(capsule.readByteArray("triangleIndexBase", new byte[0]));
        vertexBase = ByteBuffer.wrap(capsule.readByteArray("vertexBase", new byte[0]));
        createShape();
    }

    protected void createShape() {
        IndexedMesh mesh = new IndexedMesh();
        mesh.numVertices = numVertices;
        mesh.numTriangles = numTriangles;
        mesh.vertexStride = vertexStride;
        mesh.triangleIndexStride = triangleIndexStride;
        mesh.triangleIndexBase = triangleIndexBase;
        mesh.vertexBase = vertexBase;
        mesh.triangleIndexBase = triangleIndexBase;
        TriangleIndexVertexArray tiv = new TriangleIndexVertexArray(numTriangles, triangleIndexBase, triangleIndexStride, numVertices, vertexBase, vertexStride);
        cShape = new GImpactMeshShape(tiv);
        cShape.setLocalScaling(Converter.convert(worldScale));
        ((GImpactMeshShape)cShape).updateBound();
        ((GImpactMeshShape)cShape).lockChildShapes();
        cShape.setLocalScaling(Converter.convert(scale));
    }

}
