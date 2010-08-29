package com.jme3.asset;

import com.jme3.scene.Spatial;

public class ModelKey extends AssetKey<Spatial> {

    public ModelKey(String name){
        super(name);
    }

    public ModelKey(){
        super();
    }

    @Override
    public Object createClonedInstance(Object asset){
        Spatial model = (Spatial) asset;
        return model.clone();
    }

}
