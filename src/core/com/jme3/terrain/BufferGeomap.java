package com.jme3.terrain;

import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Implementation of the Geomap interface which stores data in memory as native buffers
 */
public class BufferGeomap extends AbstractGeomap implements Geomap {

    protected final IntBuffer hdata;
    protected final ByteBuffer ndata;
    protected final int width, height, maxval;

    public BufferGeomap(IntBuffer heightData, ByteBuffer normalData, int width, int height, int maxval){
        this.hdata = heightData;
        this.ndata = normalData;
        this.width = width;
        this.height = height;
        this.maxval = maxval;
    }

    public BufferGeomap(int width, int height, int maxval) {
        this(ByteBuffer.allocateDirect(width*height*4).asIntBuffer(),null,width,height,maxval);
    }

    public IntBuffer getHeightData(){
        if (!isLoaded())
            return null;

        return hdata;
    }

    public ByteBuffer getNormalData(){
        if (!isLoaded() || !hasNormalmap())
            return null;

        return ndata;
    }

    public int getMaximumValue(){
        return maxval;
    }

    public int getValue(int x, int y) {
        return hdata.get(y*width+x);
    }

    public int getValue(int i) {
        return hdata.get(i);
    }

    public Vector3f getNormal(int x, int y, Vector3f store) {
        return getNormal(y*width+x,store);
    }

    public Vector3f getNormal(int i, Vector3f store) {
        ndata.position( i*3 );
        if (store==null) store = new Vector3f();
        store.setX( (((float)(ndata.get() & 0xFF)/255f)-0.5f)*2f );
        store.setY( (((float)(ndata.get() & 0xFF)/255f)-0.5f)*2f );
        store.setZ( (((float)(ndata.get() & 0xFF)/255f)-0.5f)*2f );
        return store;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public SharedGeomap getSubGeomap(int x, int y, int w, int h) {
        if (w+x > width)
            w = width - x;
        if (h+y > height)
            h = height - y;

        return new SharedBufferGeomap(this,x,y,w,h);
    }

    public Geomap copySubGeomap(int x, int y, int w, int h){
        IntBuffer nhdata = ByteBuffer.allocateDirect(w*h*4).asIntBuffer();
        hdata.position(y*width+x);
        for (int cy = 0; cy < height; cy++){
            hdata.limit(hdata.position()+w);
            nhdata.put(hdata);
            hdata.limit(hdata.capacity());
            hdata.position(hdata.position()+width);
        }
        nhdata.flip();

        ByteBuffer nndata = null;
        if (ndata!=null){
            nndata = ByteBuffer.allocateDirect(w*h*3);
            ndata.position( (y*width+x)*3 );
            for (int cy = 0; cy < height; cy++){
                ndata.limit(ndata.position()+w*3);
                nndata.put(ndata);
                ndata.limit(ndata.capacity());
                ndata.position(ndata.position()+width*3);
            }
            nndata.flip();
        }

        return new BufferGeomap(nhdata,nndata,w,h,maxval);
    }

    public boolean hasNormalmap() {
        return ndata != null;
    }

    public boolean isLoaded() {
        return true;
    }

//    @Override
//    public FloatBuffer writeNormalArray(FloatBuffer store) {
//        if (!isLoaded() || !hasNormalmap()) throw new NullPointerException();
//
//        if (store!=null){
//            if (store.remaining() < width*height*3)
//                throw new BufferUnderflowException();
//        }else{
//            store = BufferUtils.createFloatBuffer(width*height*3);
//        }
//        ndata.rewind();
//
//        for (int z = 0; z < height; z++){
//            for (int x = 0; x < width; x++){
//                float r = ((float)(ndata.get() & 0xFF)/255f -0.5f) * 2f;
//                float g = ((float)(ndata.get() & 0xFF)/255f -0.5f) * 2f;
//                float b = ((float)(ndata.get() & 0xFF)/255f -0.5f) * 2f;
//                store.put(r).put(b).put(g);
//            }
//        }
//
//        return store;
//    }

    @Override
    public FloatBuffer writeVertexArray(FloatBuffer store, Vector3f scale, boolean center) {
        if (!isLoaded()) throw new NullPointerException();

        if (store!=null){
            if (store.remaining() < width*height*3)
                throw new BufferUnderflowException();
        }else{
            store = BufferUtils.createFloatBuffer(width*height*3);
        }
        hdata.rewind();

        assert hdata.limit() == height*width;

        Vector3f offset = new Vector3f(-getWidth() * scale.x * 0.5f,
                                       0,
                                       -getWidth() * scale.z * 0.5f);
        if (!center)
            offset.zero();

        for (int z = 0; z < height; z++){
            for (int x = 0; x < width; x++){
                store.put( (float)x*scale.x + offset.x );
                store.put( (float)hdata.get()*scale.y );
                store.put( (float)z*scale.z + offset.z );
            }
        }

        return store;
    }

}
