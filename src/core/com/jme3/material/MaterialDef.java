package com.jme3.material;

import com.jme3.asset.AssetManager;
import com.jme3.shader.VarType;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MaterialDef {

    private static final Logger logger = Logger.getLogger(MaterialDef.class.getName());

    private String name;
    private String assetName;
    private AssetManager assetManager;
    private Map<String, TechniqueDef> techniques;
    private Map<String, MatParam> matParams;

    public MaterialDef(){
    }
    
    public MaterialDef(AssetManager assetManager, String name){
        this.assetManager = assetManager;
        this.name = name;
        techniques = new HashMap<String, TechniqueDef>();
        matParams = new HashMap<String, MatParam>();
    }

    public void setAssetName(String assetName){
        this.assetName = assetName;
    }

    public String getAssetName(){
        return assetName;
    }

    public AssetManager getAssetManager(){
        return assetManager;
    }

    public String getName(){
        return name;
    }

    public void addMaterialParam(VarType type, String name, Object value) {
        matParams.put(name, new MatParam(type, name, value));
    }
    
    public MatParam getMaterialParam(String name){
        return matParams.get(name);
    }

    public void addTechniqueDef(TechniqueDef technique){
        techniques.put(technique.getName(), technique);
    }

    public TechniqueDef getTechniqueDef(String name) {
        return techniques.get(name);
    }

}
