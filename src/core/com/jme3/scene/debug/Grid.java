package com.jme3.scene.debug;

import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Grid extends Mesh {

    public Grid(int xLines, int yLines, float lineDist){
        xLines -= 2;
        yLines -= 2;
        int lineCount = xLines + yLines + 4;

        FloatBuffer fpb = BufferUtils.createFloatBuffer(6 * lineCount);
        ShortBuffer sib = BufferUtils.createShortBuffer(2 * lineCount);

        float xLineLen = (yLines + 1) * lineDist;
        float yLineLen = (xLines + 1) * lineDist;
        int curIndex = 0;

        // add lines along X
        for (int i = 0; i < xLines + 2; i++){
            float y = (i) * lineDist;

            // positions
            fpb.put(0)       .put(0).put(y);
            fpb.put(xLineLen).put(0).put(y);

            // indices
            sib.put( (short) (curIndex++) );
            sib.put( (short) (curIndex++) );
        }

        // add lines along Y
        for (int i = 0; i < yLines + 2; i++){
            float x = (i) * lineDist;

            // positions
            fpb.put(x).put(0).put(0);
            fpb.put(x).put(0).put(yLineLen);

            // indices
            sib.put( (short) (curIndex++) );
            sib.put( (short) (curIndex++) );
        }

        fpb.flip();
        sib.flip();

        setBuffer(Type.Position, 3, fpb);
        setBuffer(Type.Index, 2, sib);
        
        setMode(Mode.Lines);

        updateBound();
        updateCounts();
    }
}
