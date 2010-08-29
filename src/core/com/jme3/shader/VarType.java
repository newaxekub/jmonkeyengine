package com.jme3.shader;

public enum VarType {

    Float,
    Vector2,
    Vector3,
    Vector4,

    FloatArray(true,false),
    Vector2Array(true,false),
    Vector3Array(true,false),
    Vector4Array(true,false),

    Boolean,

    Matrix3(true,false),
    Matrix4(true,false),

    Matrix3Array(true,false),
    Matrix4Array(true,false),

    TextureBuffer(false,true),
    Texture2D(false,true),
    Texture3D(false,true),
    TextureArray(false,true),
    TextureCubeMap(false,true),
    Int;

    private boolean usesMultiData = false;
    private boolean textureType = false;

    VarType(){
    }

    VarType(boolean multiData, boolean textureType){
        usesMultiData = multiData;
        this.textureType = textureType;
    }

    public boolean isTextureType() {
        return textureType;
    }

    public boolean usesMultiData() {
        return usesMultiData;
    }
    
}
