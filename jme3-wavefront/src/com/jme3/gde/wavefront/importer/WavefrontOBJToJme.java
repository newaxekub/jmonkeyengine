/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.wavefront.importer;

import com.jme3.export.binary.BinaryExporter;
import com.jme3.gde.core.assets.AssetData;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.assets.SpatialAssetDataObject;
import com.jme3.scene.Spatial;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

public final class WavefrontOBJToJme implements ActionListener {

    private final DataObject context;

    public WavefrontOBJToJme(DataObject context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        final ProjectAssetManager manager = context.getLookup().lookup(ProjectAssetManager.class);
        if (manager == null) {
            StatusDisplayer.getDefault().setStatusText("Project has no AssetManager!");
            return;
        }

        if (context != null) {
            Runnable run = new Runnable() {

                public void run() {
                    ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Converting Model");
                    progressHandle.start();
                    FileObject file = context.getPrimaryFile();
                    FileLock lock = null;
                    try {
                        String materialPath = file.getPath().replaceAll(".obj", ".mtl").replaceAll(".OBJ", ".MTL");
                        if(!new File(materialPath).exists()){
                            Confirmation msg = new NotifyDescriptor.Confirmation(
                                    "No material file found for " + file.getNameExt() + "\n"
                                    + "A file named "+file.getNameExt().replaceAll(".obj", ".mtl").replaceAll(".OBJ", ".MTL")
                                    +" should be in the same folder.\n"
                                    + "Press OK to import mesh only.",
                                    NotifyDescriptor.OK_CANCEL_OPTION,
                                    NotifyDescriptor.WARNING_MESSAGE);
                            Object result = DialogDisplayer.getDefault().notify(msg);
                            if (!NotifyDescriptor.OK_OPTION.equals(result)) {
                                return;
                            }
                        }
                        //load model
                        Spatial model = (Spatial) ((SpatialAssetDataObject) context).loadAsset();
                        //export model
                        String outputPath = file.getParent().getPath() + "/" + file.getName() + ".j3o";
                        BinaryExporter exp = BinaryExporter.getInstance();
                        File outFile=new File(outputPath);
                        exp.save(model, outFile);

                        //store original asset path interface properties
                        DataObject targetModel=DataObject.find(FileUtil.toFileObject(outFile));
                        AssetData properties=targetModel.getLookup().lookup(AssetData.class);
                        properties.loadProperties();
                        properties.setProperty("ORIGINAL_PATH", manager.getRelativeAssetPath(file.getPath()));
                        properties.saveProperties();
                        
                        StatusDisplayer.getDefault().setStatusText("Created file " + file.getName() + ".j3o");
                        //update the tree
                        context.getPrimaryFile().getParent().refresh();
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                        Confirmation msg = new NotifyDescriptor.Confirmation(
                                "Error converting " + file.getNameExt() + "\n" + ex.toString(),
                                NotifyDescriptor.OK_CANCEL_OPTION,
                                NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(msg);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        progressHandle.finish();
                    }
                }
            };
            new Thread(run).start();
        }
    }
}
