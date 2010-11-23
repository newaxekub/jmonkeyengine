/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.gde.terraineditor;

import com.jme3.gde.core.sceneexplorer.nodes.AbstractSceneExplorerNode;
import com.jme3.gde.core.sceneexplorer.nodes.SceneExplorerChildren;
import com.jme3.gde.core.sceneexplorer.nodes.SceneExplorerNode;
import com.jme3.material.Material;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.terrain.Terrain;
import java.awt.Image;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;

/**
 * Terrain wrapper class for JMP
 *
 * @author Brent Owens
 */
@org.openide.util.lookup.ServiceProvider(service=SceneExplorerNode.class)
public class JmeTerrain extends AbstractSceneExplorerNode {

    private static Image smallImage =
            ImageUtilities.loadImage("com/jme3/gde/core/sceneexplorer/nodes/icons/geometry.gif");

    private Terrain terrain;

    public JmeTerrain() {
    }

    public JmeTerrain(Terrain terrain, SceneExplorerChildren children) {
        //super(terrain.getSpatial(), children);
        getLookupContents().add(terrain);
        this.terrain = terrain;
        setName("Terrain");
    }


    @Override
    public Class getExplorerObjectClass() {
        return Terrain.class;
    }

    @Override
    public Class getExplorerNodeClass() {
        return JmeTerrain.class;
    }


    @Override
    public Image getIcon(int type) {
        return smallImage;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return smallImage;
    }


    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = Sheet.createPropertiesSet();
        set.setDisplayName("Terrain");
        set.setName(Terrain.class.getName());
        Terrain obj = terrain;
        if (obj == null) {
            return sheet;
        }

        set.put(makeProperty(obj, int.class, "getLodLevel", "setLodLevel", "Lod Level"));
        set.put(makeProperty(obj, Material.class, "getMaterial", "setMaterial", "Material"));
        set.put(makeProperty(obj, Mesh.class, "getMesh", "Mesh"));

        sheet.put(set);
        return sheet;

    }

    public org.openide.nodes.Node[] createNodes(Object key, Object key2, boolean readOnly) {
        SceneExplorerChildren children=new SceneExplorerChildren((com.jme3.scene.Spatial)key);
        children.setReadOnly(readOnly);
        return new org.openide.nodes.Node[]{new JmeTerrain((Terrain) key, children).setReadOnly(readOnly)};
    }


}
