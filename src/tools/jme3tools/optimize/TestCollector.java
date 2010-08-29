package jme3tools.optimize;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import java.util.ArrayList;
import java.util.List;

public class TestCollector {

    public static void main(String[] args){
        Vector3f z = Vector3f.ZERO;
        Geometry g  = new Geometry("quad", new Quad(2,2));
        Geometry g2 = new Geometry("quad", new Quad(2,2));
        List<OCTTriangle> tris = new ArrayList<OCTTriangle>();
        tris.add(new OCTTriangle(z, z, z, 1, 0));
        tris.add(new OCTTriangle(z, z, z, 0, 1));
        List<Geometry> firstOne = TriangleCollector.gatherTris(new Geometry[]{ g, g2 }, tris);
        System.out.println(firstOne.get(0).getMesh());
    }

}
