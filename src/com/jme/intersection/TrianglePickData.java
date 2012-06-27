/*
 * Copyright (c) 2003-2009 jMonkeyEngine
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

package com.jme.intersection;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.jme.math.FastMath;
import com.jme.math.Ray;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;

/**
 * Pick data for triangle accuracy picking including sort by distance to
 * intersection point.
 */
public class TrianglePickData extends PickData {
    private static final Logger logger = Logger.getLogger(TrianglePickData.class.getName());

    private final Vector3f[] vertices = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f()};

    private Vector3f intersectionPoint = new Vector3f();
    private int intersectionTri = -1;
    private TriMesh intersectionMesh = null;

    public TrianglePickData(Ray ray, TriMesh targetMesh,
			ArrayList<Integer> targetTris, boolean checkDistance) {
		super(ray, targetMesh, targetTris, false);
        if (checkDistance) {
            distance = calculateDistance();
        }
	}

	protected float calculateDistance() {
		ArrayList<Integer> tris = getTargetTris();
		if (tris.isEmpty()) {
			return Float.POSITIVE_INFINITY;
		}

        TriMesh mesh = (TriMesh) getTargetMesh();

        //don't update world vectors here - it was has to be done before the intersection
        //mesh.getParentGeom().updateWorldVectors();

        float distanceSq = Float.POSITIVE_INFINITY;
		float[] distances = new float[tris.size()];
		for (int i = 0; i < tris.size(); i++) {
			int triIndex = tris.get( i );
			mesh.getTriangle(triIndex, vertices);
			float triDistanceSq = getDistanceSquaredToTriangle( vertices, mesh );
			distances[i] = triDistanceSq;
			if (triDistanceSq > 0 && triDistanceSq < distanceSq) {
                distanceSq = triDistanceSq;
			}
		}
		
		//XXX optimize! ugly bubble sort for now
		boolean sorted = false;
		while(!sorted) {
			sorted = true;
			for(int sort = 0; sort < distances.length - 1; sort++) {
				if(distances[sort] > distances[sort+1]) {
					//swap
					sorted = false;
					float temp = distances[sort+1];
					distances[sort+1] = distances[sort];
					distances[sort] = temp;
					
					//swap tris too
					int temp2 = tris.get(sort+1);
					tris.set(sort+1, tris.get(sort));
					tris.set(sort, temp2);
				}
			}
		}
		
		intersectionTri = tris.get(0).intValue();
                intersectionMesh = mesh;
                
                mesh.getTriangle(intersectionTri, vertices);
                intersectionPoint = getIntersectionPoint(vertices, mesh);
                        
		if (Float.isInfinite( distanceSq )) {
            		return distanceSq;
        	} else
			return FastMath.sqrt(distanceSq);
	}

	private float getDistanceSquaredToTriangle(Vector3f[] triangle, Spatial spatial) {
            Vector3f intersection = getIntersectionPoint(triangle, spatial);
            if (intersection != null) {
                return getRay().getOrigin().distanceSquared(intersection);
            }

            // Should not happen
            //TODO: removed because it does happen = spamming... need to find out why instead
            //  logger.warning("Couldn't detect nearest triangle intersection!");
            return Float.POSITIVE_INFINITY;
        }
        
        private Vector3f getIntersectionPoint(Vector3f[] triangle, Spatial spatial) {
            // transform triangle to world space
            Vector3f t0 = spatial.localToWorld(triangle[0], null);
            Vector3f t1 = spatial.localToWorld(triangle[1], null);
            Vector3f t2 = spatial.localToWorld(triangle[2], null);
        
            // calculate intersection
            Ray ray = getRay();
            Vector3f intersection = new Vector3f();
            if (!ray.intersectWhere(t0, t1, t2, intersection)) {
                // no intersection
                return null;
            }
            
            return intersection;
            
        }
        
        public void setIntersectionPoint(Vector3f v) {
            intersectionPoint.set(v);
        }
        
        public void getIntersectionPoint(Vector3f out) {
            out.set(intersectionPoint);
        }

        public int getIntersectionTri() {
            return(intersectionTri);
        }

        public TriMesh getIntersectionMesh() {
            return(intersectionMesh);
        }
}
