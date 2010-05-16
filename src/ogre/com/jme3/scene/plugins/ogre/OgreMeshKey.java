package com.jme3.scene.plugins.ogre;

import com.jme3.asset.AssetKey;

public class OgreMeshKey extends AssetKey {

    private OgreMaterialList materialList;

    public OgreMeshKey(String name, OgreMaterialList materialList){
        super(name);
        this.materialList = materialList;
    }

    public OgreMaterialList getMaterialList() {
        return materialList;
    }
    
}
