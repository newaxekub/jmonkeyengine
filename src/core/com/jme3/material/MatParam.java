/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.material;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.shader.Uniform;
import com.jme3.shader.VarType;
import java.io.IOException;

public class MatParam implements Savable, Cloneable {

    protected VarType type;
    protected String name;
    protected Object value;
    protected Uniform uniform;

    public MatParam(VarType type, String name, Object value){
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public MatParam(){
    }

    public VarType getVarType() {
        return type;
    }

    public String getName(){
        return name;
    }

    public Object getValue(){
        return value;
    }

    public void setValue(Object value){
        this.value = value;
    }

    public Uniform getUniform() {
        return uniform;
    }

    public void setUniform(Uniform uniform) {
        this.uniform = uniform;
    }

    @Override
    public MatParam clone(){
        try{
            MatParam param = (MatParam) super.clone();
            return param;
        }catch (CloneNotSupportedException ex){
            throw new AssertionError();
        }
    }

    public void write(JmeExporter ex) throws IOException{
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(type, "varType", null);
        oc.write(name, "name", null);
        if (value instanceof Savable){
            Savable s = (Savable) value;
            oc.write(s, "value_savable", null);
        }else if (value instanceof Float){
            Float f = (Float) value;
            oc.write(f.floatValue(), "value_float", 0f);
        }else if (value instanceof Integer){
            Integer i = (Integer) value;
            oc.write(i.intValue(), "value_int", 0);
        }else if (value instanceof Boolean){
            Boolean b = (Boolean) value;
            oc.write(b.booleanValue(), "value_bool", false);
        }
    }

    public void read(JmeImporter im) throws IOException{
        InputCapsule ic = im.getCapsule(this);
        type = ic.readEnum("varType", VarType.class, null);
        name = ic.readString("name", null);
        switch (getVarType()){
            case Boolean:
                value = ic.readBoolean("value_bool", false);
                break;
            case Float:
                value = ic.readFloat("value_float", 0f);
                break;
            case Int:
                value = ic.readInt("value_int", 0);
                break;
            default:
                value = ic.readSavable("value_savable", null);
                break;
        }
    }

    @Override
    public boolean equals(Object other){
        if (!(other instanceof MatParam))
            return false;

        MatParam otherParam = (MatParam) other;
        return otherParam.type == type &&
               otherParam.name.equals(name);
    }

    @Override
    public String toString(){
        return type.name()+" "+name;
    }
}

