package com.jme3.ui;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture2D;

/**
 * A <code>Picture</code> represents a 2D image drawn on the screen.
 * It can be used to represent sprites or other background elements.
 *
 * @author Momoko_Fan
 */
public class Picture extends Geometry {

    private float width;
    private float height;

    public Picture(String name, boolean flipY){
        super(name, new Quad(1, 1, flipY));
        setQueueBucket(Bucket.Gui);
        setCullHint(CullHint.Never);
    }

    public Picture(String name){
        this(name, false);
    }

    public Picture(){
    }

    public void setWidth(float width){
        this.width = width;
        setLocalScale(new Vector3f(width, height, 1f));
    }

    public void setHeight(float height){
        this.height = height;
        setLocalScale(new Vector3f(width, height, 1f));
    }

    public void setPosition(float x, float y){
        setLocalTranslation(x, y, 0);
    }

    public void setImage(AssetManager manager, String imgName, boolean useAlpha){
        TextureKey key = new TextureKey(imgName, true);
        Texture2D tex = (Texture2D) manager.loadTexture(key);
        setTexture(manager, tex, useAlpha);
    }

    public void setTexture(AssetManager manager, Texture2D tex, boolean useAlpha){
        if (getMaterial() == null){
            Material mat = new Material(manager, "Common/MatDefs/Gui/Gui.j3md");
            mat.setColor("m_Color", ColorRGBA.White);
            setMaterial(mat);
        }
        material.getAdditionalRenderState().setBlendMode(useAlpha ? BlendMode.Alpha : BlendMode.Off);
        material.setTexture("m_Texture", tex);
    }

}
