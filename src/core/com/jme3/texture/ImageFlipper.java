package com.jme3.texture;

import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;

public class ImageFlipper {

    public static void flipImage(Image img, int index){
        if (img.getFormat().isCompressed())
            throw new UnsupportedOperationException("Flipping compressed " +
                                                    "images is unsupported.");

        int w = img.getWidth();
        int h = img.getHeight();
        int halfH = h / 2;

        // bytes per pixel
        int bpp = img.getFormat().getBitsPerPixel() / 8;
        int scanline = w * bpp;

        ByteBuffer data = img.getData(index);
        ByteBuffer temp = BufferUtils.createByteBuffer(scanline);
        
        data.rewind();
        for (int y = 0; y < halfH; y++){
            int oppY = h - y - 1;
            // read in scanline
            data.position(y * scanline);
            data.limit(data.position() + scanline);

            temp.rewind();
            temp.put(data);

        }
    }

}
