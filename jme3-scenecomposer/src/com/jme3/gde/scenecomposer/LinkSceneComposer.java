/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.scenecomposer;

import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.assets.SpatialAssetDataObject;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.openide.filesystems.FileObject;

public final class LinkSceneComposer implements ActionListener {

    private final SpatialAssetDataObject context;

    public LinkSceneComposer(SpatialAssetDataObject context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        ProjectAssetManager manager=context.getLookup().lookup(ProjectAssetManager.class);
        if(manager == null) return;
        FileObject file=context.getPrimaryFile();
        String assetName=manager.getRelativeAssetPath(file.getPath());
        SceneComposerTopComponent composer=SceneComposerTopComponent.findInstance();
        composer.linkModel(manager.getManager(),assetName);
    }
}
