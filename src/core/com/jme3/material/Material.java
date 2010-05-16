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
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

public class Material implements Cloneable, Savable {

    private static final RenderState additiveLight = new RenderState();

    static {
        additiveLight.setBlendMode(RenderState.BlendMode.Additive);
    }

    private MaterialDef def;
    private HashMap<String, MatParam> paramValues = new HashMap<String, MatParam>();

    private Technique technique;
    private HashMap<String, Technique> techniques = new HashMap<String, Technique>();
    private int nextTexUnit = 0;
    private RenderState additionalState = null;
    private boolean transparent = false;
    private boolean recievesShadows = false;

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
            mat.paramValues = new HashMap<String, MatParam>(paramValues);
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

    public boolean isRecievesShadows() {
        return recievesShadows;
    }

    public void setRecievesShadows(boolean recievesShadows) {
        this.recievesShadows = recievesShadows;
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

    public void selectTechnique(String name){
        // check if already created
        Technique tech = techniques.get(name);
        if (tech == null){
            // create technique instance
            TechniqueDef techDef = def.getTechniqueDef(name);
            if (techDef == null)
                throw new IllegalArgumentException("For material "+def.getName()+", technique not found: "+name);
            tech = new Technique(this, techDef);
            techniques.put(name, tech);
        }else if (technique == tech){
            // attempting to switch to an already
            // active technique.
            return;
        }
        technique = tech;
        tech.makeCurrent(def.getAssetManager());
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

    public void setParam(String name, VarType type, Object value){
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
            return;
        
        MatParamTexture val = getTextureParam(name);
        if (val == null)
            paramValues.put(name, new MatParamTexture(type, name, value, nextTexUnit++));
        else
            val.setTextureValue(value);

        if (technique != null){
            technique.notifySetParam(name, type, nextTexUnit-1);
        }
    }

    public void setTexture(String name, Texture value){
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

    public void setMatrix4(String name, Matrix4f value) {
        setParam(name, VarType.Matrix4, value);
    }
    
    public void setBoolean(String name, boolean value){
        setParam(name, VarType.Boolean, value);
    }

    public void setFloat(String name, float value){
        setParam(name, VarType.Float, value);
    }

    public void setInt(String name, int value){
        setParam(name, VarType.Int, value);
    }

    public void setColor(String name, ColorRGBA value){
        setParam(name, VarType.Vector4, value);
    }

    public void setVector2(String name, Vector2f value) {
        setParam(name, VarType.Vector2, value);
    }
    
    public void setVector3(String name, Vector3f value) {
        setParam(name, VarType.Vector3, value);
    }



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
    public void updateLightListUniforms(Shader shader, Geometry g, int numLights){
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

    private void renderMultipassLighting(Shader shader, Geometry g, Renderer r){
        LightList lightList = g.getWorldLightList();
        Uniform lightColor = shader.getUniform("g_LightColor");
        Uniform lightPos = shader.getUniform("g_LightPosition");

        for (int i = 0; i < lightList.size(); i++){
            if (i == 1){
                r.applyRenderState(additiveLight);
            }

            Light l = lightList.get(i);
            ColorRGBA color = l.getColor();
            ColorRGBA color2 = new ColorRGBA(color);
            color2.a = l.getType().getId();
            lightColor.setValue(VarType.Vector4, color2);

            switch (l.getType()){
                case Directional:
                    DirectionalLight dl = (DirectionalLight) l;
                    Vector3f dir = dl.getDirection();
                    Quaternion q1 = new Quaternion(dir.getX(), dir.getY(), dir.getZ(), -1);
                    lightPos.setValue(VarType.Vector4, q1);
                    break;
                case Point:
                    PointLight pl = (PointLight) l;
                    Vector3f pos = pl.getPosition();
                    float invRadius = pl.getRadius();
                    if (invRadius != 0){
                        invRadius = 1f / invRadius;
                    }
                    Quaternion q2 = new Quaternion(pos.getX(), pos.getY(), pos.getZ(), invRadius);
                    lightPos.setValue(VarType.Vector4, q2);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown type of light: "+l.getType());
            }

            r.setShader(shader);
            r.renderMesh(g.getMesh(), g.getLodLevel(), 1);
        }
    }

    /**
     * Should be called after selectTechnique()
     * @param geom
     * @param r
     */
    public void render(Geometry geom, RenderManager rm){
        if (technique == null){
            // XXX: hack warning, choose "FixedFunc" if GLSL100
            // not supported by renderer
            if (!rm.getRenderer().getCaps().contains(Caps.GLSL100)){
                selectTechnique("FixedFunc");
            }else{
                selectTechnique("Default");
            }
        }else if (technique.isNeedReload()){
            technique.makeCurrent(def.getAssetManager());
        }

        Renderer r = rm.getRenderer();
        TechniqueDef techDef = technique.getDef();
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

        
        // update camera and world matrices
        // NOTE: setWorldTransform should have been called already
        // XXX:
        if (techDef.isUsingShaders())
            rm.updateUniformBindings(technique.getWorldBindUniforms());

        // setup textures
        Collection<MatParam> params = paramValues.values();
        for (MatParam param : params){
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
        paramValues = (HashMap<String, MatParam>) ic.readStringSavableMap("parameters", null);

        // load the textures and update nextTexUnit
        for (MatParam param : paramValues.values()){
            if (param instanceof MatParamTexture){
                MatParamTexture texVal = (MatParamTexture) param;
//                texVal.tryLoadFromKey(im.getAssetManager());
                if (nextTexUnit < texVal.getUnit()+1){
                    nextTexUnit = texVal.getUnit()+1;
                }
            }
        }
    }

}
