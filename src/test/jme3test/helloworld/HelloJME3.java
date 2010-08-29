package jme3test.helloworld;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.math.ColorRGBA;

/** Sample 1 - how to get started with the most simple JME 3 application.
 * Display a blue 3D cube and view from all sides by
 * moving the mouse and pressing the WASD keys. */
public class HelloJME3 extends SimpleApplication {

    public static void main(String[] args){
        HelloJME3 app = new HelloJME3();
        app.start(); // start JME3
    }

    @Override
    public void simpleInitApp() {
        Box b = new Box(Vector3f.ZERO, 1, 1, 1);  // take a cube shape
        Geometry geom = new Geometry("Box", b);   // create a Spatial from it
        Material mat = new Material(assetManager, 
          "Common/MatDefs/Misc/SolidColor.j3md"); // load a solid color material
        mat.setColor("m_Color", ColorRGBA.Blue);  // make the solid color blue
        geom.setMaterial(mat);      // give the box the solid blue material
        rootNode.attachChild(geom); // make the blue box appear in the scene
    }
}