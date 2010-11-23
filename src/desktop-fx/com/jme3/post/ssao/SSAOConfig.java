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

package com.jme3.post.ssao;

/**
 *
 * @author nehon
 * @deprectad don't use this class params are now embeded in the filter
 */
@Deprecated
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
