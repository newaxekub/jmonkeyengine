/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.gde.terraineditor;

import com.jme3.gde.core.sceneexplorer.nodes.JmeSpatial;
import com.jme3.scene.Node;
import org.openide.loaders.DataObject;

/**
 *
 * @author normenhansen
 */
public class TerrainEditorController {
    private JmeSpatial jmeRootNode;
    private DataObject currentFileObject;

    private JmeSpatial selectedSpat;

    private Node rootNode;

    public TerrainEditorController(JmeSpatial jmeRootNode, DataObject currentFileObject) {
        this.jmeRootNode = jmeRootNode;
        this.currentFileObject = currentFileObject;
        rootNode=jmeRootNode.getLookup().lookup(Node.class);
    }

    public void cleanup(){

    }

    public void doCleanup(){
    }

}
