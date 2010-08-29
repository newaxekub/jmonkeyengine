package com.jme3.scene.plugins.ogre;

import com.jme3.asset.ModelKey;
import com.jme3.material.MaterialList;

public class OgreMeshKey extends ModelKey {

    private MaterialList materialList;

    public OgreMeshKey(String name, MaterialList materialList){
        super(name);
        this.materialList = materialList;
    }

    public OgreMeshKey(){
        super();
    }

    public MaterialList getMaterialList() {
        return materialList;
    }

}
