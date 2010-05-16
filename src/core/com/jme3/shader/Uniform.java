package com.jme3.shader;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;
import java.io.IOException;
import java.nio.FloatBuffer;

public class Uniform extends ShaderVariable {

    /**
     * Currently set value of the uniform.
     */
    protected Object value = null;
    protected FloatBuffer multiData = null;

    /**
     * Type of uniform
     */
    protected VarType varType;

    /**
     * Binding to a renderer value, or null if user-defined uniform
     */
    protected UniformBinding binding;

    protected Object lastChanger = null;

    @Override
    public void write(JmeExporter ex) throws IOException{
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(varType, "varType", null);
        oc.write(binding, "binding", null);
        switch (varType){
            case Boolean:
                oc.write( ((Boolean)value).booleanValue(), "valueBoolean", false );
                break;
            case Float:
                oc.write( ((Float)value).floatValue(), "valueFloat", 0);
                break;
            case FloatArray:
                oc.write( (FloatBuffer)value, "valueFloatArray", null);
                break;
            case Int:
                oc.write( ((Integer)value).intValue(), "valueInt", 0);
                break;
            case Matrix3:
                oc.write( (Matrix3f)value, "valueMatrix3", null);
                break;
            case Matrix3Array:
            case Matrix4Array:
            case Vector2Array:
                throw new UnsupportedOperationException("Come again?");
            case Matrix4:
                oc.write( (Matrix4f)value, "valueMatrix4", null);
                break;
            case Vector2:
                oc.write( (Vector2f)value, "valueVector2", null);
                break;
            case Vector3:
                oc.write( (Vector3f)value, "valueVector3", null);
                break;
            case Vector3Array:
                oc.write( (FloatBuffer)value, "valueVector3Array", null);
                break;
            case Vector4:
                oc.write( (ColorRGBA)value, "valueVector4", null);
                break;
            case Vector4Array:
                oc.write( (FloatBuffer)value, "valueVector4Array", null);
                break;
        }
    }

    @Override
    public void read(JmeImporter im) throws IOException{
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        varType = ic.readEnum("varType", VarType.class, null);
        binding = ic.readEnum("binding", UniformBinding.class, null);
        switch (varType){
            case Boolean:
                value = ic.readBoolean("valueBoolean", false);
                break;
            case Float:
                value = ic.readFloat("valueFloat", 0);
                break;
            case FloatArray:
                value = ic.readFloatBuffer("valueFloatArray", null);
                break;
            case Int:
                value = ic.readInt("valueInt", 0);
                break;
            case Matrix3:
                multiData = ic.readFloatBuffer("valueMatrix3", null);
                value = multiData;
                break;
            case Matrix4:
                multiData = ic.readFloatBuffer("valueMatrix4", null);
                value = multiData;
                break;
            case Vector2:
                value = ic.readSavable("valueVector2", null);
                break;
            case Vector3:
                value = ic.readSavable("valueVector3", null);
                break;
            case Vector3Array:
                value = ic.readFloatBuffer("valueVector3Array", null);
                break;
            case Vector4:
                value = ic.readSavable("valueVector4", null);
                break;
            case Vector4Array:
                value = ic.readFloatBuffer("valueVector4Array", null);
                break;
        }
    }

    public void setBinding(UniformBinding binding){
        this.binding = binding;
    }

    public UniformBinding getBinding(){
        return binding;
    }

    public VarType getVarType() {
        return varType;
    }

    public Object getValue(){
        return value;
    }

    public void setLastChanger(Object lastChanger){
        this.lastChanger = lastChanger;
    }

    public Object getLastChanger(){
        return lastChanger;
    }

    public void setValue(VarType type, Object value){
        if (location == -1)
            return;

        if (varType != null && varType != type)
            throw new IllegalArgumentException("Expected a "+varType.name()+" value!");

        if (value == null)
            throw new NullPointerException();

        switch (type){
            case Matrix3:
                Matrix3f m3 = (Matrix3f) value;
                if (multiData == null)
                    multiData = BufferUtils.createFloatBuffer(9);
                else{
                    m3.fillFloatBuffer(multiData, true);
                    multiData.clear();
                }
                break;
            case Matrix4:
                Matrix4f m4 = (Matrix4f) value;
                if (multiData == null)
                    multiData = BufferUtils.createFloatBuffer(16);
                else{
                    m4.fillFloatBuffer(multiData, true);
                    multiData.clear();
                }
                break;
            case FloatArray:
                float[] fa = (float[]) value;
                if (multiData == null)
                    multiData = BufferUtils.createFloatBuffer(fa);
                else{
                    multiData = BufferUtils.ensureLargeEnough(multiData, fa.length);
                    multiData.clear();
                    multiData.put(fa).clear();
                }
                break;
            case Vector2Array:
                Vector2f[] v2a = (Vector2f[]) value;
                if (multiData == null)
                    multiData = BufferUtils.createFloatBuffer(v2a);
                else{
                    multiData = BufferUtils.ensureLargeEnough(multiData, v2a.length * 2);
                    multiData.clear();
                    for (int i = 0; i < v2a.length; i++)
                        BufferUtils.setInBuffer(v2a[i], multiData, i);
                    multiData.clear();
                }
                break;
            case Vector3Array:
                Vector3f[] v3a = (Vector3f[]) value;
                if (multiData == null)
                    multiData = BufferUtils.createFloatBuffer(v3a);
                else{
                    multiData = BufferUtils.ensureLargeEnough(multiData, v3a.length * 3);
                    multiData.clear();
                    for (int i = 0; i < v3a.length; i++)
                        BufferUtils.setInBuffer(v3a[i], multiData, i);
                    multiData.clear();
                }
                break;
            case Vector4Array:
                Quaternion[] v4a = (Quaternion[]) value;
                if (multiData == null)
                    multiData = BufferUtils.createFloatBuffer(v4a);
                else{
                    multiData = BufferUtils.ensureLargeEnough(multiData, v4a.length * 4);
                    multiData.clear();
                    for (int i = 0; i < v4a.length; i++)
                        BufferUtils.setInBuffer(v4a[i], multiData, i);
                    multiData.clear();
                }
                break;
            case Matrix3Array:
                Matrix3f[] m3a = (Matrix3f[]) value;
                throw new UnsupportedOperationException();
//                break;
            case Matrix4Array:
                Matrix4f[] m4a = (Matrix4f[]) value;
                throw new UnsupportedOperationException();
//                break;
            default:
                this.value = value;
                break;
        }

        if (multiData != null)
            this.value = multiData;
        
        varType = type;
        updateNeeded = true;
    }

    public void setVector4Length(int length){
        if (location == -1)
            return;

        FloatBuffer fb = (FloatBuffer) value;
        if (fb == null || fb.capacity() < length){
            value = BufferUtils.createFloatBuffer(length * 4);
        }

        varType = VarType.Vector4Array;
        updateNeeded = true;
    }

    public void setVector4InArray(float x, float y, float z, float w, int index){
        if (location == -1)
            return;

        if (varType != null && varType != VarType.Vector4Array)
            throw new IllegalArgumentException("Expected a "+varType.name()+" value!");

        FloatBuffer fb = (FloatBuffer) value;
        fb.position(index * 4);
        fb.put(x).put(y).put(z).put(w);
        fb.rewind();
        updateNeeded = true;
    }
    
    public boolean isUpdateNeeded(){
        return updateNeeded;
    }

    public void clearUpdateNeeded(){
        updateNeeded = false;
    }

    public void reset(){
        lastChanger = null;
        location = -2;
        updateNeeded = true;
    }

}
