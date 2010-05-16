package com.jme3.asset;

import java.io.IOException;

/**
 * An interface for asset loaders.
 */
public interface AssetLoader {

    /**
     * Loads asset from the given input stream, parsing it into
     * an application-usable object.
     *
     * @return An object representing the resource.
     * @throws java.io.IOException If an I/O error occurs while loading
     */
    public Object load(AssetInfo assetInfo) throws IOException;
}
