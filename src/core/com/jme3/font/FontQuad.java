package com.jme3.font;

import com.jme3.math.ColorRGBA;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

public class FontQuad {

    private int lineNumber;
    private int wordNumber;
    private float sizeScale;
    private BitmapCharacter bitmapChar = null;
    private char character;
    private float wordWidth;
    private float totalWidth;

    private float quadPosX;
    private float quadPosY;
    private float quadTexX;
    private float quadTexY;
    private float quadPosWidth;
    private float quadPosHeight;
    private float quadTexWidth;
    private float quadTexHeight;
    
    private final ColorRGBA color = new ColorRGBA();
    private int colorInt = 0xFFFFFFFF;

    public FontQuad() {
    }

    public void appendPositions(FloatBuffer fb){
        // NOTE: subtracting the height here
        // because OGL's Ortho origin is at lower-left
        fb.put(quadPosX).put(quadPosY).put(0f);
        fb.put(quadPosX).put(quadPosY - quadPosHeight).put(0f);
        fb.put(quadPosX + quadPosWidth).put(quadPosY - quadPosHeight).put(0f);
        fb.put(quadPosX + quadPosWidth).put(quadPosY).put(0f);
    }

    public void appendPositions(ShortBuffer sb){
        short x = (short) quadPosX;
        short y = (short) quadPosY;
        short xpw = (short) (x + quadPosWidth);
        short ymh = (short) (y - quadPosHeight);
        
        sb.put(x).put(y).put((short)0);
        sb.put(x).put(ymh).put((short)0);
        sb.put(xpw).put(ymh).put((short)0);
        sb.put(xpw).put(y).put((short)0);
    }

    public void appendTexCoords(FloatBuffer fb){
        // flip coords to be compatible with OGL
        float u0 = quadTexX;
        float v0 = 1f - quadTexY;
        float u1 = u0 + quadTexWidth;
        float v1 = v0 - quadTexHeight;

        // upper left
        fb.put(u0).put(v0);
        // lower left
        fb.put(u0).put(v1);
        // lower right
        fb.put(u1).put(v1);
        // upper right
        fb.put(u1).put(v0);
    }

    public void appendColors(ByteBuffer bb){
        if (color != null){
            bb.putInt(colorInt);
            bb.putInt(colorInt);
            bb.putInt(colorInt);
            bb.putInt(colorInt);
        }else{
            bb.putInt(0xFFFFFFFF);
            bb.putInt(0xFFFFFFFF);
            bb.putInt(0xFFFFFFFF);
            bb.putInt(0xFFFFFFFF);
        }
    }

    public void appendIndices(ShortBuffer sb, int quadIndex){
        // each quad has 4 indices
        short v0 = (short) (quadIndex * 4);
        short v1 = (short) (v0 + 1);
        short v2 = (short) (v0 + 2);
        short v3 = (short) (v0 + 3);

        sb.put(v0).put(v1).put(v2);
        sb.put(v0).put(v2).put(v3);
//        sb.put(new short[]{ v0, v1, v2,
//                            v0, v2, v3 });
    }

    public void storeToArrays(float[] pos, float[] tc, short[] idx, byte[] colors, int quadIdx){
        float x = quadPosX;
        float y = quadPosY;
        float xpw = (x + quadPosWidth);
        float ymh = (y - quadPosHeight);

        pos[0] = x;   pos[1]  = y;   pos[2]  = 0;
        pos[3] = x;   pos[4]  = ymh; pos[5]  = 0;
        pos[6] = xpw; pos[7]  = ymh; pos[8]  = 0;
        pos[9] = xpw; pos[10] = y;   pos[11] = 0;

        float u0 = quadTexX;
        float v0 = 1f - quadTexY;
        float u1 = u0 + quadTexWidth;
        float v1 = v0 - quadTexHeight;

        tc[0] = u0; tc[1] = v0;
        tc[2] = u0; tc[3] = v1;
        tc[4] = u1; tc[5] = v1;
        tc[6] = u1; tc[7] = v0;

        if (color != null){
            colors[0] = (byte) (colorInt & 0xff);
            colors[1] = (byte) ((colorInt >> 8) & 0xff);
            colors[2] = (byte) ((colorInt >> 16) & 0xff);
            colors[3] = (byte) ((colorInt >> 24) & 0xff);
            System.arraycopy(colors, 0, colors, 4,  4);
            System.arraycopy(colors, 0, colors, 8,  4);
            System.arraycopy(colors, 0, colors, 12, 4);
        }else{
            Arrays.fill(colors, (byte) 0xff);
        }

        short i0 = (short) (quadIdx * 4);
        short i1 = (short) (i0 + 1);
        short i2 = (short) (i0 + 2);
        short i3 = (short) (i0 + 3);

        idx[0] = i0; idx[1] = i1; idx[2] = i2;
        idx[3] = i0; idx[4] = i2; idx[5] = i3;
    }
    
    public void setSize(float width, float height){
        quadPosWidth = width;
        quadPosHeight = height;
    }

    public void setPosition(float x, float y){
        quadPosX = x;
        quadPosY = y;
    }

    public void setX(float x){
        quadPosX = x;
    }

    public void setY(float y){
        quadPosY = y;
    }

    public float getX() {
        return quadPosX;
    }

    public float getY() {
        return quadPosY;
    }
    
    public void setUV(float u, float v, float uSize, float vSize){
        quadTexX = u;
        quadTexY = v;
        quadTexWidth = uSize;
        quadTexHeight = vSize;
    }

    public void setColor(ColorRGBA color){
        this.color.set(color);
        colorInt = color.asIntABGR();
    }

    public ColorRGBA getColor() {
        return color;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int number) {
        lineNumber = number;
    }

    public int getWordNumber() {
        return wordNumber;
    }

    public void setWordNumber(int number) {
        wordNumber = number;
    }

    public float getSizeScale() {
        return sizeScale;
    }

    public void setSizeScale(float scale) {
        sizeScale = scale;
    }

    public BitmapCharacter getBitmapChar() {
        return bitmapChar;
    }

    public void setBitmapChar(BitmapCharacter ch) {
        bitmapChar = ch;
    }

    public char getCharacter() {
        return character;
    }

    public void setCharacter(char m_character) {
        this.character = m_character;
    }

    public float getWordWidth() {
        return wordWidth;
    }

    public void setWordWidth(float width) {
        wordWidth = width;
    }

    public void setTotalWidth(float totalWidth) {
        this.totalWidth = totalWidth;
    }

    public float getTotalWidth() {
        return totalWidth;
    }
}