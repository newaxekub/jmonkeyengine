package com.jme3.asset;

import com.jme3.audio.AudioKey;
import com.jme3.audio.AudioData;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.scene.Spatial;
import com.jme3.shader.Shader;
import com.jme3.shader.ShaderKey;
import com.jme3.texture.Texture;

/**
 * <code>AssetManager</code> provides an interface for managing the data assets
 * of a jME3 application.
 */
public interface AssetManager {

    /**
     * Registers a loader for the given extensions.
     * @param loaderClassName
     * @param extensions
     */
    public void registerLoader(String loaderClassName, String ... extensions);
    public void registerLocator(String rootPath, String locatorClassName);


    public void registerLoader(Class<? extends AssetLoader> loaderClass, String ... extensions);
    public void registerLocator(String rootPath, Class<? extends AssetLocator> locatorClass);

    public void setAssetEventListener(AssetEventListener listener);

    /**
     * Load an asset from a key, the asset will be located
     * by one of the {@link AssetLocator} implementations provided in the
     * {@link AssetManager#registerLocator(java.lang.String, java.lang.Class) }
     * call. If located successfully, it will be loaded via the the appropriate
     * {@link AssetLoader} implementation based on the file's extension, as
     * specified in the call 
     * {@link AssetManager#registerLoader(java.lang.Class, java.lang.String[]) }.
     *
     * @param <T> The object type that will be loaded from the AssetKey instance.
     * @param key The AssetKey
     * @return The loaded asset, or null if it was failed to be located
     * or loaded.
     */
    public <T> T loadAsset(AssetKey<T> key);

    /**
     * Load a named asset by name.
     * @param name
     * @return
     */
    public Object loadAsset(String name);

    /**
     * Loads texture file, supported types are BMP, JPG, PNG, GIF,
     * TGA and DDS.
     *
     * @param key
     * @return
     */
    public Texture loadTexture(TextureKey key);

    /**
     * Loads texture file, supported types are BMP, JPG, PNG, GIF,
     * TGA and DDS.
     *
     * @param name
     * @return
     */
    public Texture loadTexture(String name);

    /**
     * Load audio file, supported types are WAV or OGG.
     * @param key
     * @return
     */
    public AudioData loadAudio(AudioKey key);

    /**
     * Load audio file, supported types are WAV or OGG.
     * The file is loaded without stream-mode.
     * @param name
     * @return
     */
    public AudioData loadAudio(String name);

    /**
     * Loads a named model. Models can be jME3 object files (J3O) or
     * OgreXML/OBJ files.
     * @param key
     * @return
     */
    public Spatial loadModel(ModelKey key);

    /**
     * Loads a named model. Models can be jME3 object files (J3O) or
     * OgreXML/OBJ files.
     * @param name
     * @return
     */
    public Spatial loadModel(String name);

    /**
     * Load a material (J3M) file.
     * @param name
     * @return
     */
    public Material loadMaterial(String name);

    /**
     * Loads shader file(s), shouldn't be used by end-user in most cases.
     */
    public Shader loadShader(ShaderKey key);

    /**
     * Load a font file. Font files are in AngelCode text format,
     * and are with the extension "fnt".
     *
     * @param name
     * @return
     */
    public BitmapFont loadFont(String name);
}
