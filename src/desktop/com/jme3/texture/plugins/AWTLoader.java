package com.jme3.texture.plugins;

import com.jme3.asset.*;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.util.BufferUtils;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DirectColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

public class AWTLoader implements AssetLoader {

    public static final ColorModel AWT_RGBA4444 = new DirectColorModel(16,
                                                                       0xf000,
                                                                       0x0f00,
                                                                       0x00f0,
                                                                       0x000f);

    public static final ColorModel AWT_RGBA5551
            = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), 
                                      new int[]{5, 5, 5, 1},
                                      true,
                                      false,
                                      Transparency.BITMASK,
                                      DataBuffer.TYPE_BYTE);

    private byte[] extractImageData(BufferedImage img){
        DataBuffer buf = img.getRaster().getDataBuffer();
        switch (buf.getDataType()){
            case DataBuffer.TYPE_BYTE:
                DataBufferByte byteBuf = (DataBufferByte) buf;
                return byteBuf.getData();
        }
        return null;
    }

    private void flipImage(byte[] img, int width, int height, int bpp){
        int scSz = (width * bpp) / 8;
        byte[] sln = new byte[scSz];
        int y2 = 0;
        for (int y1 = 0; y1 < height / 2; y1++){
            y2 = height - y1 - 1;
            System.arraycopy(img, y1 * scSz, sln, 0,         scSz);
            System.arraycopy(img, y2 * scSz, img, y1 * scSz, scSz);
            System.arraycopy(sln, 0,         img, y2 * scSz, scSz);
        }
    }

    public Image load(BufferedImage img, boolean flipY){
        int width = img.getWidth();
        int height = img.getHeight();

        switch (img.getType()){
            case BufferedImage.TYPE_3BYTE_BGR: // most common in JPEG images
               byte[] dataBuf = extractImageData(img);
               if (flipY)
                   flipImage(dataBuf, width, height, 24);
               ByteBuffer data = BufferUtils.createByteBuffer(img.getWidth()*img.getHeight()*3);
               data.put(dataBuf);
               return new Image(Format.BGR8, width, height, data);
            case BufferedImage.TYPE_BYTE_GRAY: // grayscale fonts
                byte[] dataBuf2 = extractImageData(img);
                if (flipY)
                    flipImage(dataBuf2, width, height, 8);
                ByteBuffer data2 = BufferUtils.createByteBuffer(img.getWidth()*img.getHeight());
                data2.put(dataBuf2);
                return new Image(Format.Luminance8, width, height, data2);
            default:
                break;
        }

        if (img.getTransparency() == Transparency.OPAQUE){
            ByteBuffer data = BufferUtils.createByteBuffer(img.getWidth()*img.getHeight()*3);
            // no alpha
            for (int y = 0; y < height; y++){
                for (int x = 0; x < width; x++){
                    int ny = y;
                    if (flipY){
                        ny = height - y - 1;
                    }

                    int rgb = img.getRGB(x,ny);
                    byte r = (byte) ((rgb & 0x00FF0000) >> 16);
                    byte g = (byte) ((rgb & 0x0000FF00) >> 8);
                    byte b = (byte) ((rgb & 0x000000FF));
                    data.put(r).put(g).put(b);
                }
            }
            data.flip();
            return new Image(Format.RGB8, width, height, data);
        }else{
            ByteBuffer data = BufferUtils.createByteBuffer(img.getWidth()*img.getHeight()*4);
            // no alpha
            for (int y = 0; y < height; y++){
                for (int x = 0; x < width; x++){
                    int ny = y;
                    if (flipY){
                        ny = height - y - 1;
                    }

                    int rgb = img.getRGB(x,ny);
                    byte a = (byte) ((rgb & 0xFF000000) >> 24);
                    byte r = (byte) ((rgb & 0x00FF0000) >> 16);
                    byte g = (byte) ((rgb & 0x0000FF00) >> 8);
                    byte b = (byte) ((rgb & 0x000000FF));
                    data.put(r).put(g).put(b).put(a);
                }
            }
            data.flip();
            return new Image(Format.RGBA8, width, height, data);
        }
    }

    public Image load(InputStream in, boolean flipY) throws IOException{
        ImageIO.setUseCache(false);
        BufferedImage img = ImageIO.read(in);
        if (img == null)
            return null;

        return load(img, flipY);
    }

    public Object load(AssetInfo info) throws IOException {
        if (ImageIO.getImageWritersBySuffix(info.getKey().getExtension()) != null){
            InputStream in = info.openStream();
            boolean flip = ((TextureKey) info.getKey()).isFlipY();
            Image img = load(in, flip);
            in.close();
            return img;
        }
        return null;
    }
}
