/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.wavefront.importer;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.gde.core.assets.ProjectAssetManager;
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
                    ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Converting Wavefront");
                    progressHandle.start();

                    FileObject file = context.getPrimaryFile();
                    FileLock lock = null;
                    try {
                        lock = file.lock();
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
                        String outputPath = file.getParent().getPath() + File.separator + file.getName() + ".j3o";
                        ((DesktopAssetManager) manager.getManager()).clearCache();
                        Spatial model = manager.getManager().loadModel(manager.getRelativeAssetPath(file.getPath()));
                        BinaryExporter exp = BinaryExporter.getInstance();
                        exp.save(model, new File(outputPath));
                        StatusDisplayer.getDefault().setStatusText("Created file " + file.getName() + ".j3o");
                        //try make NetBeans update the tree.. :/
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

        StatusDisplayer.getDefault().setStatusText("Import with project AssetManager: " + manager);
    }
}
