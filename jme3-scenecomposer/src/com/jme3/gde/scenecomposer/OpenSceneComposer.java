/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.scenecomposer;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.assets.BinaryModelDataObject;
import com.jme3.scene.Spatial;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

public final class OpenSceneComposer implements ActionListener {

    private final BinaryModelDataObject context;

    public OpenSceneComposer(BinaryModelDataObject context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        Runnable call = new Runnable() {

            public void run() {
                ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Opening in SceneComposer");
                progressHandle.start();
                final ProjectAssetManager manager = context.getLookup().lookup(ProjectAssetManager.class);
                if (manager == null) {
                    return;
                }
                final FileObject file = context.getPrimaryFile();
                String assetName = manager.getRelativeAssetPath(file.getPath());
                FileLock lock = null;
                final Spatial spat;
                try {
                    ((DesktopAssetManager) manager.getManager()).clearCache();
                    lock = file.lock();
                    spat = manager.getManager().loadModel(assetName);
                    java.awt.EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            SceneComposerTopComponent composer = SceneComposerTopComponent.findInstance();
                            composer.openScene(spat, file, manager);
                        }
                    });
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    Confirmation msg = new NotifyDescriptor.Confirmation(
                            "Error opening " + file.getNameExt() + "\n" + ex.toString(),
                            NotifyDescriptor.OK_CANCEL_OPTION,
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(msg);
                } finally {
                    if (lock != null) {
                        lock.releaseLock();
                    }
                    progressHandle.finish();
                }
            }
        };
        new Thread(call).start();
    }
}
