/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.gde.terraineditor;

import com.jme3.terrain.heightmap.AbstractHeightMap;

/**
 * Very simple heightmap class that is a heightmap of floats that is size*size
 * in size, and has height values of zero.
 *
 * @author bowens
 */
public class FlatHeightmap extends AbstractHeightMap {

    private int size;
    private float[] heightmapData;

    public FlatHeightmap(int size) {
        this.size = size;
    }

    @Override
    public boolean load() {
        heightmapData = new float[size*size];
        return true;
    }

    @Override
    public float[] getHeightMap() {
        return heightmapData;
    }

}
