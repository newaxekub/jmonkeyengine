package com.jme3.asset.plugins;

import com.jme3.asset.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>UrlLocator</code> is a locator that combines a root url
 * and the given path in the AssetKey to construct a new url
 * that allows locating the asset.
 * @author Kiirill Vainer
 */
public class UrlLocator implements AssetLocator {

    private static final Logger logger = Logger.getLogger(UrlLocator.class.getName());
    private URL root;

    private static class UrlAssetInfo extends AssetInfo {

        private URLConnection conn;

        public UrlAssetInfo(AssetManager manager, AssetKey key, URLConnection conn){
            super(manager, key);
            this.conn = conn;
        }

        @Override
        public InputStream openStream() {
            try{
                return conn.getInputStream();
            }catch (IOException ex){
                return null; // failure..
            }
        }
    }

    public void setRootPath(String rootPath) {
        try {
            this.root = new URL(rootPath);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Invalid rootUrl specified", ex);
        }
    }

    public AssetInfo locate(AssetManager manager, AssetKey key) {
        String name = key.getName();
        
        try{
            URL url = new URL(root, name);
            URLConnection conn = url.openConnection();
            conn.setUseCaches(false);
            return new UrlAssetInfo(manager, key, conn);
        }catch (IOException ex){
            logger.log(Level.WARNING, "Error while locating " + name, ex);
            return null;
        }
    }


}
