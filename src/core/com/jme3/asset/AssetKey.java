package com.jme3.asset;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import java.io.IOException;

/**
 * <code>AssetKey</code> is a key that is used to
 * look up a resource from a cache. 
 * This class should be immutable.
 */
public class AssetKey implements Savable {

    protected String name;
    protected transient String folder;
    protected transient String extension;

    public AssetKey(String name){
        this.name = name;
        this.extension = getExtension(name);
    }

    public AssetKey(){
    }

    protected static String getExtension(String name){
        int idx = name.lastIndexOf('.');
        //workaround for filenames ending with xml and another dot ending before that (my.mesh.xml)
        if(name.toLowerCase().indexOf(".xml")==name.length()-4){
            idx = name.substring(0, idx).lastIndexOf('.');;
            if(idx==-1){
                idx=name.lastIndexOf('.');
            }
        }
        if (idx <= 0 || idx == name.length() - 1)
            return "";
        else
            return name.substring(idx+1).toLowerCase();
    }

    protected static String getFolder(String name){
        int idx = name.lastIndexOf('/');
        if (idx <= 0 || idx == name.length() - 1)
            return "";
        else
            return name.substring(0, idx+1);
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    public String getFolder(){
        if (folder == null)
            folder = getFolder(name);
        
        return folder;
    }

    /**
     * Do any post-processing on the resource after it has been loaded.
     * @param asset
     */
    public Object postProcess(Object asset){
        return asset;
    }

    /**
     * Create an instance of the asset. Usually it's a special type of cloning.
     * @param asset
     * @return The asset, possibly cloned.
     */
    public Object createClonedInstance(Object asset){
        return asset;
    }

    /**
     * @return True if the asset for this key should be cached. Subclasses
     * should override this method if they want to override caching behavior.
     */
    public boolean shouldCache(){
        return true;
    }

    /**
     * @return Should return true, if the asset objects implement the "Asset"
     * interface and want to be removed from the cache when no longer
     * referenced in user-code.
     */
    public boolean useSmartCache(){
        return false;
    }
    
    @Override
    public boolean equals(Object other){
        if (!(other instanceof AssetKey)){
            return false;
        }
        return name.equals(((AssetKey)other).name);
    }

    @Override
    public int hashCode(){
        return name.hashCode();
    }

    @Override
    public String toString(){
        return name;
    }

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(name, "name", null);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        name = ic.readString("name", null);
        extension = getExtension(name);
    }

}
