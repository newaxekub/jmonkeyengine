/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.core.filetypes.actions;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.scene.SceneRequest;
import com.jme3.gde.core.scene.nodes.JmeNode;
import com.jme3.gde.core.scene.nodes.NodeUtility;
import com.jme3.scene.Node;
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
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

public final class OpenModel implements ActionListener {

    private final DataObject context;

    public OpenModel(DataObject context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        Runnable call = new Runnable() {

            public void run() {
                ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Opening Model");
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
                    file.lock();
                    spat = manager.getManager().loadModel(assetName);
                    if (spat instanceof Node) {
                        //TODO: change scenecomposer to not depend on awt thread (move stuff from TopComponent)
                        JmeNode jmeNode = NodeUtility.createNode((Node) spat);
                        SceneApplication app=SceneApplication.getApplication();
                        SceneRequest request = new SceneRequest(app, jmeNode, manager);
                        request.setWindowTitle("SceneViewer - View Model");
                        app.requestScene(request);
                    } else {

                    }

                } catch (IOException ex) {
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
