/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.ogretools;

import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.assets.SpatialAssetDataObject;
import com.jme3.gde.ogretools.convert.OgreXMLConvert;
import com.jme3.gde.ogretools.convert.OgreXMLConvertOptions;
import com.jme3.scene.Spatial;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Exceptions;

public class OgreBinaryMeshDataObject extends SpatialAssetDataObject {

    public OgreBinaryMeshDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
    }

    @Override
    public Spatial loadAsset() {
        OgreXMLConvertOptions options=new OgreXMLConvertOptions(getPrimaryFile().getPath());
        options.setBinaryFile(true);
        OgreXMLConvert conv=new OgreXMLConvert();
        ProgressHandle handle=ProgressHandleFactory.createHandle("Converting OgreBinary");
        handle.start(4);
        conv.doConvert(options, handle);
        handle.progress(3);
        ProjectAssetManager mgr = getLookup().lookup(ProjectAssetManager.class);
        if (mgr == null) {
            return null;
        }
        String assetKey = mgr.getRelativeAssetPath(options.getDestFile());
        FileLock lock = null;
        try {
            lock = getPrimaryFile().lock();
            Spatial spatial = mgr.getManager().loadModel(assetKey);
            lock.releaseLock();
            File deleteFile=new File(options.getDestFile());
            deleteFile.delete();
            handle.finish();
            return spatial;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            if (lock != null) {
                lock.releaseLock();
            }
        }
        File deleteFile=new File(options.getDestFile());
        deleteFile.delete();
        handle.finish();
        return null;
    }

}
