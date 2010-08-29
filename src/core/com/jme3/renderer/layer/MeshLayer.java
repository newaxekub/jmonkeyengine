package com.jme3.renderer.layer;

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;

/**
 * Layer for handling meshes.
 */
public interface MeshLayer {

    /**
     * Uploads the vertex buffer's data onto the GPU, assiging it an ID if
     * needed.
     */
    public void updateBufferData(VertexBuffer vb);

    /**
     * Deletes a vertex buffer from the GPU.
     * @param vb The vertex buffer to delete
     */
    public void deleteBuffer(VertexBuffer vb);

    /**
     * Sets the vertex attrib. This data is exposed in the shader depending
     * on type, e.g Type.Position would be given as inPosition, Type.Tangent is
     * given as inTangent, etc.
     *
     * @param vb
     * @throws InvalidArgumentException If the given vertex buffer is an
     * index buffer.
     */
    public void setVertexAttrib(VertexBuffer vb);

    /**
     * Draws the list of triangles given in the index buffer.
     * Each triangle is composed of 3 indices to a vertex, the attribs of which
     * are supplied using <code>setVertexAttrib</code>.
     * The int variable gl_VertexID can be used to access the current
     * vertex index inside the vertex shader.
     *
     * @param count The number of instances to draw
     */
    public void drawTriangleList(VertexBuffer indexBuf, Mesh.Mode mode, int count, int vertCount);

    /**
     * Clears all vertex attributes set with <code>setVertexAttrib</code>.
     */
    public void clearVertexAttribs();

    /**
     * Renders <code>count</code> meshes, with the geometry data supplied.
     * The shader which is currently set with <code>setShader</code> is
     * responsible for transforming the input verticies into clip space
     * and shading it based on the given vertex attributes.
     * The int variable gl_InstanceID can be used to access the current
     * instance of the mesh being rendered inside the vertex shader.
     *
     * @param mesh
     * @param count
     */
    public void renderMesh(Mesh mesh, int count);

}
