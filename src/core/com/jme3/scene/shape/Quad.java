package com.jme3.scene.shape;

import com.jme3.scene.*;
import com.jme3.scene.VertexBuffer.Type;

public class Quad extends Mesh {

    private float width;
    private float height;

    /**
     * Do not use this constructor. Serialization purposes only.
     */
    public Quad(){
    }

    public Quad(float width, float height){
        updateGeometry(width, height);
    }

    public Quad(float width, float height, boolean flipCoords){
        updateGeometry(width, height, flipCoords);
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    public void updateGeometry(float width, float height){
        updateGeometry(width, height, false);
    }

    public void updateGeometry(float width, float height, boolean flipCoords) {
        this.width = width;
        this.height = height;
        setBuffer(Type.Position, 3, new float[]{0,      0,      0,
                                                width,  0,      0,
                                                width,  height, 0,
                                                0,      height, 0
                                                });
        

        if (flipCoords){
            setBuffer(Type.TexCoord, 2, new float[]{0, 1,
                                                    1, 1,
                                                    1, 0,
                                                    0, 0});
        }else{
            setBuffer(Type.TexCoord, 2, new float[]{0, 0,
                                                    1, 0,
                                                    1, 1,
                                                    0, 1});
        }
        setBuffer(Type.Normal, 3, new float[]{0, 0, 1,
                                              0, 0, 1,
                                              0, 0, 1,
                                              0, 0, 1});
        if (height < 0){
            setBuffer(Type.Index, 3, new short[]{0, 2, 1,
                                                 0, 3, 2});
        }else{
            setBuffer(Type.Index, 3, new short[]{0, 1, 2,
                                                 0, 2, 3});
        }
        

        updateBound();
    }


}
