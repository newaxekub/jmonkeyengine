package com.jme3.asset;

import java.io.InputStream;

/**
 * The result of locating an asset through an AssetKey. Provides
 * a means to read the asset data through an InputStream.
 *
 * @author Kirill Vainer
 */
public abstract class AssetInfo {

    protected AssetManager manager;
    protected AssetKey key;

    public AssetInfo(AssetManager manager, AssetKey key) {
        this.manager = manager;
        this.key = key;
    }

    public AssetKey getKey() {
        return key;
    }

    public AssetManager getManager() {
        return manager;
    }

    @Override
    public String toString(){
        return getClass().getName() + "[" + "key=" + key + "]";
    }

    public abstract InputStream openStream();

}
