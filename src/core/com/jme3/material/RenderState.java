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

package com.jme3.material;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import java.io.IOException;

public class RenderState implements Cloneable, Savable {

    public static final RenderState DEFAULT = new RenderState();
    public static final RenderState NULL = new RenderState();

    public enum TestFunc {
        Never,
        Equal,
        Less,
        LessOrEqual,
        Greater,
        GreaterOrEqual,
        NotEqual,
        Always,
    }

    public enum BlendMode {

        /**
         * No blending mode is used.
         */
        Off,

        /**
         * Additive blending. For use with glows and particle emitters.
         *
         * Result = Source Color + Destination Color
         */
        Additive,

        /**
         * Premultiplied alpha blending, for use with premult alpha textures.
         *
         * Result = Source Color + (Dest Color * 1 - Source Alpha)
         */
        PremultAlpha,

        /**
         * Additive blending that is multiplied with source alpha.
         * For use with glows and particle emitters.
         *
         * Result = (Source Alpha * Source Color) + Dest Color
         */
        AlphaAdditive,

        /**
         * Color blending, blends in color from dest color
         * using source color.
         *
         * Result = Source Color + (1 - Source Color) * Dest Color
         */
        Color,

        /**
         * Alpha blending, interpolates to source color from dest color
         * using source alpha.
         *
         * Result = Source Alpha * Source Color +
         *          (1 - Source Alpha) * Dest Color
         */
        Alpha,

        /**
         * Multiplies the source and dest colors.
         *
         * Result = Source Color * Dest Color
         */
        Modulate,

        /**
         * Multiplies the source and dest colors then doubles the result.
         *
         * Result = 2 * Source Color * Dest Color
         */
        ModulateX2
    }

    public enum FaceCullMode {

        /**
         * Face culling is disabled.
         */
        Off,

        /**
         * Cull front faces
         */
        Front,

        /**
         * Cull back faces
         */
        Back,

        /**
         * Cull both front and back faces. 
         */
        FrontAndBack
    }

    static {
        NULL.cullMode = FaceCullMode.Off;
        NULL.depthTest = false;
    }

    boolean pointSprite = false;
    boolean wireframe = false;
    FaceCullMode cullMode = FaceCullMode.Back;

    boolean depthWrite = true;
    boolean depthTest = true;

    boolean colorWrite = true;

    BlendMode blendMode = BlendMode.Off;
    boolean alphaTest = false;
    float alphaFallOff = 0;

    boolean offsetEnabled = false;
    float offsetFactor = 0;
    float offsetUnits = 0;

    public void write(JmeExporter ex) throws IOException{
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(pointSprite, "pointSprite", false);
        oc.write(wireframe, "wireframe", false);
        oc.write(cullMode, "cullMode", FaceCullMode.Back);
        oc.write(depthWrite, "depthWrite", true);
        oc.write(depthTest, "depthTest", true);
        oc.write(colorWrite, "colorWrite", true);
        oc.write(blendMode, "blendMode", BlendMode.Off);
        oc.write(alphaTest, "alphaTest", false);
        oc.write(alphaFallOff, "alphaFallOff", 0);
        oc.write(offsetEnabled, "offsetEnabled", false);
        oc.write(offsetFactor, "offsetFactor", 0);
        oc.write(offsetUnits, "offsetUnits", 0);
    }

    public void read(JmeImporter im) throws IOException{
        InputCapsule ic = im.getCapsule(this);
        pointSprite = ic.readBoolean("pointSprite", false);
        wireframe  = ic.readBoolean("wireframe", false);
        cullMode  = ic.readEnum("cullMode", FaceCullMode.class, FaceCullMode.Back);
        depthWrite  = ic.readBoolean("depthWrite", true);
        depthTest  = ic.readBoolean("depthTest", true);
        colorWrite  = ic.readBoolean("colorWrite", true);
        blendMode  = ic.readEnum("blendMode", BlendMode.class, BlendMode.Off);
        alphaTest  = ic.readBoolean("alphaTest", false);
        alphaFallOff  = ic.readFloat("alphaFallOff", 0);
        offsetEnabled  = ic.readBoolean("offsetEnabled", false);
        offsetFactor  = ic.readFloat("offsetFactor", 0);
        offsetUnits  = ic.readFloat("offsetUnits", 0);
    }

    @Override
    public RenderState clone(){
        try{
            return (RenderState) super.clone();
        }catch (CloneNotSupportedException ex){
            throw new AssertionError();
        }
    }

    public boolean isPointSprite() {
        return pointSprite;
    }

    public void setPointSprite(boolean pointSprite) {
        this.pointSprite = pointSprite;
    }

    public boolean isColorWrite() {
        return colorWrite;
    }

    public float getPolyOffsetFactor() {
        return offsetFactor;
    }

    public float getPolyOffsetUnits() {
        return offsetUnits;
    }

    public boolean isPolyOffset(){
        return offsetEnabled;
    }

    public float getAlphaFallOff() {
        return alphaFallOff;
    }

    public void setAlphaFallOff(float alphaFallOff) {
        this.alphaFallOff = alphaFallOff;
    }

    public boolean isAlphaTest() {
        return alphaTest;
    }

    public void setAlphaTest(boolean alphaTest) {
        this.alphaTest = alphaTest;
    }

    public FaceCullMode getFaceCullMode() {
        return cullMode;
    }

    public void setColorWrite(boolean colorWrite){
        this.colorWrite = colorWrite;
    }

    public void setPolyOffset(float factor, float units){
        offsetEnabled = true;
        offsetFactor = factor;
        offsetUnits = units;
    }

    public void setFaceCullMode(FaceCullMode cullMode) {
        this.cullMode = cullMode;
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
    }

    public boolean isDepthTest() {
        return depthTest;
    }

    public void setDepthTest(boolean depthTest) {
        this.depthTest = depthTest;
    }

    public boolean isDepthWrite() {
        return depthWrite;
    }

    public void setDepthWrite(boolean depthWrite) {
        this.depthWrite = depthWrite;
    }

    public boolean isWireframe() {
        return wireframe;
    }

    public void setWireframe(boolean wireframe) {
        this.wireframe = wireframe;
    }

}
