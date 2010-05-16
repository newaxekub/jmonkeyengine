package com.jme3.scene.debug;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;

public class WireFrustum extends Mesh {

    public WireFrustum(Vector3f[] points){
        if (points != null)
            setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(points));

        setBuffer(Type.Index, 2,
                new short[]{
                     0, 1,
                     1, 2,
                     2, 3,
                     3, 0,

                     4, 5,
                     5, 6,
                     6, 7,
                     7, 4,

                     0, 4,
                     1, 5,
                     2, 6,
                     3, 7,
                }
        );
        setMode(Mode.Lines);
    }

    public void update(Vector3f[] points){
        VertexBuffer vb = getBuffer(Type.Position);
        if (vb == null){
            setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(points));
            return;
        }


        FloatBuffer b = BufferUtils.createFloatBuffer(points);
        FloatBuffer a = (FloatBuffer) vb.getData();
        b.rewind();
        a.rewind();
        a.put(b);
        a.rewind();

        vb.setUpdateNeeded();
    }

}
