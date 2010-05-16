package jme3test.bounding;

import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;

/**
 * Tests picking/collision between bounds and shapes.
 */
public class TestRayCollision {

    public static void main(String[] args){
        Ray r = new Ray(Vector3f.ZERO, Vector3f.UNIT_X);
        BoundingBox bbox = new BoundingBox(new Vector3f(5, 0, 0), 1, 1, 1);

        CollisionResults res = new CollisionResults();
        bbox.collideWith(r, res);

        System.out.println("Bounding:" +bbox);
        System.out.println("Ray: "+r);

        System.out.println("Num collisions: "+res.size());
        for (int i = 0; i < res.size(); i++){
            System.out.println("--- Collision #"+i+" ---");
            float dist = res.getCollision(i).getDistance();
            Vector3f pt = res.getCollision(i).getContactPoint();
            System.out.println("distance: "+dist);
            System.out.println("point: "+pt);
        }
    }

}
