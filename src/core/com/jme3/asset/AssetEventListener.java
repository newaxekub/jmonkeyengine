package com.jme3.asset;

/**
 * Interface for listening to various events happening inside AssetManager.
 * 
 * @author Kirill Vainer
 */
public interface AssetEventListener {

    /**
     * Called when an asset has been successfuly loaded (e.g: loaded from
     * file system and parsed).
     *
     * @param key the AssetKey for the asset loaded.
     */
    public void assetLoaded(AssetKey key);

    /**
     * Called when an asset has been requested (e.g any of the load*** methods
     * in AssetManager are called).
     * In contrast to the assetLoaded() method, this one will be called even
     * if the asset has failed to load, or if it was retrieved from the cache.
     *
     * @param key
     */
    public void assetRequested(AssetKey key);

}
