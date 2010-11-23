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

import com.jme3.asset.AssetKey;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector2f;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.light.PointLight;
import com.jme3.material.TechniqueDef.LightMode;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Caps;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.scene.Geometry;
import com.jme3.shader.Shader;
import com.jme3.shader.Uniform;
import com.jme3.shader.VarType;
import com.jme3.texture.Texture;
import com.jme3.util.ListMap;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Material implements Cloneable, Savable {

    private static final Logger logger = Logger.getLogger(Material.class.getName());

    private static final RenderState additiveLight = new RenderState();
    private static final RenderState depthOnly = new RenderState();

    static {
        depthOnly.setDepthTest(true);
        depthOnly.setDepthWrite(true);
        depthOnly.setFaceCullMode(RenderState.FaceCullMode.Back);
        depthOnly.setColorWrite(false);

        additiveLight.setBlendMode(RenderState.BlendMode.Additive);
    }

    private MaterialDef def;
    private ListMap<String, MatParam> paramValues = new ListMap<String, MatParam>();
//    private HashMap<String, MatParam> paramValues = new HashMap<String, MatParam>();

    private Technique technique;
    private HashMap<String, Technique> techniques = new HashMap<String, Technique>();
    private int nextTexUnit = 0;
    private RenderState additionalState = null;
    private boolean transparent = false;
    private boolean receivesShadows = false;

    public static class MatParamTexture extends MatParam {

        private Texture texture;
        private int unit;
        private transient TextureKey key;

        public MatParamTexture(VarType type, String name, Texture texture, int unit){
            super(type, name, texture);
            this.texture = texture;
            this.unit = unit;
        }

        public MatParamTexture(){
        }

        public Texture getTextureValue(){
            return texture;
        }

        public void setTextureValue(Texture value){
            this.value = value;
            this.texture = value;
        }
        
        public int getUnit() {
            return unit;
        }

        @Override
        public void write(JmeExporter ex) throws IOException{
            super.write(ex);
            OutputCapsule oc = ex.getCapsule(this);
            oc.write(unit, "texture_unit", -1);
            oc.write(texture, "texture", null);
        }

        @Override
        public void read(JmeImporter im) throws IOException{
            super.read(im);
            InputCapsule ic = im.getCapsule(this);
            unit = ic.readInt("texture_unit", -1);
            texture = (Texture) ic.readSavable("texture", null);
            key = texture.getTextureKey();
        }
    }

    public Material(MaterialDef def){
        if (def == null)
            throw new NullPointerException("Material definition cannot be null");

        this.def = def;
    }

    public Material(AssetManager contentMan, String defName){
        this( (MaterialDef) contentMan.loadAsset(new AssetKey(defName)) );
    }

    /**
     * Do not use this constructor. Serialization purposes only.
     */
    public Material(){
    }

    @Override
    public Material clone(){
        try{
            Material mat = (Material) super.clone();
            if (additionalState != null){
                mat.additionalState = additionalState.clone();
            }
            mat.technique = null;
            mat.techniques = new HashMap<String, Technique>();

            mat.paramValues = new ListMap<String, MatParam>();
            for (int i = 0; i < paramValues.size(); i++){
                Map.Entry<String, MatParam> entry = paramValues.getEntry(i);
                mat.paramValues.put(entry.getKey(), entry.getValue().clone());
            }

            return mat;
        }catch (CloneNotSupportedException ex){
            throw new AssertionError();
        }
    }

    public Technique getActiveTechnique() {
        return technique;
    }

    /**
     * Should only be used in Technique.makeCurrent()
     * @param tech
     */
    public void setActiveTechnique(Technique tech){
        technique = tech;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    public boolean isReceivesShadows() {
        return receivesShadows;
    }

    public void setReceivesShadows(boolean receivesShadows) {
        this.receivesShadows = receivesShadows;
    }
    
    public RenderState getAdditionalRenderState(){
        if (additionalState == null)
            additionalState = new RenderState();
        return additionalState;
    }

    public MaterialDef getMaterialDef(){
        return def;
    }

    void updateUniformLinks(){
        for (MatParam param : paramValues.values()){
            param.uniform = technique.getShader().getUniform(param.name);
        }
    }

    public MatParam getParam(String name){
        MatParam param = paramValues.get(name);
        if (param instanceof MatParam)
            return (MatParam) param;

        return null;
    }

    public MatParamTexture getTextureParam(String name){
        MatParam param = paramValues.get(name);
        if (param instanceof MatParamTexture)
            return (MatParamTexture) param;

        return null;
    }

    public Collection<MatParam> getParams(){
        return paramValues.values();
    }

    private void checkSetParam(VarType type, String name){
        MatParam paramDef = def.getMaterialParam(name);
        if (paramDef == null)
            throw new IllegalArgumentException("Material parameter is not defined: " + name);

        if (paramDef.getVarType() != type)
            logger.logp(Level.WARNING, "Material parameter being set: {0} with " +
                                      "type {1} doesn't match definition type {2}",
                                      name, type.name(), paramDef.getVarType());
    }

    /**
     * Pass a parameter to the material shader
     * @param name the name of the parameter defined in the material definition (j3md)
     * @param type the type of the parameter @see com.jme3.shaderVarType
     * @param value the value of the param
     */
    public void setParam(String name, VarType type, Object value){
        checkSetParam(type, name);

        MatParam val = getParam(name);
        if (technique != null){
            technique.notifySetParam(name, type, value);
        }
        if (val == null)
            paramValues.put(name, new MatParam(type, name, value));
        else
            val.setValue(value);
    }

    public void setTextureParam(String name, VarType type, Texture value){
        if (value == null)
            throw new NullPointerException();

        checkSetParam(type, name);
        
        MatParamTexture val = getTextureParam(name);
        if (val == null)
            paramValues.put(name, new MatParamTexture(type, name, value, nextTexUnit++));
        else
            val.setTextureValue(value);

        if (technique != null){
            technique.notifySetParam(name, type, nextTexUnit-1);
        }
    }

    /**
     * Pass a texture to the material shader
     * @param name the name of the texture defined in the material definition (j3md) (for example m_Texture for Lighting.j3md)
     * @param value the Texture object previously loaded by the asset manager
     */
    public void setTexture(String name, Texture value){
        if (value == null)
            throw new NullPointerException();

        VarType paramType = null;
        switch (value.getType()){
            case TwoDimensional:
                paramType = VarType.Texture2D;
                break;
            case TwoDimensionalArray:
                paramType = VarType.TextureArray;
                break;
            case ThreeDimensional:
                paramType = VarType.Texture3D;
                break;
            case CubeMap:
                paramType = VarType.TextureCubeMap;
                break;
            default:
                throw new UnsupportedOperationException("Unknown texture type: "+value.getType());
        }
        
        setTextureParam(name, paramType, value);
    }

    /**
     * Pass a Matrix4f to the material shader
     * @param name the name of the matrix defined in the material definition (j3md)
     * @param value the Matrix4f object
     */
    public void setMatrix4(String name, Matrix4f value) {
        setParam(name, VarType.Matrix4, value);
    }

    /**
     * Pass a boolean to the material shader
     * @param name the name of the boolean defined in the material definition (j3md)
     * @param value the boolean value
     */
    public void setBoolean(String name, boolean value){
        setParam(name, VarType.Boolean, value);
    }

    /**
     * Pass a float to the material shader
     * @param name the name of the float defined in the material definition (j3md)
     * @param value the float value
     */
    public void setFloat(String name, float value){
        setParam(name, VarType.Float, value);
    }

    /**
     * Pass an int to the material shader
     * @param name the name of the int defined in the material definition (j3md)
     * @param value the int value
     */
    public void setInt(String name, int value){
        setParam(name, VarType.Int, value);
    }

    /**
     * Pass a Color to the material shader
     * @param name the name of the color defined in the material definition (j3md)
     * @param value the ColorRGBA value
     */
    public void setColor(String name, ColorRGBA value){
        setParam(name, VarType.Vector4, value);
    }

    /**
     * Pass a Vector2f to the material shader
     * @param name the name of the Vector2f defined in the material definition (j3md)
     * @param value the Vector2f value
     */
    public void setVector2(String name, Vector2f value) {
        setParam(name, VarType.Vector2, value);
    }

    /**
    * Pass a Vector3f to the material shader
    * @param name the name of the Vector3f defined in the material definition (j3md)
    * @param value the Vector3f value
    */
    public void setVector3(String name, Vector3f value) {
        setParam(name, VarType.Vector3, value);
    }

//    /**
//     * get the additional state on this material
//     * @return additionalState
//     */
//    public RenderState getAdditionalState() {
//        return additionalState;
//    }
//
//    /**
//     * set an additional state to the material
//     * @param additionalState
//     */
//    public void setAdditionalState(RenderState additionalState) {
//        this.additionalState = additionalState;
//    }

    /**
     * Uploads the lights in the light list as two uniform arrays.<br/><br/>
     *      * <p>
     * <code>uniform vec4 g_LightColor[numLights];</code><br/>
     * // g_LightColor.rgb is the diffuse/specular color of the light.<br/>
     * // g_Lightcolor.a is the type of light, 0 = Directional, 1 = Point, <br/>
     * // 2 = Spot. <br/>
     * <br/>
     * <code>uniform vec4 g_LightPosition[numLights];</code><br/>
     * // g_LightPosition.xyz is the position of the light (for point lights)<br/>
     * // or the direction of the light (for directional lights).<br/>
     * // g_LightPosition.w is the inverse radius (1/r) of the light (for attenuation) <br/>
     * </p>
     *
     * @param shader
     * @param lightList
     */
    protected void updateLightListUniforms(Shader shader, Geometry g, int numLights){
        if (numLights == 0) // this shader does not do lighting, ignore.
            return;

        LightList lightList = g.getWorldLightList();
        Uniform lightColor = shader.getUniform("g_LightColor");
        Uniform lightPos = shader.getUniform("g_LightPosition");
        lightColor.setVector4Length(numLights);
        lightPos.setVector4Length(numLights);

        for (int i = 0; i < numLights; i++){
            if (lightList.size() <= i){
                lightColor.setVector4InArray(0f, 0f, 0f, 0f, i);
                lightPos.setVector4InArray(0f, 0f, 0f, 0f, i);
            }else{
                Light l = lightList.get(i);
                ColorRGBA color = l.getColor();
                lightColor.setVector4InArray(color.getRed(),
                                             color.getGreen(),
                                             color.getBlue(),
                                             l.getType().getId(),
                                             i);

                switch (l.getType()){
                    case Directional:
                        DirectionalLight dl = (DirectionalLight) l;
                        Vector3f dir = dl.getDirection();
                        lightPos.setVector4InArray(dir.getX(), dir.getY(), dir.getZ(), -1, i);
                        break;
                    case Point:
                        PointLight pl = (PointLight) l;
                        Vector3f pos = pl.getPosition();
                        float invRadius = pl.getRadius();
                        if (invRadius != 0){
                            invRadius = 1f / invRadius;
                        }
                        lightPos.setVector4InArray(pos.getX(), pos.getY(), pos.getZ(), invRadius, i);
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown type of light: "+l.getType());
                }
            }
        }
    }

    protected void renderMultipassLighting(Shader shader, Geometry g, Renderer r){
//        if (r.getCaps().contains(Caps.MeshInstancing)){
//            r.applyRenderState(depthOnly);
//            r.setShader(shader);
//            r.renderMesh(g.getMesh(), g.getLodLevel(), 1);

//            int numLights = g.getWorldLightList().size();
//            updateLightListUniforms(shader, g, numLights);
//            r.applyRenderState(additiveLight);
//            r.setShader(shader);
//            r.renderMesh(g.getMesh(), g.getLodLevel(), numLights);
//        }else{
            LightList lightList = g.getWorldLightList();
            Uniform lightColor = shader.getUniform("g_LightColor");
            Uniform lightPos = shader.getUniform("g_LightPosition");

            for (int i = 0; i < lightList.size(); i++){
                if (i == 1){
                    r.applyRenderState(additiveLight);
                }

                Light l = lightList.get(i);
                ColorRGBA color = l.getColor();
                ColorRGBA color2;
                if (lightColor.getValue() != null){
                    color2 = (ColorRGBA) lightColor.getValue();
                }else{
                    color2 = new ColorRGBA();
                }
                color2.set(color);
                color2.a = l.getType().getId();
                lightColor.setValue(VarType.Vector4, color2);

                switch (l.getType()){
                    case Directional:
                        DirectionalLight dl = (DirectionalLight) l;
                        Vector3f dir = dl.getDirection();
                        Quaternion q1;
                        if (lightPos.getValue() != null){
                            q1 = (Quaternion) lightPos.getValue();
                        }else{
                            q1 = new Quaternion();
                        }
                        q1.set(dir.getX(), dir.getY(), dir.getZ(), -1);
                        lightPos.setValue(VarType.Vector4, q1);
                        break;
                    case Point:
                        PointLight pl = (PointLight) l;
                        Vector3f pos = pl.getPosition();
                        float invRadius = pl.getRadius();
                        if (invRadius != 0){
                            invRadius = 1f / invRadius;
                        }
                        Quaternion q2;
                        if (lightPos.getValue() != null){
                            q2 = (Quaternion) lightPos.getValue();
                        }else{
                            q2 = new Quaternion();
                        }
                        q2.set(pos.getX(), pos.getY(), pos.getZ(), invRadius);
                        lightPos.setValue(VarType.Vector4, q2);
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown type of light: "+l.getType());
                }

                r.setShader(shader);
                r.renderMesh(g.getMesh(), g.getLodLevel(), 1);
            }
//        }
    }

    public void selectTechnique(String name, RenderManager renderManager){
        // check if already created
        Technique tech = techniques.get(name);
        if (tech == null){
            // When choosing technique, we choose one that
            // supports all the caps.
            EnumSet<Caps> rendererCaps = renderManager.getRenderer().getCaps();

            if (name.equals("Default")){
                List<TechniqueDef> techDefs = def.getDefaultTechniques();
                for (TechniqueDef techDef : techDefs){
                    if (rendererCaps.containsAll(techDef.getRequiredCaps())){
                        // use the first one that supports all the caps
                        tech = new Technique(this, techDef);
                        techniques.put(name, tech);
                    }
                }
            }else{
                // create "special" technique instance
                TechniqueDef techDef = def.getTechniqueDef(name);
                if (techDef == null)
                    throw new IllegalArgumentException("For material "+def.getName()+", technique not found: "+name);

                if (!rendererCaps.containsAll(techDef.getRequiredCaps())){
                    throw new UnsupportedOperationException("The explicitly chosen technique '" + name + "' on material '" + def.getName() + "'\n" +
                                                            "requires caps " + techDef.getRequiredCaps() + " which are not" +
                                                            "supported by the video renderer");
                }

                tech = new Technique(this, techDef);
                techniques.put(name, tech);
            }
        }else if (technique == tech){
            // attempting to switch to an already
            // active technique.
            return;
        }

        technique = tech;
        tech.makeCurrent(def.getAssetManager());
    }

    private void autoSelectTechnique(RenderManager rm){
        if (technique == null){
            // XXX: hack warning, choose "FixedFunc" if GLSL100
            // not supported by renderer
            if (!rm.getRenderer().getCaps().contains(Caps.GLSL100)){
                selectTechnique("FixedFunc", rm);
            }else{
                selectTechnique("Default", rm);
            }
        }else if (technique.isNeedReload()){
            technique.makeCurrent(def.getAssetManager());
        }
    }

    /**
     * "Pre-load" the material, including textures and shaders, to the 
     * renderer.
     */
    public void preload(RenderManager rm){
        autoSelectTechnique(rm);

        Renderer r = rm.getRenderer();
        TechniqueDef techDef = technique.getDef();
        
        Collection<MatParam> params = paramValues.values();
        for (MatParam param : params){
            if (param instanceof MatParamTexture){
                MatParamTexture texParam = (MatParamTexture) param;
                r.setTexture(0, texParam.getTextureValue());
            }else{
                if (!techDef.isUsingShaders())
                    continue;
                
                technique.updateUniformParam(param.getName(),
                                             param.getVarType(),
                                             param.getValue(), true);
            }
        }

        Shader shader = technique.getShader();
        if (techDef.isUsingShaders())
            r.setShader(shader);
    }

    /**
     * Should be called after selectTechnique()
     * @param geom
     * @param r
     */
    public void render(Geometry geom, RenderManager rm){
        autoSelectTechnique(rm);



        Renderer r = rm.getRenderer();
        TechniqueDef techDef = technique.getDef();

        if (techDef.getLightMode() == LightMode.MultiPass
         && geom.getWorldLightList().size() == 0)
            return;

        if (techDef.getRenderState() != null){
            r.applyRenderState(techDef.getRenderState());
            if (additionalState != null)
                r.applyRenderState(additionalState);
        }else{
            if (additionalState != null)
                r.applyRenderState(additionalState);
            else
                r.applyRenderState(RenderState.DEFAULT);
        }
        if (rm.getForcedRenderState() != null)
            r.applyRenderState(rm.getForcedRenderState());

        // update camera and world matrices
        // NOTE: setWorldTransform should have been called already
        // XXX:
        if (techDef.isUsingShaders())
            rm.updateUniformBindings(technique.getWorldBindUniforms());

        // setup textures
//        Collection<MatParam> params = paramValues.values();
//        for (MatParam param : params){
        for (int i = 0; i < paramValues.size(); i++){
            MatParam param = paramValues.getValue(i);
            if (param instanceof MatParamTexture){
                MatParamTexture texParam = (MatParamTexture) param;
                r.setTexture(texParam.getUnit(), texParam.getTextureValue());
                if (techDef.isUsingShaders()){
                    technique.updateUniformParam(texParam.getName(),
                                                 texParam.getVarType(),
                                                 texParam.getUnit(), true);
                }
            }else{
                if (!techDef.isUsingShaders())
                    continue;
                
                technique.updateUniformParam(param.getName(),
                                             param.getVarType(),
                                             param.getValue(), true);
            }
        }

        Shader shader = technique.getShader();

        // send lighting information, if needed
        switch (techDef.getLightMode()){
            case Disable:
                r.setLighting(null);
                break;
            case SinglePass:
                updateLightListUniforms(shader, geom, 4);
                break;
            case FixedPipeline:
                r.setLighting(geom.getWorldLightList());
                break;
            case MultiPass:
                // NOTE: Special case!
                renderMultipassLighting(shader, geom, r);
                // very important, notice the return statement!
                return;
        }

        // upload and bind shader
        if (techDef.isUsingShaders())
            r.setShader(shader);
        
        r.renderMesh(geom.getMesh(), geom.getLodLevel(), 1);
    }

    public void write(JmeExporter ex) throws IOException{
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(def.getAssetName(), "material_def", null);
        oc.write(additionalState, "render_state", null);
        oc.write(transparent, "is_transparent", false);
        oc.writeStringSavableMap(paramValues, "parameters", null);
    }

    public void read(JmeImporter im) throws IOException{
        InputCapsule ic = im.getCapsule(this);
        String defName = ic.readString("material_def", null);
        def = (MaterialDef) im.getAssetManager().loadAsset(new AssetKey(defName));
        additionalState = (RenderState) ic.readSavable("render_state", null);
        transparent = ic.readBoolean("is_transparent", false);

        HashMap<String, MatParam> params = (HashMap<String, MatParam>) ic.readStringSavableMap("parameters", null);
        paramValues.putAll(params);
//        paramValues = (HashMap<String, MatParam>)

        // load the textures and update nextTexUnit
        for (MatParam param : paramValues.values()){
            if (param instanceof MatParamTexture){
                MatParamTexture texVal = (MatParamTexture) param;
                if (nextTexUnit < texVal.getUnit()+1){
                    nextTexUnit = texVal.getUnit()+1;
                }
            }
        }
    }

}
