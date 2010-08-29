/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.shadow;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.GeometryList;

import java.util.Arrays;

import static java.lang.Math.*;

/**
 * Includes various useful shadow mapping functions.
 *
 * See:
 * http://appsrv.cse.cuhk.edu.hk/~fzhang/pssm_vrcia/
 * http://http.developer.nvidia.com/GPUGems3/gpugems3_ch10.html
 * for more info.
 */
public final class PssmShadowUtil {

    public static void main(String[] args){
        float[] splits = new float[5];
        float[] splitsShader = new float[3];
        updateFrustumSplits(splits, 1, 1000, 0.5f);
        System.arraycopy(splits, 1, splitsShader, 0, splitsShader.length);
        System.out.println(Arrays.toString(splitsShader));

        for (int i = 0; i < splits.length-1; i++){
            System.out.println(splits[i] + " - " + splits[i+1]);
        }
    }

    /**
     * Updates the frustum splits stores in <code>splits</code> using PSSM.
     */
    public static void updateFrustumSplits(float[] splits, float near, float far, float lambda) {
        for (int i = 0; i < splits.length; i++) {
            float IDM = i / (float) splits.length;
            float log = near * FastMath.pow((far / near), IDM);
            float uniform = near + (far - near) * IDM;
            splits[i] = log * lambda + uniform * (1.0f - lambda);
        }

        // This is used to improve the correctness of the calculations. Our main near- and farplane
        // of the camera always stay the same, no matter what happens.
        splits[0] = near;
        splits[splits.length - 1] = far;
    }

    /**
     * Compute the Zfar in the model vieuw to adjust the Zfar distance for the splits calculation
     */
    public static float computeZFar(GeometryList occ, GeometryList recv, Camera cam) {
        Matrix4f mat = cam.getViewMatrix();
        BoundingBox bbOcc  = ShadowUtil.computeUnionBound(occ, mat);
        BoundingBox bbRecv = ShadowUtil.computeUnionBound(recv, mat);

        return min(max(bbOcc.getZExtent() - bbOcc.getCenter().z, bbRecv.getZExtent() - bbRecv.getCenter().z), cam.getFrustumFar());
    }
}
