package com.jme3.asset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>ImplHandler</code> manages the asset loader and asset locator
 * implementations in a thread safe way. This allows implementations
 * which store local persistent data to operate with a multi-threaded system.
 * This is done by keeping an instance of each asset loader and asset
 * locator object in a thread local.
 */
public class ImplHandler {

    private static final Logger logger = Logger.getLogger(ImplHandler.class.getName());

    private final AssetManager owner;
    
    private final ArrayList<ImplThreadLocal> genericLocators =
                new ArrayList<ImplThreadLocal>();

    private final HashMap<String, ImplThreadLocal> loaders =
                new HashMap<String, ImplThreadLocal>();

    public ImplHandler(AssetManager owner){
        this.owner = owner;
    }

    protected class ImplThreadLocal extends ThreadLocal {

        private final Class<?> type;
        private final String path;

        public ImplThreadLocal(Class<?> type){
            this.type = type;
            path = null;
        }

        public ImplThreadLocal(Class<?> type, String path){
            this.type = type;
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public Class<?> getTypeClass(){
            return type;
        }

        @Override
        protected Object initialValue(){
            try {
                return type.newInstance();
            } catch (InstantiationException ex) {
                logger.log(Level.SEVERE,"Cannot create locator of type {0}, does"
                            + " the class have an empty and publically accessible"+
                              " constructor?", type.getName());
                logger.throwing(type.getName(), "<init>", ex);
            } catch (IllegalAccessException ex) {
                logger.log(Level.SEVERE,"Cannot create locator of type {0}, "
                            + "does the class have an empty and publically "
                            + "accessible constructor?", type.getName());
                logger.throwing(type.getName(), "<init>", ex);
            }
            return null;
        }
    }

    /**
     * Attempts to locate the given resource name.
     * @param name The full name of the resource.
     * @return The AssetInfo containing resource information required for
     * access, or null if not found.
     */
    public AssetInfo tryLocate(AssetKey key){
        synchronized (genericLocators){
            if (genericLocators.size() == 0)
                return null;

            for (ImplThreadLocal local : genericLocators){
                AssetLocator locator = (AssetLocator) local.get();
                if (local.getPath() != null){
                    locator.setRootPath((String) local.getPath());
                }
                AssetInfo info = locator.locate(owner, key);
                if (info != null)
                    return info;
            }
        }
        return null;
    }

    public int getLocatorCount(){
        synchronized (genericLocators){
            return genericLocators.size();
        }
    }

    /**
     * Returns the AssetLoader registered for the given extension
     * of the current thread.
     * @return AssetLoader registered with addLoader.
     */
    public AssetLoader aquireLoader(AssetKey key){
        synchronized (loaders){
            ImplThreadLocal local = loaders.get(key.getExtension());
            if (local != null){
                AssetLoader loader = (AssetLoader) local.get();
                return loader;
            }
            return null;
        }
    }

    public void addLoader(final Class<?> loaderType, String ... extensions){
        ImplThreadLocal local = new ImplThreadLocal(loaderType);
        for (String extension : extensions){
            extension = extension.toLowerCase();
            synchronized (loaders){
                loaders.put(extension, local);
            }
        }
    }

    public void addLocator(final Class<?> locatorType, String rootPath){
        ImplThreadLocal local = new ImplThreadLocal(locatorType, rootPath);
        synchronized (genericLocators){
            genericLocators.add(local);
        }
    }

    public void removeLocator(final Class<?> locatorType, String rootPath){
        synchronized (genericLocators){
            Iterator<ImplThreadLocal> it = genericLocators.iterator();
            while (it.hasNext()){
                ImplThreadLocal locator = it.next();
                if (locator.getPath().equals(rootPath) &&
                    locator.getTypeClass().equals(locatorType)){
                    it.remove();
                }
            }
        }
    }

}
