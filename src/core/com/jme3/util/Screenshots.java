package com.jme3.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;

public final class Screenshots {
    public static void convertScreenShot(ByteBuffer bgraBuf, BufferedImage out){
        byte[] cpuArray = new byte[out.getWidth() * out.getHeight() * 4];

        // copy native memory to java memory
        bgraBuf.clear();
        bgraBuf.get(cpuArray);
        bgraBuf.clear();

        // flip the components the way AWT likes them
        for (int i = 0; i < cpuArray.length; i+=4){
            byte b = cpuArray[i+0];
            byte g = cpuArray[i+1];
            byte r = cpuArray[i+2];
            byte a = cpuArray[i+3];

            cpuArray[i+0] = a;
            cpuArray[i+1] = b;
            cpuArray[i+2] = g;
            cpuArray[i+3] = r;
        }

        WritableRaster wr = out.getRaster();
        DataBufferByte db = (DataBufferByte) wr.getDataBuffer();
        System.arraycopy(cpuArray, 0, db.getData(), 0, cpuArray.length);
    }
}
