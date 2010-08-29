package com.jme3.video;

import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;

public class VFrame extends Texture2D {

    private long time;

    public VFrame(int width, int height){
        super(width, height, Format.RGBA8);
        getImage().setData(BufferUtils.createByteBuffer(width*height*4));
    }

    public long getTime(){
        return time;
    }

    public void setTime(long time){
        this.time = time;
    }

    @Override
    public String toString(){
        return super.toString() + "[Time="+time+"]";
    }

}
