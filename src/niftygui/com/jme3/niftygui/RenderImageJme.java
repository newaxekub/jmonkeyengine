package com.jme3.niftygui;

import com.jme3.asset.TextureKey;
import com.jme3.texture.Image;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture2D;
import de.lessvoid.nifty.spi.render.RenderImage;

public class RenderImageJme implements RenderImage {

    private NiftyJmeDisplay display;
    private Texture2D texture;
    private Image image;

    public RenderImageJme(String filename, boolean linear, NiftyJmeDisplay display){
        this.display = display;

        TextureKey key = new TextureKey(filename, true);

        key.setAnisotropy(0);
        key.setAsCube(false);
        key.setGenerateMips(false);
        
        texture = (Texture2D) display.getAssetManager().loadTexture(key);
        
//        if (linear){
            texture.setMagFilter(MagFilter.Bilinear);
            texture.setMinFilter(MinFilter.BilinearNoMipMaps);
//        }else{
//            texture.setMagFilter(MagFilter.Nearest);
//            texture.setMinFilter(MinFilter.NearestNoMipMaps);
//        }

        this.image = texture.getImage();
    }

    public Texture2D getTexture(){
        return texture;
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public void dispose() {
    }
}
