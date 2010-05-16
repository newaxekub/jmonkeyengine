package com.jme3.asset;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.TextureCubeMap;
import java.io.IOException;
import java.nio.ByteBuffer;

public class TextureKey extends AssetKey {

    private boolean generateMips;
    private boolean flipY;
    private boolean asCube;
    private int anisotropy;

    public TextureKey(String name, boolean flipY){
        super(name);
        this.flipY = flipY;
    }

    public TextureKey(String name){
        super(name);
        this.flipY = true;
    }

    public TextureKey(){
    }

    public Object createClonedInstance(Object asset){
        Texture tex = (Texture) asset;
        return tex.createSimpleClone();
    }

    @Override
    public Object postProcess(Object asset){
        Image img = (Image) asset;
        if (img == null)
            return null;

        Texture tex;
        if (isAsCube()){
            if (isFlipY()){
                // also flip -y and +y image in cubemap
                ByteBuffer pos_y = img.getData(2);
                img.setData(2, img.getData(3));
                img.setData(3, pos_y);
            }
            tex = new TextureCubeMap();
        }else{
            tex = new Texture2D();
        }

        // enable mipmaps if image has them
        // or generate them if requested by user
        if (img.hasMipmaps() || isGenerateMips())
            tex.setMinFilter(Texture.MinFilter.Trilinear);

        tex.setTextureKey(this);
        tex.setAnisotropicFilter(getAnisotropy());
        tex.setName(getName());
        tex.setImage(img);
        return tex;
    }

    public boolean isFlipY() {
        return flipY;
    }

    public int getAnisotropy() {
        return anisotropy;
    }

    public void setAnisotropy(int anisotropy) {
        this.anisotropy = anisotropy;
    }

    public boolean isAsCube() {
        return asCube;
    }

    public void setAsCube(boolean asCube) {
        this.asCube = asCube;
    }

    public boolean isGenerateMips() {
        return generateMips;
    }

    public void setGenerateMips(boolean generateMips) {
        this.generateMips = generateMips;
    }

    public void write(JmeExporter ex) throws IOException{
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(flipY, "flip_y", false);
        oc.write(generateMips, "generate_mips", false);
        oc.write(asCube, "as_cubemap", false);
        oc.write(anisotropy, "anisotropy", 0);
    }

    @Override
    public void read(JmeImporter im) throws IOException{
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        flipY = ic.readBoolean("flip_y", false);
        generateMips = ic.readBoolean("generate_mips", false);
        asCube = ic.readBoolean("as_cubemap", false);
        anisotropy = ic.readInt("anisotropy", 0);
    }
}
