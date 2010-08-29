package com.jme3.terrain;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class SharedBufferGeomap extends AbstractGeomap implements SharedGeomap {

    protected final BufferGeomap parent;
    protected final IntBuffer hdata;
    protected final ByteBuffer ndata;
    protected final int startX, startY, width, height;

    public SharedBufferGeomap(BufferGeomap parent, int x, int y, int w, int h){
        this.parent = parent;
        hdata = parent.getHeightData();
        ndata = parent.getNormalData();
        startX = x;
        startY = y;
        width = w;
        height = h;
    }

    public boolean hasNormalmap() {
        return parent.hasNormalmap();
    }

    public boolean isLoaded() {
        return parent.isLoaded();
    }

    public int getMaximumValue(){
        return parent.getMaximumValue();
    }

    public Geomap getParent() {
        return parent;
    }

    public int getXOffset() {
        return startX;
    }

    public int getYOffset() {
        return startY;
    }

    public int getValue(int x, int y) {
        return parent.getValue(startX+x,startY+y);
    }

    public int getValue(int i) {
        int r = i % width;
        return getValue(r,(i-r)/width);
    }

    public Vector3f getNormal(int x, int y, Vector3f store) {
        return parent.getNormal(startX+x,startY+y,store);
    }

    public Vector3f getNormal(int i, Vector3f store) {
        int r = i % width;
        return getNormal(r,(i-r)/width,store);
    }

    @Override
    public Vector2f getUV(int x, int y, Vector2f store){
        return parent.getUV(startX+x, startY+y, store);
    }

    @Override
    public Vector2f getUV(int i, Vector2f store){
        int r = i % width;
        return getUV(r,(i-r)/width,store);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Geomap copy() {
        return parent.copySubGeomap(startX,startY,width,height);
    }

    public SharedGeomap getSubGeomap(int x, int y, int w, int h) {
        if (x<0 || y<0 || x>width || y>height || w+x>width || h+y>height)
            throw new IndexOutOfBoundsException();

        return parent.getSubGeomap(startX+x,startY+y,w,h);
    }

    public Geomap copySubGeomap(int x, int y, int w, int h) {
        if (x<0 || y<0 || x>width || y>height || w>width || h>height)
            throw new IndexOutOfBoundsException();

        return parent.copySubGeomap(startX+x,startY+y,w,h);
    }

}
