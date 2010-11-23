package com.jme3.util;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;

public class SkyFactory {

    private static final Sphere sphereMesh = new Sphere(10, 10, 10, false, true);

    public static Spatial createSky(AssetManager assetManager, Texture texture, Vector3f normalScale, boolean sphereMap){
        Geometry sky = new Geometry("Sky", sphereMesh);
        sky.setQueueBucket(Bucket.Sky);
        sky.setCullHint(Spatial.CullHint.Never);

        Material skyMat = new Material(assetManager, "Common/MatDefs/Misc/Sky.j3md");
        skyMat.setTexture("m_Texture", texture);
        skyMat.setVector3("m_NormalScale", normalScale);
        if (sphereMap){
            skyMat.setBoolean("m_SphereMap", sphereMap);
        }
        sky.setMaterial(skyMat);
        
        
        return sky;
    }

    public static Spatial createSky(AssetManager assetManager, Texture texture, boolean sphereMap){
        return createSky(assetManager, texture, Vector3f.UNIT_XYZ, sphereMap);
    }

    public static Spatial createSky(AssetManager assetManager, String textureName, boolean sphereMap){
        TextureKey key = new TextureKey(textureName, true);
        key.setGenerateMips(true);
        key.setAsCube(!sphereMap);
        Texture tex = assetManager.loadTexture(key);
        return createSky(assetManager, tex, sphereMap);
    }
}
