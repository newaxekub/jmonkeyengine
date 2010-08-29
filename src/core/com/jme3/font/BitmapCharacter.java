package com.jme3.font;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.util.IntMap;
import com.jme3.util.IntMap.Entry;
import java.io.IOException;

/**
 * Represents a single bitmap character.
 */
public class BitmapCharacter implements Savable {

    private int x;
    private int y;
    private int width;
    private int height;
    private int xOffset;
    private int yOffset;
    private int xAdvance;
    private IntMap<Integer> kerning = new IntMap<Integer>();

    // / <summary>Clones the BitmapCharacter</summary>
    // / <returns>Cloned BitmapCharacter</returns>
    public BitmapCharacter Clone() {
        BitmapCharacter result = new BitmapCharacter();
        result.x = x;
        result.y = y;
        result.width = width;
        result.height = height;
        result.xOffset = xOffset;
        result.yOffset = yOffset;
        result.xAdvance = xAdvance;
        result.kerning = kerning.clone();
        return result;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getXOffset() {
        return xOffset;
    }

    public void setXOffset(int offset) {
        xOffset = offset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public void setYOffset(int offset) {
        yOffset = offset;
    }

    public int getXAdvance() {
        return xAdvance;
    }

    public void setXAdvance(int advance) {
        xAdvance = advance;
    }

    public void addKerning(int second, int amount){
        kerning.put(second, amount);
    }

    public int getKerning(int second){
        Integer i = kerning.get(second);
        if (i == null)
            return -1;
        else
            return i.intValue();
    }

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(x, "x", 0);
        oc.write(y, "y", 0);
        oc.write(width, "width", 0);
        oc.write(height, "height", 0);
        oc.write(xOffset, "xOffset", 0);
        oc.write(yOffset, "yOffset", 0);
        oc.write(xAdvance, "xAdvance", 0);

        int[] seconds = new int[kerning.size()];
        int[] amounts = new int[seconds.length];

        int i = 0;
        for (Entry<Integer> entry : kerning){
            seconds[i] = entry.getKey();
            amounts[i] = entry.getValue();
            i++;
        }

        oc.write(seconds, "seconds", null);
        oc.write(amounts, "amounts", null);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        x = ic.readInt("x", 0);
        y = ic.readInt("y", 0);
        width = ic.readInt("width", 0);
        height = ic.readInt("height", 0);
        xOffset = ic.readInt("xOffset", 0);
        yOffset = ic.readInt("yOffset", 0);
        xAdvance = ic.readInt("xAdvance", 0);

        int[] seconds = ic.readIntArray("seconds", null);
        int[] amounts = ic.readIntArray("amounts", null);

        for (int i = 0; i < seconds.length; i++){
            kerning.put(seconds[i], amounts[i]);
        }
    }
}