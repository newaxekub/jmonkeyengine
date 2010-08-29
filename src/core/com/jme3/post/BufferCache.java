package com.jme3.post;

import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import java.util.HashMap;

public class BufferCache {

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
