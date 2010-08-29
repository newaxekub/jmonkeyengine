package com.jme3.material;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.shader.DefineList;
import com.jme3.shader.UniformBinding;
import com.jme3.shader.VarType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TechniqueDef implements Savable {

    public enum LightMode {
        Disable,
        SinglePass,
        MultiPass,
        FixedPipeline,
    }

    public enum ShadowMode {
        Disable,
        InPass,
        PostPass,
    }

    private String name;

    private String vertName;
    private String fragName;
    private String shaderLang;
    private DefineList presetDefines;
    private boolean usesShaders;

    private RenderState renderState;
    private LightMode lightMode   = LightMode.Disable;
    private ShadowMode shadowMode = ShadowMode.Disable;

    private HashMap<String, String> defineParams;
    private ArrayList<UniformBinding> worldBinds;
//    private final Map<String, Attribute> attribs = new HashMap<String, Attribute>();

    public TechniqueDef(String name){
        this.name = name == null ? "Default" : name;
    }

    /**
     * Do not use this constructor.
     */
    public TechniqueDef(){
    }

    public void write(JmeExporter ex) throws IOException{
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(name, "name", null);
        oc.write(vertName, "vertName", null);
        oc.write(fragName, "fragName", null);
        oc.write(shaderLang, "shaderLang", null);
        oc.write(presetDefines, "presetDefines", null);
        oc.write(lightMode, "lightMode", LightMode.Disable);
        oc.write(shadowMode, "shadowMode", ShadowMode.Disable);
        oc.write(renderState, "renderState", null);
        oc.write(usesShaders, "usesShaders", false);
        // TODO: Finish this when Map<String, String> export is available
//        oc.write(defineParams, "defineParams", null);
        // TODO: Finish this when List<Enum> export is available
//        oc.write(worldBinds, "worldBinds", null);
    }

    public void read(JmeImporter im) throws IOException{
        InputCapsule ic = im.getCapsule(this);
        name = ic.readString("name", null);
        vertName = ic.readString("vertName", null);
        fragName = ic.readString("fragName", null);
        shaderLang = ic.readString("shaderLang", null);
        presetDefines = (DefineList) ic.readSavable("presetDefines", null);
        lightMode = ic.readEnum("lightMode", LightMode.class, LightMode.Disable);
        shadowMode = ic.readEnum("shadowMode", ShadowMode.class, ShadowMode.Disable);
        renderState = (RenderState) ic.readSavable("renderState", null);
        usesShaders = ic.readBoolean("usesShaders", false);
    }

    public String getName(){
        return name;
    }

    public LightMode getLightMode() {
        return lightMode;
    }

    public void setLightMode(LightMode lightMode) {
        this.lightMode = lightMode;
    }

    public ShadowMode getShadowMode() {
        return shadowMode;
    }

    public void setShadowMode(ShadowMode shadowMode) {
        this.shadowMode = shadowMode;
    }

    public RenderState getRenderState() {
        return renderState;
    }

    public void setRenderState(RenderState renderState) {
        this.renderState = renderState;
    }

    public boolean isUsingShaders(){
        return usesShaders;
    }

    public void setShaderFile(String vert, String frag, String lang){
        this.vertName = vert;
        this.fragName = frag;
        this.shaderLang = lang;

        usesShaders = true;
    }

    public DefineList getShaderPresetDefines() {
        return presetDefines;
    }

    public String getShaderParamDefine(String paramName){
        if (defineParams == null)
            return null;
        
        return defineParams.get(paramName);
    }

    public void addShaderParamDefine(String paramName, String defineName){
        if (defineParams == null)
            defineParams = new HashMap<String, String>();

        defineParams.put(paramName, defineName);
    }

    public void addShaderPresetDefine(String defineName, VarType type, Object value){
        if (presetDefines == null)
            presetDefines = new DefineList();

        presetDefines.set(defineName, type, value);
    }

    public String getFragName() {
        return fragName;
    }

    public String getVertName() {
        return vertName;
    }

    public String getShaderLanguage() {
        return shaderLang;
    }

    public boolean addWorldParam(String name) {
        if (worldBinds == null){
            worldBinds = new ArrayList<UniformBinding>();
        }
        for (UniformBinding binding : UniformBinding.values()) {
            if (binding.name().equals(name)) {
                worldBinds.add(binding);
                return true;
            }
        }
        return false;
    }

//    public void addAttribute(String name) {
//        Attribute attrib = shader.getAttribute(name);
//        attribs.put(name, attrib);
//    }

    public List<UniformBinding> getWorldBindings() {
        return worldBinds;
    }

}
