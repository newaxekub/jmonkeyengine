package com.jme3.renderer;

import com.jme3.material.RenderState;
import com.jme3.scene.VertexBuffer;
import com.jme3.texture.Texture;

/**
 * Represents the current state of the graphics library. This class is used
 * internally to reduce state changes. NOTE: This class is specific to OpenGL.
 */
public class RenderContext {
    
    /**
     * If back-face culling is enabled.
     */
    public RenderState.FaceCullMode cullMode = RenderState.FaceCullMode.Off;

    /**
     * If Depth testing is enabled.
     */
    public boolean depthTestEnabled = false;

    public boolean alphaTestEnabled = false;

    public boolean depthWriteEnabled = false;

    public boolean colorWriteEnabled = true;

    public boolean clipRectEnabled = false;

    public boolean polyOffsetEnabled = false;
    public float polyOffsetFactor = 0;
    public float polyOffsetUnits = 0;

    public boolean normalizeEnabled = false;

    public int matrixMode = -1;

    public float pointSize = 1;

    public RenderState.BlendMode blendMode = RenderState.BlendMode.Off;

    /**
     * If wireframe rendering is enabled. False if fill rendering is enabled.
     */
    public boolean wireframe = false;

    /**
     * The currently bound shader program.
     */
    public int boundShaderProgram;

    /**
     * Currently bound Framebuffer Object.
     */
    public int boundFBO = 0;

    /**
     * Currently bound Renderbuffer
     */
    public int boundRB = 0;

    /**
     * Currently bound draw buffer
     * -2 = GL_NONE
     * -1 = GL_BACK
     *  0 = GL_COLOR_ATTACHMENT0
     *  n = GL_COLOR_ATTACHMENTn
     *  where n is an integer greater than 1
     */
    public int boundDrawBuf = -1;

    /**
     * Currently bound read buffer
     *
     * @see RenderContext#boundDrawBuf
     */
    public int boundReadBuf = -1;

    /**
     * Currently bound element array vertex buffer.
     */
    public int boundElementArrayVBO;

    public int boundVertexArray;

    /**
     * Currently bound array vertex buffer.
     */
    public int boundArrayVBO;

    public int numTexturesSet = 0;

    /**
     * Current bound texture IDs for each texture unit.
     */
    public Texture[] boundTextures = new Texture[16];

    public IDList textureIndexList = new IDList();

    public int boundTextureUnit = 0;

    /**
     * Vertex attribs currently bound and enabled. If a slot is null, then
     * it is disabled.
     */
    public VertexBuffer[] boundAttribs = new VertexBuffer[16];

    public IDList attribIndexList = new IDList();

    public void reset(){
        cullMode = RenderState.FaceCullMode.Off;
        depthTestEnabled = false;
        alphaTestEnabled = false;
        depthWriteEnabled = false;
        colorWriteEnabled = false;
        clipRectEnabled = false;
        polyOffsetEnabled = false;
        polyOffsetFactor = 0;
        polyOffsetUnits = 0;
        normalizeEnabled = false;
        matrixMode = -1;
        pointSize = 1;
        blendMode = RenderState.BlendMode.Off;
        wireframe = false;
        boundShaderProgram = 0;
        boundFBO = 0;
        boundRB = 0;
        boundDrawBuf = -1;
        boundElementArrayVBO = 0;
        boundVertexArray = 0;
        boundArrayVBO = 0;
        numTexturesSet = 0;
        for (int i = 0; i < boundTextures.length; i++)
            boundTextures[i] = null;

        textureIndexList.reset();
        boundTextureUnit = 0;
        for (int i = 0; i < boundAttribs.length; i++)
            boundAttribs[i] = null;

        attribIndexList.reset();
    }
}
