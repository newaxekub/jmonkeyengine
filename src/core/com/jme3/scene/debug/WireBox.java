package com.jme3.scene.debug;

import com.jme3.bounding.BoundingBox;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.VertexBuffer.Usage;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;

public class WireBox extends Mesh {

    public WireBox(){
        this(1,1,1);
    }
    
    public WireBox(float xExt, float yExt, float zExt){
        updatePositions(xExt,yExt,zExt);
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

        updateCounts();
    }

    public void updatePositions(float xExt, float yExt, float zExt){
        VertexBuffer pvb = getBuffer(Type.Position);
        FloatBuffer pb;
        if (pvb == null){
            pvb = new VertexBuffer(Type.Position);
            pb = BufferUtils.createVector3Buffer(8);
            pvb.setupData(Usage.Dynamic, 3, Format.Float, pb);
            setBuffer(pvb);
        }else{
            pb = (FloatBuffer) pvb.getData();
        }
        pb.rewind();
        pb.put(
            new float[]{
                -xExt, -yExt,  zExt,
                 xExt, -yExt,  zExt,
                 xExt,  yExt,  zExt,
                -xExt,  yExt,  zExt,

                -xExt, -yExt, -zExt,
                 xExt, -yExt, -zExt,
                 xExt,  yExt, -zExt,
                -xExt,  yExt, -zExt,
            }
        );
        updateBound();
    }

    public void fromBoundingBox(BoundingBox bbox){
        updatePositions(bbox.getXExtent(), bbox.getYExtent(), bbox.getZExtent());
    }

}
