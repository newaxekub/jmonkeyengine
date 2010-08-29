package com.jme3.scene.debug;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.TempVars;

public class Arrow extends Mesh {

    private static final float[] positions = new float[]{
                    0,     0,      0,
                    0,     0,      1, // tip
                    0.05f,  0,   0.9f, // tip right
                   -0.05f,  0,   0.9f, // tip left
                    0,   0.05f,  0.9f, // tip top
                    0,  -0.05f,  0.9f, // tip buttom
    };

    public Arrow(Vector3f extent){
        float len = extent.length();
        Vector3f dir = extent.normalize();

        Quaternion tempQuat = new Quaternion();
        tempQuat.lookAt(dir, Vector3f.UNIT_Y);
        tempQuat.normalize();
        Vector3f tempVec = new Vector3f();

        float[] newPositions = new float[positions.length];
        for (int i = 0; i < positions.length; i+=3){
            Vector3f vec = tempVec.set(positions[i],
                                       positions[i+1],
                                       positions[i+2]);
            vec.multLocal(len);
            tempQuat.mult(vec, vec);

            newPositions[i]   = vec.getX();
            newPositions[i+1] = vec.getY();
            newPositions[i+2] = vec.getZ();
        }

        setBuffer(Type.Position, 3, newPositions);
        setBuffer(Type.Index, 2,
                new short[]{
                    0, 1,
                    1, 2,
                    1, 3,
                    1, 4,
                    1, 5,
                }
        );
        setMode(Mode.Lines);

        updateBound();
        updateCounts();
    }
}
