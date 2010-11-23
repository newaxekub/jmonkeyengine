/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.gde.vehiclecreator;

import com.jme3.export.binary.BinaryExporter;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.sceneexplorer.nodes.JmeSpatial;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author normenhansen
 */
public class VehicleEditorController {

    private JmeSpatial jmeRootNode;
    private JmeSpatial selectedSpat;
    private FileObject currentFileObject;

    public VehicleEditorController(JmeSpatial jmeRootNode, FileObject currentFileObject) {
        this.jmeRootNode = jmeRootNode;
        this.currentFileObject = currentFileObject;
    }

    public JmeSpatial getJmeRootNode() {
        return jmeRootNode;
    }

    public JmeSpatial getSelectedSpat() {
        return selectedSpat;
    }

    public void setSelectedSpat(JmeSpatial selectedSpat) {
        this.selectedSpat = selectedSpat;
    }

    public FileObject getCurrentFileObject() {
        return currentFileObject;
    }

    public void awtCall(final String name, final Vector3f point) {
        if (selectedSpat == null) {
            return;
        }
        try {
            final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
            if (node != null) {
                SceneApplication.getApplication().enqueue(new Callable() {

                    public Object call() throws Exception {
                        doAwtCall(node);
                        return null;

                    }
                }).get();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void doAwtCall(Spatial selected) {

    }

    public void saveScene() {
        final Node node = jmeRootNode.getLookup().lookup(Node.class);
        final FileObject file = currentFileObject;
        SceneApplication.getApplication().enqueue(new Callable() {

            public Object call() throws Exception {
                doSaveScene(node, file);
                return null;
            }
        });
    }

    public void doSaveScene(Node node, FileObject currentFileObject) {
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Saving File..");
        progressHandle.start();
        BinaryExporter exp = BinaryExporter.getInstance();
        try {
            exp.save(node, FileUtil.toFile(currentFileObject));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        progressHandle.finish();
        StatusDisplayer.getDefault().setStatusText(currentFileObject.getNameExt() + " saved.");
        //try make NetBeans update the tree.. :/

    }

    private void refreshSelected(final JmeSpatial spat) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (spat != null) {
                    spat.refresh(false);
                }
            }
        });

    }

    private void refreshSelected() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (getSelectedSpat() != null) {
                    getSelectedSpat().refresh(false);
                }
            }
        });

    }

    private void refreshSelectedParent() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (getSelectedSpat() != null) {
                    ((JmeSpatial) getSelectedSpat().getParentNode()).refresh(false);
                }
            }
        });

    }

    private void refreshRoot() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (getJmeRootNode() != null) {
                    getJmeRootNode().refresh(true);
                }
            }
        });

    }

    public void cleanup() {
        final Node node = jmeRootNode.getLookup().lookup(Node.class);
        SceneApplication.getApplication().enqueue(new Callable() {

            public Object call() throws Exception {
                doCleanup(node);
                return null;
            }
        });
    }

    public void doCleanup(Node node) {
        node.removeFromParent();
    }

}
