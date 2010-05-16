package com.jme3.collision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class CollisionResults implements Iterable<CollisionResult> {

    private final ArrayList<CollisionResult> results = new ArrayList<CollisionResult>();
    private boolean sorted = true;

    public void clear(){
        results.clear();
    }

    public Iterator<CollisionResult> iterator() {
        if (!sorted){
            Collections.sort(results);
            sorted = true;
        }

        return results.iterator();
    }

    public void addCollision(CollisionResult result){
        results.add(result);
        sorted = false;
    }

    public int size(){
        return results.size();
    }

    public CollisionResult getClosestCollision(){
        if (size() == 0)
            return null;

        if (!sorted){
            Collections.sort(results);
            sorted = true;
        }

        return results.get(0);
    }

    public CollisionResult getFarthestCollision(){
        if (size() == 0)
            return null;

        if (!sorted){
            Collections.sort(results);
            sorted = true;
        }

        return results.get(size()-1);
    }

    public CollisionResult getCollision(int index){
        if (!sorted){
            Collections.sort(results);
            sorted = true;
        }

        return results.get(index);
    }

}
