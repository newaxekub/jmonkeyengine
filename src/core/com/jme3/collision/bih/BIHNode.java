package com.jme3.collision.bih;

import com.jme3.bounding.BoundingBox;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Matrix4f;
import com.jme3.math.Ray;
import com.jme3.math.Triangle;
import com.jme3.math.Vector3f;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Math.min;
import static java.lang.Math.max;

/**
 * Bounding Interval Hierarchy.
 * Based on:
 *
 * Instant Ray Tracing: The Bounding Interval Hierarchy
 * By Carsten Wächter and Alexander Keller
 */
public class BIHNode implements Savable {

    int leftIndex, rightIndex;

    BIHNode left;
    BIHNode right;
    float leftPlane;
    float rightPlane;
    int axis;

    public BIHNode(int l, int r){
        leftIndex = l;
        rightIndex = r;
        axis = 3; // indicates leaf
    }

    public BIHNode(int axis){
        this.axis = axis;
    }

    public BIHNode(){
    }

    /*
     * int leftIndex, rightIndex;

    BIHNode left;
    BIHNode right;
    float leftPlane;
    float rightPlane;
    int axis;*/

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(leftIndex,  "left_index", 0);
        oc.write(rightIndex, "right_index", 0);
        oc.write(leftPlane, "left_plane", 0);
        oc.write(rightPlane, "right_plane", 0);
        oc.write(axis, "axis", 0);
        oc.write(left, "left_node", null);
        oc.write(right, "right_node", null);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        leftIndex = ic.readInt("left_index", 0);
        rightIndex = ic.readInt("right_index", 0);
        leftPlane = ic.readFloat("left_plane", 0);
        rightPlane = ic.readFloat("right_plane", 0);
        axis = ic.readInt("axis", 0);
        left = (BIHNode) ic.readSavable("left_node", null);
        right = (BIHNode) ic.readSavable("right_node", null);
    }

    private static class BIHStackData {

        private final BIHNode node;
        private final float min, max;

        public BIHStackData(BIHNode node, float min, float max) {
            this.node = node;
            this.min = min;
            this.max = max;
        }

    }

    private static final ArrayList<BIHStackData> stack = new ArrayList<BIHStackData>();

    public final int intersectWhere(Collidable col,
                                    BoundingBox box,
                                    Matrix4f worldMatrix,
                                    BIHTree tree,
                                    CollisionResults results){
        stack.clear();

        float[] minExts  = { box.getCenter().x - box.getXExtent(),
                             box.getCenter().y - box.getYExtent(),
                             box.getCenter().z - box.getZExtent() };

        float[] maxExts  = { box.getCenter().x + box.getXExtent(),
                             box.getCenter().y + box.getYExtent(),
                             box.getCenter().z + box.getZExtent() };

        stack.add(new BIHStackData(this, 0,0));

        Triangle t = new Triangle();
        int cols = 0;

        stackloop: while (stack.size() > 0){
            BIHNode node = stack.remove(stack.size()-1).node;

            while (node.axis != 3){
                int a = node.axis;

                float maxExt = maxExts[a];
                float minExt = minExts[a];

                if (node.leftPlane <= node.rightPlane){
                    // means there's a gap in the middle
                    // if the box is in that gap, we stop there
                    if (minExt > node.leftPlane
                    &&  maxExt < node.rightPlane)
                        continue stackloop;
                }

                if (maxExt < node.rightPlane){
                    node = node.left;
                }else if (minExt > node.leftPlane){
                    node = node.right;
                }else{
                    stack.add(new BIHStackData(node.right, 0, 0));
                    node = node.left;
                }
//                if (maxExt < node.leftPlane
//                 && maxExt < node.rightPlane){
//                    node = node.left;
//                }else if (minExt > node.leftPlane
//                       && minExt > node.rightPlane){
//                    node = node.right;
//                }else{
                    
//                }
            }

            for (int i = node.leftIndex; i <= node.rightIndex; i++){
                tree.getTriangle(i, t.get1(), t.get2(), t.get3());
                if (worldMatrix != null){
                    worldMatrix.mult(t.get1(), t.get1());
                    worldMatrix.mult(t.get2(), t.get2());
                    worldMatrix.mult(t.get3(), t.get3());
                }

                int added = col.collideWith(t, results);

                if (added > 0){
                    int index = tree.getTriangleIndex(i);
                    int start = results.size() - added;

                    for (int j = start; j < results.size(); j++){
                        CollisionResult cr = results.getCollision(j);
                        cr.setTriangleIndex(index);
                    }

                    cols += added;
                }
            }
        }

        return cols;
    }

    public final int intersectWhere(Ray r,
                                    Matrix4f worldMatrix,
                                    BIHTree tree,
                                    float sceneMin,
                                    float sceneMax,
                                    CollisionResults results){
        stack.clear();

        float tHit = Float.POSITIVE_INFINITY;
        float[] origins = { r.getOrigin().x,
                            r.getOrigin().y,
                            r.getOrigin().z };
        float[] invDirections = { 1f / r.getDirection().x,
                                  1f / r.getDirection().y,
                                  1f / r.getDirection().z };

        Vector3f v1 = new Vector3f(),
                 v2 = new Vector3f(),
                 v3 = new Vector3f();
        int cols = 0;

        stack.add(new BIHStackData(this, sceneMin, sceneMax));
        stackloop: while (stack.size() > 0){

            BIHStackData data = stack.remove(stack.size()-1);
            BIHNode node = data.node;
            float tMin = data.min, 
                  tMax = data.max;

            if (tMax < tMin)
                continue;
            
            leafloop: while (node.axis != 3){ // while node is not a leaf
                int a = node.axis;
                
                // find the origin and direction value for the given axis
                float origin = origins[a];
                float invDirection = invDirections[a];

                float tNearSplit, tFarSplit;
                BIHNode nearNode, farNode;

                tNearSplit = (node.leftPlane  - origin) * invDirection;
                tFarSplit  = (node.rightPlane - origin) * invDirection;
                nearNode = node.left;
                farNode  = node.right;

                if (invDirection < 0){
                    float tmpSplit = tNearSplit;
                    tNearSplit = tFarSplit;
                    tFarSplit = tmpSplit;

                    BIHNode tmpNode = nearNode;
                    nearNode = farNode;
                    farNode = tmpNode;
                }

                if (tMin > tNearSplit && tMax < tFarSplit){
                    continue stackloop;
                }

                if (tMin > tNearSplit){
                    tMin = max(tMin, tFarSplit);
                    node = farNode;
                }else if (tMax < tFarSplit){
                    tMax = min(tMax, tNearSplit);
                    node = nearNode;
                }else{
                    stack.add(new BIHStackData(farNode,  max(tMin, tFarSplit), tMax));
                    tMax = min(tMax, tNearSplit);
                    node = nearNode;
                }
            }

//            if ( (node.rightIndex - node.leftIndex) > minTrisPerNode){
//                // on demand subdivision
//                node.subdivide();
//                stack.add(new BIHStackData(node, tMin, tMax));
//                continue stackloop;
//            }

            // a leaf
            for (int i = node.leftIndex; i <= node.rightIndex; i++){
                tree.getTriangle(i, v1,v2,v3);

                if (worldMatrix != null){
                    worldMatrix.mult(v1);
                    worldMatrix.mult(v2);
                    worldMatrix.mult(v3);
                }

                float t = r.intersects(v1,v2,v3);
                if (t < tHit){
                    tHit = t;
                    tMax = min(tMax, tHit);
                    Vector3f contactPoint = new Vector3f(r.direction)
                                                .multLocal(tHit)
                                                .addLocal(r.origin);
                    CollisionResult cr = new CollisionResult(contactPoint, tHit);
                    cr.setTriangleIndex(tree.getTriangleIndex(i));
                    results.addCollision(cr);
                    cols ++;
                }
            }
//            if (results.size() > 0)
//                return;
        }

        return cols;
    }

}
