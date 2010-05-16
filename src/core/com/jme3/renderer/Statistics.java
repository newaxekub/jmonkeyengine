package com.jme3.renderer;

import com.jme3.scene.Mesh;
import com.jme3.shader.Shader;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Texture;
import java.util.HashSet;

public class Statistics {

    protected int numObjects;
    protected int numTriangles;
    protected int numVertices;
    protected int numShaderSwitches;
    protected int numTextureBinds;
    protected int numFboSwitches;
    protected int numUniformsSet;

    protected int memoryShaders;
    protected int memoryFrameBuffers;
    protected int memoryTextures;

    protected HashSet<Integer> shadersUsed = new HashSet<Integer>();
    protected HashSet<Integer> texturesUsed = new HashSet<Integer>();
    protected HashSet<Integer> fbosUsed = new HashSet<Integer>();

    public String[] getLabels(){
        return new String[]{ "Vertices",
                             "Triangles",
                             "Uniforms",

                             "Objects",

                             "Shaders (S)",
                             "Shaders (F)",
                             "Shaders (M)",

                             "Textures (S)",
                             "Textures (F)",
                             "Textures (M)",

                             "FrameBuffers (S)",
                             "FrameBuffers (F)",
                             "FrameBuffers (M)" };

    }

    public void getData(int[] data){
        data[0] = numVertices;
        data[1] = numTriangles;
        data[2] = numUniformsSet;
        data[3] = numObjects;

        data[4] = numShaderSwitches;
        data[5] = shadersUsed.size();
        data[6] = memoryShaders;

        data[7] = numTextureBinds;
        data[8] = texturesUsed.size();
        data[9] = memoryTextures;
        
        data[10] = numFboSwitches;
        data[11] = fbosUsed.size();
        data[12] = memoryFrameBuffers;
    }

    public void onMeshDrawn(Mesh mesh, int lod){
        numObjects ++;
        numTriangles += mesh.getTriangleCount(lod);
        numVertices += mesh.getVertexCount();
    }

    public void onShaderUse(Shader shader, boolean wasSwitched){
        assert shader.id >= 1;

        if (!shadersUsed.contains(shader.id))
            shadersUsed.add(shader.id);

        if (wasSwitched)
            numShaderSwitches++;
    }

    public void onUniformSet(){
        numUniformsSet ++;
    }

    public void onTextureUse(Texture texture, boolean wasSwitched){
        assert texture.id >= 1;

        if (!texturesUsed.contains(texture.id))
            texturesUsed.add(texture.id);

        if (wasSwitched)
            numTextureBinds ++;
    }

    public void onFrameBufferUse(FrameBuffer fb, boolean wasSwitched){
        if (fb != null){
            assert fb.id >= 1;

            if (!fbosUsed.contains(fb.id))
                fbosUsed.add(fb.id);
        }

        if (wasSwitched)
            numFboSwitches ++;
    }
    
    public void clearFrame(){
        shadersUsed.clear();
        texturesUsed.clear();
        fbosUsed.clear();

        numObjects = 0;
        numTriangles = 0;
        numVertices = 0;
        numShaderSwitches = 0;
        numTextureBinds = 0;
        numFboSwitches = 0;
        numUniformsSet = 0;
    }

    public void onNewShader(){
        memoryShaders ++;
    }

    public void onNewTexture(){
        memoryTextures ++;
    }

    public void onNewFrameBuffer(){
        memoryFrameBuffers ++;
    }

    public void onDeleteShader(){
        memoryShaders --;
    }

    public void onDeleteTexture(){
        memoryTextures --;
    }

    public void onDeleteFrameBuffer(){
        memoryFrameBuffers --;
    }

    public void clearMemory(){
        memoryFrameBuffers = 0;
        memoryShaders = 0;
        memoryTextures = 0;
    }

    

}
