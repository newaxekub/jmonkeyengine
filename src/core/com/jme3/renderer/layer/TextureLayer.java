/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.renderer.layer;

import com.jme3.texture.Texture;

/**
 * Renderer layer for handling textures
 */
public interface TextureLayer {

     /**
     * Prepares the texture for use and uploads its image data if neceessary.
     */
    public void updateTextureData(Texture tex);

    /**
     * Sets the texture to use for the given texture unit.
     */
    public void setTexture(int unit, Texture tex);

    /**
     * Clears all set texture units
     * @see #setTexture
     */
    public void clearTextureUnits();

    /**
     * Deletes a texture from the GPU.
     * @param tex
     */
    public void deleteTexture(Texture tex);

}
