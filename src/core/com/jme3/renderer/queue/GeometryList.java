package com.jme3.renderer.queue;

import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import java.util.Arrays;

/**
 * This class is a special function list of Spatial objects for render
 * queueing.
 *
 * @author Jack Lindamood
 * @author Three Rings - better sorting alg.
 */
public class GeometryList {

    private static final int DEFAULT_SIZE = 32;

    private Geometry[] geometries;
    private int size;
    private GeometryComparator comparator;

    public GeometryList(GeometryComparator comparator) {
        size = 0;
        geometries = new Geometry[DEFAULT_SIZE];
        this.comparator = comparator;
    }

    public void setCamera(Camera cam){
        this.comparator.setCamera(cam);
    }

    public int size(){
        return size;
    }

    public Geometry get(int index){
        return geometries[index];
    }

    /**
     * Adds a spatial to the list. List size is doubled if there is no room.
     *
     * @param s
     *            The spatial to add.
     */
    public void add(Geometry g) {
        if (size == geometries.length) {
            Geometry[] temp = new Geometry[size * 2];
            System.arraycopy(geometries, 0, temp, 0, size);
            geometries = temp; // original list replaced by double-size list
        }
        geometries[size++] = g;
    }

    /**
     * Resets list size to 0.
     */
    public void clear() {
        for (int i = 0; i < size; i++){
            geometries[i] = null;
        }

        size = 0;
    }

    /**
     * Sorts the elements in the list acording to their Comparator.
     */
    public void sort() {
        if (size > 1) {
            // sort the spatial list using the comparator
            Arrays.sort(geometries, comparator);
        }
    }
}