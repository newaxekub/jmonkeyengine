package com.jme3.material;

import com.jme3.asset.AssetManager;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.shader.DefineList;
import com.jme3.shader.Shader;
import com.jme3.shader.ShaderKey;
import com.jme3.shader.Uniform;
import com.jme3.shader.UniformBinding;
import com.jme3.shader.VarType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a technique instance.
 */
public class Technique implements Savable {

    private static final Logger logger = Logger.getLogger(Technique.class.getName());

    private TechniqueDef def;
    private Material owner;
    private ArrayList<Uniform> worldBindUniforms;
    private DefineList defines;
    private Shader shader;
    private boolean needReload = true;

    public Technique(Material owner, TechniqueDef def){
        this.owner = owner;
        this.def = def;
        if (def.isUsingShaders()){
            this.worldBindUniforms = new ArrayList<Uniform>();
            this.defines = new DefineList();
        }
    }

    public Technique(){
    }

    public void write(JmeExporter ex) throws IOException{
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(def, "def", null);
        // TODO:
        // oc.write(owner, "owner", null);
        oc.writeSavableArrayList(worldBindUniforms, "worldBindUniforms", null);
        oc.write(defines, "defines", null);
        oc.write(shader, "shader", null);
    }

    public void read(JmeImporter im) throws IOException{
        InputCapsule ic = im.getCapsule(this);
        def = (TechniqueDef) ic.readSavable("def", null);
        worldBindUniforms = ic.readSavableArrayList("worldBindUniforms", null);
        defines = (DefineList) ic.readSavable("defines", null);
        shader = (Shader) ic.readSavable("shader", null);
        if (shader != null)
            owner.updateUniformLinks();
    }

    public TechniqueDef getDef() {
        return def;
    }

    public Shader getShader() {
        return shader;
    }

    public List<Uniform> getWorldBindUniforms() {
        return worldBindUniforms;
    }

    /**
     * @param paramName
     */
    public void notifySetParam(String paramName, VarType type, Object value){
        String defineName = def.getShaderParamDefine(paramName);
        if (defineName != null){
            defines.set(defineName, type, value);
            needReload = true;
        }
        if (shader != null){
            updateUniformParam(paramName, type, value);
        }
    }

    void updateUniformParam(String paramName, VarType type, Object value, boolean ifNotOwner){
        Uniform u = shader.getUniform(paramName);
//        if (ifNotOwner && u.getLastChanger() == owner)
//            return;

        switch (type){
            case Texture2D: // fall intentional
            case Texture3D:
            case TextureArray:
            case TextureCubeMap:
            case Int:     u.setValue(VarType.Int, value); break;
            default:      u.setValue(type, value); break;
        }
//        u.setLastChanger(owner);
    }

    void updateUniformParam(String paramName, VarType type, Object value){
        updateUniformParam(paramName, type, value, false);
    }

    public boolean isNeedReload(){
        return needReload;
    }

    /**
     * Prepares the technique for use by loading the shader and setting
     * the proper defines based on material parameters.
     */
    public void makeCurrent(AssetManager manager){
        // check if reload is needed..
        if (def.isUsingShaders()){
            DefineList newDefines = new DefineList();
            Collection<MatParam> params = owner.getParams();
            for (MatParam param : params){
                String defineName = def.getShaderParamDefine(param.getName());
                if (defineName != null){
                    newDefines.set(defineName, param.getVarType(), param.getValue());
                }
            }

            if (!needReload && defines.getCompiled().equals(newDefines.getCompiled())){
                newDefines = null;
                // defines have not been changed..
            }else{
                defines.clear();
                defines.addFrom(newDefines);
                // defines changed, recompile needed
                loadShader(manager);
            }
        }
    }

    private void loadShader(AssetManager manager){
        // recompute define list
        DefineList allDefines = new DefineList();
        allDefines.addFrom(def.getShaderPresetDefines());
        allDefines.addFrom(defines);

        ShaderKey key = new ShaderKey(def.getVertName(),
                                                  def.getFragName(),
                                                  allDefines,
                                                  def.getShaderLanguage());
        shader = manager.loadShader(key);
        if (shader == null){
            logger.warning("Failed to reload shader!");
            return;
        }

        // refresh the uniform links
        owner.updateUniformLinks();

        // register the world bound uniforms
        worldBindUniforms.clear();
        for (UniformBinding binding : def.getWorldBindings()){
            Uniform uniform = shader.getUniform("g_"+binding.name());
            uniform.setBinding(binding);
            if (uniform != null)
                worldBindUniforms.add(uniform);
        }

        needReload = false;
    }

}
