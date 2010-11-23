/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.terraineditor;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.scene.controller.AbstractCameraController;
import com.jme3.gde.core.sceneexplorer.nodes.JmeNode;
import com.jme3.gde.terraineditor.TerrainEditorTopComponent.TerrainEditButton;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import com.jme3.terrain.Terrain;
import java.util.concurrent.Callable;

/**
 *
 * @author normenhansen
 */
public class TerrainCameraController extends AbstractCameraController {

    private JmeNode rootNode;
    private Node toolsNode;
    private Node terrainNode;
    private boolean active;
    private TerrainEditButton currentEditButtonState;
    private Geometry marker;

    public TerrainCameraController(Camera cam, InputManager inputManager, JmeNode rootNode) {
        super(cam, inputManager);
        this.rootNode = rootNode;
    }

    public void setToolsNode(Node toolsNode) {
        this.toolsNode = toolsNode;
    }

    
    @Override
    protected void checkClick(int button) {
    }

    public void setActive(boolean bln) {
        this.active = bln;
    }

    public boolean isActive() {
        return active;
    }

    public void postRender() {
        
    }

    public void setTerrainEditButtonState(final TerrainEditButton state) {
        if (getTerrain(null) == null)
            return; // no terrain in map yet

        currentEditButtonState = state;
        if (state == TerrainEditButton.none) {
            SceneApplication.getApplication().enqueue(new Callable<Object>() {
                public Object call() throws Exception {
                    hideEditTool();
                    return null;
                }
            });
        } else if (state == TerrainEditButton.raiseTerrain) {
            SceneApplication.getApplication().enqueue(new Callable<Object>() {
                public Object call() throws Exception {
                    showRaiseLowerEditTool(state);
                    return null;
                }
            });
        }
    }
    
    private Node getTerrain(Spatial root) {
        if (terrainNode != null)
            return terrainNode;
        
        if (root == null)
            root = rootNode.getLookup().lookup(Node.class);

        // is this the terrain?
        if (root instanceof Terrain && root instanceof Node) {
            terrainNode = (Node)root;
            return terrainNode;
        }

        if (root instanceof Node) {
            Node n = (Node) root;
            for (Spatial c : n.getChildren()) {
                if (c instanceof Node){
                    Node res = getTerrain(c);
                    if (res != null)
                        return res;
                }
            }
        }
        
        return null;
    }

    private Vector3f getTerrainCollisionPoint() {
        CollisionResults results = new CollisionResults();
        Ray ray = new Ray();
        Vector3f pos = cam.getWorldCoordinates(new Vector2f(mouseX, mouseY), 0).clone();
        Vector3f dir = cam.getWorldCoordinates(new Vector2f(mouseX, mouseY), 0.3f).clone();
        dir.subtractLocal(pos).normalizeLocal();
        ray.setOrigin(pos);
        ray.setDirection(dir);
        getTerrain(null).collideWith(ray, results);
        if (results == null) {
            return null;
        }
        final CollisionResult result = results.getClosestCollision();
        return result.getContactPoint();
    }

    private void hideEditTool() {
        if (marker != null) {
            
        }
    }

    private void showRaiseLowerEditTool(TerrainEditButton terrainEditButton) {
        if (marker == null) {
            marker = new Geometry("edit marker");
            Mesh m = new Cylinder(1, 8, 2, 2, true);
            marker.setMesh(m);
            Material mat = new Material(SceneApplication.getApplication().getAssetManager(), "Common/MatDefs/Misc/SolidColor.j3md");
            mat.setColor("m_Color", ColorRGBA.Green);
            marker.setMaterial(mat);
        }


    }
}
