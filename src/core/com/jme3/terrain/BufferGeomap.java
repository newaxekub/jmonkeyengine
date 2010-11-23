/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jme3.terrain;

import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Implementation of the Geomap interface which stores data in memory as native buffers
 */
public class BufferGeomap extends AbstractGeomap implements Geomap {

    protected FloatBuffer hdata;
    protected ByteBuffer ndata;
    protected int width, height, maxval;

    public BufferGeomap(FloatBuffer heightData, ByteBuffer normalData, int width, int height, int maxval){
        this.hdata = heightData;
        this.ndata = normalData;
        this.width = width;
        this.height = height;
        this.maxval = maxval;
    }

    public BufferGeomap(int width, int height, int maxval) {
        this(ByteBuffer.allocateDirect(width*height*4).asFloatBuffer(),null,width,height,maxval);
    }

    public FloatBuffer getHeightData(){
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
        return (int) hdata.get(y*width+x);
    }

    public int getValue(int i) {
        return (int) hdata.get(i);
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
        FloatBuffer nhdata = ByteBuffer.allocateDirect(w*h*4).asFloatBuffer();
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
