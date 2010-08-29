/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.post.ssao;

/**
 *
 * @author nehon
 */
public class SSAOConfig {

    protected float sampleRadius = 5.1f;
    protected float intensity = 1.5f;
    protected float scale = 0.2f;
    protected float bias = 0.1f;
    protected boolean useOnlyAo = false;
    protected boolean useAo = true;

    public SSAOConfig() {
    }

    public SSAOConfig(float sampleRadius, float intensity, float scale, float bias, boolean useOnlyAo, boolean useAo) {
        this.sampleRadius = sampleRadius;
        this.intensity = intensity;
        this.scale = scale;
        this.bias = bias;
        this.useAo = useAo;
        this.useOnlyAo = useOnlyAo;
    }

    public float getBias() {
        return bias;
    }

    public void setBias(float bias) {
        this.bias = bias;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public float getSampleRadius() {
        return sampleRadius;
    }

    public void setSampleRadius(float sampleRadius) {
        this.sampleRadius = sampleRadius;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public boolean isUseAo() {
        return useAo;
    }

    public void setUseAo(boolean useAo) {
        this.useAo = useAo;
    }

    public boolean isUseOnlyAo() {
        return useOnlyAo;
    }

    public void setUseOnlyAo(boolean useOnlyAo) {
        this.useOnlyAo = useOnlyAo;
    }
}
