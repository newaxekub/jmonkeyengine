/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jme3.renderer.queue;

import com.jme3.material.Material;
import com.jme3.material.Technique;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;

public class OpaqueComparator implements GeometryComparator {

    private Camera cam;
    private final Vector3f tempVec = new Vector3f();

    public void setCamera(Camera cam){
        this.cam = cam;
    }

    public float distanceToCam(Geometry spat){
        if (spat == null)
            return Float.NEGATIVE_INFINITY;

        if (spat.queueDistance != Float.NEGATIVE_INFINITY)
                return spat.queueDistance;

        Vector3f camPosition = cam.getLocation();
        Vector3f viewVector = cam.getDirection();
        Vector3f spatPosition = null;

        if (spat.getWorldBound() != null){
            spatPosition = spat.getWorldBound().getCenter();
        }else{
            spatPosition = spat.getWorldTranslation();
        }

        spatPosition.subtract(camPosition, tempVec);
        spat.queueDistance = tempVec.dot(tempVec);

        float retval = Math.abs(tempVec.dot(viewVector)
                / viewVector.dot(viewVector));
        viewVector.mult(retval, tempVec);

        spat.queueDistance = tempVec.length();

        return spat.queueDistance;
    }

    public int compare(Geometry o1, Geometry o2) {
        if (o1 == null || o2 == null)
            return -1;

        Material m1 = o1.getMaterial();
        Material m2 = o2.getMaterial();
        Technique t1 = m1.getActiveTechnique();
        Technique t2 = m2.getActiveTechnique();
        int sid1 = -1, sid2 = -1;
        if (t1 != null){
            if (t1.getShader() != null){
                sid1 = t1.getShader().getId();
            }
        }
        if (t2 != null){
            if (t2.getShader() != null){
                sid2 = t2.getShader().getId();
            }
        }
        sid1++; sid2++;
//        sid1 *= 1000;
//        sid2 *= 1000;
//        float near = renderer.getCamera().getFrustumNear();
//        float far  = renderer.getCamera().getFrustumFar();
//        d1 = (d1 - near) / far;
//        d2 = (d2 - near) / far;
//        sid1 += d1 * 1000;
//        sid2 += d2 * 1000;

        if (sid1 == sid2){
            // use the same shader.
            // sort front-to-back then.
            
            float d1 = distanceToCam(o1);
            float d2 = distanceToCam(o2);

            if (d1 == d2)
                return 0;
            else if (d1 < d2)
                return -1;
            else
                return 1;
            
        } else if (sid1 < sid2)
            return 1;
        else
            return -1;
    }

}
