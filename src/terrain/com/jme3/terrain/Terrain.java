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

package com.jme3.terrain;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import java.util.List;

public interface Terrain {

	/**
	 * Get the height of the terrain at the specified X-Z coorindate.
	 * @param xz the X-Z coordinate
	 * @return
	 */
	public float getHeight(Vector2f xz);
	
	/**
	 * Set the height at the specified X-Z coordinate.
	 * @param xzCoordinate coordinate to set the height
	 * @param height that will be set at the coordinate
	 */
	public void setHeight(Vector2f xzCoordinate, float height);
	
	/**
	 * Get the heightmap of the entire terrain.
	 * This can return null if that terrain object does not store the height data
	 */
	public float[] getHeightMap();

	/**
	 * Tell the terrain system to use/not use Level of Detail algorithms.
	 */
	public void useLOD(boolean useLod);
	
	public boolean isUsingLOD();
	
	/**
	 * This is calculated by the specific LOD algorithm.
	 * A value of one means that the terrain is showing full detail.
	 * The higher the value, the more the terrain has been generalized 
	 * and the less detailed it will be.
	 */
	public int getMaxLod();
	
	/**
	 * Called in the update (pre or post, up to you) method of your game.
	 * Calculates the level of detail of the terrain and adjusts its geometry.
	 * This is where the Terrain's LOD algorithm will change the detail of 
	 * the terrain based on how far away this position is from the particular
	 * terrain patch.
	 * @param location often the Camera's location
	 */
	public void update(List<Vector3f> location);
	
}
