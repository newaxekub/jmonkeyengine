package com.jme3.shader;

import com.jme3.asset.AssetKey;

public class ShaderKey extends AssetKey {

    protected final String fragName;
    protected final DefineList defines;
    protected final String language;

    public ShaderKey(String vertName, String fragName, DefineList defines, String lang){
        super(vertName);
        this.fragName = fragName;
        this.defines = defines;
        this.language = lang;
    }

    @Override
    public String toString(){
        return name + ":" + fragName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }

        final ShaderKey other = (ShaderKey) obj;
        if (name.equals(other.name) && fragName.equals(other.fragName)){
//            return true;
            if (defines != null && other.defines != null)
                return defines.getCompiled().equals(other.defines.getCompiled());
            else if (defines != null || other.defines != null)
                return false;
            else
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + name.hashCode();
        hash = 41 * hash + fragName.hashCode();
        hash = 41 * hash + (defines != null ? defines.getCompiled().hashCode() : 0);
        return hash;
    }

    public DefineList getDefines() {
        return defines;
    }

    public String getVertName(){
        return name;
    }

    public String getFragName() {
        return fragName;
    }

    public String getLanguage() {
        return language;
    }
    
}
