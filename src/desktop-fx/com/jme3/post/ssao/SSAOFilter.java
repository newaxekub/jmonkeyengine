/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.post.ssao;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.Filter;
import com.jme3.post.Filter.Pass;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.shader.VarType;
import com.jme3.texture.Image.Format;


/**
 *
 * @author nehon
 */
public class SSAOFilter extends Filter {

    private Pass normalPass;
    private Material normalMaterial;
    private Vector3f frustumCorner;
    private Vector2f frustumNearFar;
    private SSAOConfig config;
    private Vector2f[] samples={new Vector2f(1.0f,0.0f),new Vector2f(-1.0f,0.0f),new Vector2f(0.0f,1.0f),new Vector2f(0.0f,-1.0f)};

    public SSAOFilter(ViewPort vp) {
        this(vp, new SSAOConfig());
    }

    public SSAOFilter(ViewPort vp, SSAOConfig config) {
        this.config=config;
        normalPass = new Pass();
        setRequiresDepthTexture(true);
        frustumNearFar=new Vector2f();

        int screenWidth = vp.getCamera().getWidth();
        int screenHeight = vp.getCamera().getHeight();
        //Need to fix this for the moment only works for 45° FOV
        float farY = FastMath.tan((float) (FastMath.DEG_TO_RAD * 45.0 / 2.0)) * vp.getCamera().getFrustumFar();
        float farX = farY * (screenWidth / screenHeight);
        frustumCorner = new Vector3f(farX, farY, vp.getCamera().getFrustumFar());
        frustumNearFar.x=vp.getCamera().getFrustumNear();
        frustumNearFar.y=vp.getCamera().getFrustumFar();

    }

    @Override
    public void init(AssetManager manager, int width, int height) {
        super.init(manager, width, height);
        normalPass.init(width, height, Format.RGB32F, Format.Depth);
    }

    @Override
    protected Format getDefaultPassDepthFormat() {
        return Format.Depth32F;
    }

    @Override
    public void preRender(RenderManager renderManager, ViewPort viewPort) {
        Renderer r = renderManager.getRenderer();
        r.setFrameBuffer(normalPass.getRenderFrameBuffer());
        renderManager.getRenderer().clearBuffers(true, true, true);
        renderManager.setForcedMaterial(normalMaterial);
        renderManager.renderViewPortQueues(viewPort, false);
        renderManager.setForcedMaterial(null);
        renderManager.getRenderer().setFrameBuffer(viewPort.getOutputFrameBuffer());

    }

    @Override
    public Material getMaterial() {
        material.setTexture("m_Normals", normalPass.getRenderedTexture());
        material.setVector3("frustumCorner", frustumCorner);
        material.setFloat("m_SampleRadius", config.getSampleRadius() );
        material.setFloat("m_Intensity", config.getIntensity());
        material.setFloat("m_Scale", config.getScale());
        material.setFloat("m_Bias", config.getBias());
        material.setBoolean("m_UseAo", config.isUseAo());
        material.setBoolean("m_UseOnlyAo", config.isUseOnlyAo());
        material.setVector2("m_FrustumNearFar",frustumNearFar);
        material.setParam("m_Samples",VarType.Vector2Array,samples);

 //
        return material;
    }

    @Override
    public void initMaterial(AssetManager manager) {
        material = new Material(manager, "Common/MatDefs/SSAO/ssao.j3md");
        normalMaterial = new Material(manager, "Common/MatDefs/SSAO/normal.j3md");
    }

    public SSAOConfig getConfig() {
        return config;
    }

    public void setConfig(SSAOConfig config) {
        this.config = config;
    }

    
}
