package com.jme3.shader;

import com.jme3.asset.AssetKey;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import java.io.IOException;

public class ShaderKey extends AssetKey<Shader> {

    protected String fragName;
    protected DefineList defines;
    protected String language;

    public ShaderKey(){
    }

    public ShaderKey(String vertName, String fragName, DefineList defines, String lang){
        super(vertName);
        this.fragName = fragName;
        this.defines = defines;
        this.language = lang;
    }

    @Override
    public String toString(){
        return "V="+name + " F=" + fragName + (defines != null ? defines : "");
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

    @Override
    public void write(JmeExporter ex) throws IOException{
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(fragName, "fragment_name", null);
        oc.write(language, "language", null);
    }

    @Override
    public void read(JmeImporter im) throws IOException{
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        fragName = ic.readString("fragment_name", null);
        language = ic.readString("language", null);
    }

}
