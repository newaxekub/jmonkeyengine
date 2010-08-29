package com.jme3.post;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;

/**
 * Filter abstract class
 * Any Filter must extends this class
 * Holds a frameBuffer and a texture
 * The getMaterial must return a Material that use a GLSL shader immplementing the desired effect
 */
public abstract class Filter {


    private String name;
    private boolean requiresDepthTexture=false;
    protected Pass defaultPass=new Pass();
    protected Material material;

    public Filter(String name) {
        this.name=name;
    }

    public class Pass{

          protected FrameBuffer renderFrameBuffer;
          protected Texture2D renderedTexture;

          public void init(int width,int height,Format textureFormat,Format depthBurfferFormat) {
               renderFrameBuffer = new FrameBuffer(width, height, 0);
               renderedTexture = new Texture2D(width, height, textureFormat);
               renderFrameBuffer.setDepthBuffer(depthBurfferFormat);
               renderFrameBuffer.setColorTexture(renderedTexture);
          }

        public FrameBuffer getRenderFrameBuffer() {
            return renderFrameBuffer;
        }

        public void setRenderFrameBuffer(FrameBuffer renderFrameBuffer) {
            this.renderFrameBuffer = renderFrameBuffer;
        }

        public Texture2D getRenderedTexture() {
            return renderedTexture;
        }

        public void setRenderedTexture(Texture2D renderedTexture) {
            this.renderedTexture = renderedTexture;
        }

    }

    protected Format getDefaultPassTextureFormat(){
        return Format.RGBA8;
    }

    protected Format getDefaultPassDepthFormat(){
        return Format.Depth;
    }

    public Filter() {
        this("filter");
    }

    public void init(AssetManager manager,int width,int height) {
        defaultPass.init(width, height, getDefaultPassTextureFormat(), getDefaultPassDepthFormat());
        initMaterial(manager);
    }

    public void cleanup(Renderer r){
        if(defaultPass.renderFrameBuffer!=null){
            r.deleteFrameBuffer(defaultPass.renderFrameBuffer);
            r.deleteTexture(defaultPass.renderedTexture);
        }
    }

    public abstract void initMaterial(AssetManager manager);
    public abstract Material getMaterial();
    public abstract void preRender(RenderManager renderManager,ViewPort viewPort);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FrameBuffer getRenderFrameBuffer() {
        return defaultPass.renderFrameBuffer;
    }

    public void setRenderFrameBuffer(FrameBuffer renderFrameBuffer) {
        this.defaultPass.renderFrameBuffer = renderFrameBuffer;
    }

    public Texture2D getRenderedTexture() {
        return defaultPass.renderedTexture;
    }

    public void setRenderedTexture(Texture2D renderedTexture) {
        this.defaultPass.renderedTexture = renderedTexture;
    }

    public boolean isRequiresDepthTexture() {
        return requiresDepthTexture;
    }

    public void setRequiresDepthTexture(boolean requiresDepthTexture) {
        this.requiresDepthTexture = requiresDepthTexture;
    }

}
