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

package com.jme3.post;

import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import java.util.HashMap;

class BufferCache {

    private static class BufferKey {
        int width;
        int height;
        Format format;
        boolean ms;

        public BufferKey(int width, int height, Format format, boolean ms) {
            this.width = width;
            this.height = height;
            this.format = format;
            this.ms = ms;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BufferKey other = (BufferKey) obj;
            if (this.width != other.width) {
                return false;
            }
            if (this.height != other.height) {
                return false;
            }
            if (this.format != other.format && (this.format == null || !this.format.equals(other.format))) {
                return false;
            }
            if (this.ms != other.ms) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 73 * hash + this.width;
            hash = 73 * hash + this.height;
            hash = 73 * hash + (this.format != null ? this.format.hashCode() : 0);
            hash = 73 * hash + (this.ms ? 1 : 0);
            return hash;
        }

    }

    private final HashMap<BufferKey, FrameBuffer> bufferCache = new HashMap<BufferKey, FrameBuffer>();
    private int viewWidth, viewHeight, viewSamples;
    private Format format;

    public FrameBuffer getFrameBuffer(int width, int height, Format format, boolean multiSample){
        BufferKey key = new BufferKey(width, height, format, multiSample);
        FrameBuffer fb = bufferCache.remove(key);
        if (fb != null)
            return fb;

        fb = new FrameBuffer(width, height, multiSample ? viewSamples : 0);
        fb.setColorTexture(new Texture2D(width, height, format));
        return fb;
    }

    public FrameBuffer getFrameBuffer(int width, int height){
        return getFrameBuffer(width, height, format, true);
    }

    public FrameBuffer getFrameBuffer(Format format, boolean ms){
        return getFrameBuffer(viewWidth, viewHeight, format, ms);
    }

    public FrameBuffer getFrameBuffer(boolean ms){
        return getFrameBuffer(format, ms);
    }

    public FrameBuffer getFrameBuffer(){
        return getFrameBuffer(true);
    }

    public void putFrameBuffer(FrameBuffer fb){
        BufferKey key = new BufferKey(fb.getWidth(),
                                      fb.getHeight(),
                                      fb.getColorBuffer().getFormat(),
                                      fb.getSamples() > 0);
        bufferCache.put(key, fb);
    }

}
