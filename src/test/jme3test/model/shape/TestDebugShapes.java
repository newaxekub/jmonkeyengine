package jme3test.model.shape;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.debug.WireSphere;

public class TestDebugShapes extends SimpleApplication {

    public static void main(String[] args){
        TestDebugShapes app = new TestDebugShapes();
        app.start();
    }

    public Geometry putShape(Mesh shape, ColorRGBA color){
        Geometry g = new Geometry("shape", shape);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", color);
        g.setMaterial(mat);
        rootNode.attachChild(g);
        return g;
    }

    public void putArrow(Vector3f pos, Vector3f dir, ColorRGBA color){
        putShape(new Arrow(dir), color).setLocalTranslation(pos);
    }

    public void putBox(Vector3f pos, float size, ColorRGBA color){
        putShape(new WireBox(size, size, size), color).setLocalTranslation(pos);
    }

    public void putGrid(Vector3f pos, ColorRGBA color){
        putShape(new Grid(6, 6, 0.2f), color).center().move(pos);
    }

    public void putSphere(Vector3f pos, ColorRGBA color){
        putShape(new WireSphere(1), color).setLocalTranslation(pos);
    }

    @Override
    public void simpleInitApp() {
        cam.setLocation(new Vector3f(2,1.5f,2));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        putArrow(Vector3f.ZERO, Vector3f.UNIT_X, ColorRGBA.Red);
        putArrow(Vector3f.ZERO, Vector3f.UNIT_Y, ColorRGBA.Green);
        putArrow(Vector3f.ZERO, Vector3f.UNIT_Z, ColorRGBA.Blue);

        putBox(new Vector3f(2, 0, 0), 0.5f, ColorRGBA.Yellow);
        putGrid(new Vector3f(3.5f, 0, 0), ColorRGBA.White);
        putSphere(new Vector3f(4.5f, 0, 0), ColorRGBA.Magenta);
    }

}
