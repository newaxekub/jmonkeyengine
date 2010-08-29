package com.jme3.asset.plugins;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * <code>FileLocator</code> allows you to specify a folder where to
 * look for assets. 
 * @author Kirill Vainer
 */
public class FileLocator implements AssetLocator {

    private File root;

    public void setRootPath(String rootPath) {
        if (rootPath == null)
            throw new NullPointerException();

        root = new File(rootPath);
        if (!root.isDirectory())
            throw new RuntimeException("Given root path not a directory");
    }

    private static class AssetInfoFile extends AssetInfo {

        private File file;

        public AssetInfoFile(AssetManager manager, AssetKey key, File file){
            super(manager, key);
            this.file = file;
        }

        @Override
        public InputStream openStream() {
            try{
                return new FileInputStream(file);
            }catch (FileNotFoundException ex){
                return null;
            }
        }
    }

    public AssetInfo locate(AssetManager manager, AssetKey key) {
        String name = key.getName();
        File file = new File(root, name);
        if (file.exists() && file.isFile()){
            return new AssetInfoFile(manager, key, file);
        }else{
            return null;
        }
    }

}
