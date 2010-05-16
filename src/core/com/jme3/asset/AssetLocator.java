package com.jme3.asset;

/**
 * <code>AssetLocator</code> is used to locate a resource based on an AssetKey.
 *
 * @author Kirill Vainer
 */
public interface AssetLocator {
    public void setRootPath(String rootPath);
    public AssetInfo locate(AssetManager manager, AssetKey key);
}
