package com.jme3.shader;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class DefineList implements Savable {

    private final SortedMap<String, String> defines = new TreeMap<String, String>();
    private String compiled = null;

    public void write(JmeExporter ex) throws IOException{
        OutputCapsule oc = ex.getCapsule(this);
        // TODO: Fix exporting of defines
        getCompiled();
        oc.write(compiled, "compiled", null);
    }

    public void read(JmeImporter im) throws IOException{
        InputCapsule ic = im.getCapsule(this);
        compiled = ic.readString(compiled, null);
    }

    public void clear() {
        defines.clear();
        compiled = "";
    }

    public String get(String key){
        compiled = null;
        return defines.get(key);
    }

//    public void set(String key, String val){
//        compiled = null;
//        defines.put(key, val);
//    }

    public void set(String key, VarType type, Object val){
        compiled = null;
        if (val == null){
            defines.remove(key);
            return;
        }

        switch (type){
            case Boolean:
                if ( ((Boolean) val).booleanValue() )
                    defines.put(key, "1");
                break;
            case Float:
            case Int:
                defines.put(key, val.toString());
                break;
            default:
                defines.put(key, "1");
                break;
        }
    }

    public void remove(String key){
        compiled = null;
        defines.remove(key);
    }

    public void addFrom(DefineList other){
        compiled = null;
        if (other == null)
            return;
        
        defines.putAll(other.defines);
    }

    public String getCompiled(){
        if (compiled == null){
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : defines.entrySet()){
                sb.append("#define ").append(entry.getKey()).append(" ");
                sb.append(entry.getValue()).append('\n');
            }
            compiled = sb.toString();
        }
        return compiled;
    }

}
