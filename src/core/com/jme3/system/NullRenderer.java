package com.jme3.system;

import com.jme3.light.LightList;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.renderer.Caps;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.Statistics;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.shader.Shader;
import com.jme3.shader.Shader.ShaderSource;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Texture;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.EnumSet;

public class NullRenderer implements Renderer {

    private static final EnumSet<Caps> caps = EnumSet.noneOf(Caps.class);
    private static final Statistics stats = new Statistics();

    public Collection<Caps> getCaps() {
        return caps;
    }

    public Statistics getStatistics() {
        return stats;
    }

    public void clearBuffers(boolean color, boolean depth, boolean stencil) {
    }

    public void setBackgroundColor(ColorRGBA color) {
    }

    public void applyRenderState(RenderState state) {
    }

    public void setDepthRange(float start, float end) {
    }

    public void onFrame() {
    }

    public void setWorldMatrix(Matrix4f worldMatrix) {
    }

    public void setViewProjectionMatrices(Matrix4f viewMatrix, Matrix4f projMatrix) {
    }

    public void setViewPort(int x, int y, int width, int height) {
    }

    public void setClipRect(int x, int y, int width, int height) {
    }

    public void clearClipRect() {
    }

    public void setLighting(LightList lights) {
    }

    public void setShader(Shader shader) {
    }

    public void deleteShader(Shader shader) {
    }

    public void deleteShaderSource(ShaderSource source) {
    }

    public void copyFrameBuffer(FrameBuffer src, FrameBuffer dst) {
    }

    public void setFrameBuffer(FrameBuffer fb) {
    }

    public void readFrameBuffer(FrameBuffer fb, ByteBuffer byteBuf) {
    }

    public void deleteFrameBuffer(FrameBuffer fb) {
    }

    public void setTexture(int unit, Texture tex) {
    }

    public void deleteTexture(Texture tex) {
    }

    public void updateBufferData(VertexBuffer vb) {
    }

    public void deleteBuffer(VertexBuffer vb) {
    }

    public void renderMesh(Mesh mesh, int lod, int count) {
    }

    public void resetGLObjects() {
    }

    public void cleanup() {
    }

}
