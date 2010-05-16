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
