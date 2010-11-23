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
package com.jme3.post;

import com.jme3.asset.AssetManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.material.Material;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import java.io.IOException;
import java.util.List;

/**
 * Filter abstract class
 * Any Filter must extends this class
 * Holds a frameBuffer and a texture
 * The getMaterial must return a Material that use a GLSL shader immplementing the desired effect
 */
public abstract class Filter implements Savable {

    private String name;
    protected Pass defaultPass;
    protected List<Pass> postRenderPasses;
    protected Material material;
    protected boolean enabled = true;

    public Filter(String name) {
        this.name = name;
    }

    public class Pass {

        protected FrameBuffer renderFrameBuffer;
        protected Texture2D renderedTexture;
        protected Material passMaterial;

        public void init(int width, int height, Format textureFormat, Format depthBurfferFormat) {
            renderFrameBuffer = new FrameBuffer(width, height, 0);
            renderedTexture = new Texture2D(width, height, textureFormat);
            renderFrameBuffer.setDepthBuffer(depthBurfferFormat);
            renderFrameBuffer.setColorTexture(renderedTexture);
        }

        public void init(int width, int height, Format textureFormat, Format depthBurfferFormat, Material material) {
            init(width, height, textureFormat, depthBurfferFormat);
            passMaterial = material;
        }

        public boolean requiresSceneAsTexture() {
            return false;
        }

        public void beforeRender() {
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

        public Material getPassMaterial() {
            return passMaterial;
        }

        public void setPassMaterial(Material passMaterial) {
            this.passMaterial = passMaterial;
        }
    }

    protected Format getDefaultPassTextureFormat() {
        return Format.RGB8;
    }

    protected Format getDefaultPassDepthFormat() {
        return Format.Depth;
    }

    public Filter() {
        this("filter");
    }

    public void init(AssetManager manager, ViewPort vp) {
        defaultPass = new Pass();
        defaultPass.init(vp.getCamera().getWidth(), vp.getCamera().getHeight(), getDefaultPassTextureFormat(), getDefaultPassDepthFormat());
        initFilter(manager, vp);
        //TODO delete this once the deprecated method will be removed
        viewPort = vp;
    }

    public void cleanup(Renderer r) {
        if (defaultPass.renderFrameBuffer != null) {
            r.deleteFrameBuffer(defaultPass.renderFrameBuffer);
            r.deleteTexture(defaultPass.renderedTexture);
        }
    }

    //TODO delete this once the deprecated method will be removed
    private ViewPort viewPort;
    /**
     * @deprecated use initFilter(AssetManager manager,ViewPort vp) method instead
     */
    @Deprecated
    public void initMaterial(AssetManager manager) {
        initFilter(manager, viewPort);
    }    

    /**
     * This method is called once xhen the filter is added to the FilterPostProcessor
     * It should contain Maerial initializations and extra passes initialization
     * @param manager
     */
    public abstract void initFilter(AssetManager manager, ViewPort vp);

    /**
     * this method should return the material used for this filter.
     * this method is called every frames
     * @return
     */
    public abstract Material getMaterial();

    /**
     * Override this method if you want to make a pre pass, before the actual rendering of the frame
     * @param renderManager
     * @param viewPort
     */
    public void preRender(RenderManager renderManager, ViewPort viewPort) {
    }

    /**
     * Use this method if you want to modify parameters according to tpf before the rendering of the frame.
     * This is usefull for animated filters
     * @param tpf the time used to render the previous frame
     */
    public void preFrame(float tpf) {
    }

    /**
     * Override this method if you want to save extra properties when the filter is saved else only basic properties of the filter will be saved
     * This method should always begin by super.write(ex);
     * @param ex
     * @throws IOException
     */
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(name, "name", "");
        oc.write(enabled, "enabled", true);
    }

    /**
     * Override this method if you want to load extra properties when the filter is loaded else only basic properties of the filter will be loaded
     * This method should always begin by super.read(ex);
     * @param ex
     * @throws IOException
     */
    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        name = ic.readString("name", "");
        enabled = ic.readBoolean("enabled", true);

    }

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

    /**
     * Override this method and retrun true if your Filter need the depth texture
     * @return
     */
    public boolean isRequiresDepthTexture() {
        return false;
    }

    public List<Pass> getPostRenderPasses() {
        return postRenderPasses;
    }

    public void setPostRenderPasses(List<Pass> postRenderPasses) {
        this.postRenderPasses = postRenderPasses;
    }
}
