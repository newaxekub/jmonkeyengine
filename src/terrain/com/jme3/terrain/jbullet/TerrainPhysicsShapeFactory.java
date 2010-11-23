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

package com.jme3.terrain.jbullet;

import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainQuad;

/**
 * Create a node populated with the collision heightfield of the Terrain tree.
 * Terrain will always have a mass of 0,meaning it cannot move.
 * 
 * @author Brent Owens
 */
public class TerrainPhysicsShapeFactory {

	/**
	 * Creates the actual collision node.
	 * 
	 * @param terrain to create the collision heightfield from
	 * @return the node with the physics node
	 */
	public Node createPhysicsMesh(TerrainQuad terrain) {

		PhysicsNode n = new PhysicsNode(new HeightfieldCollisionShape(terrain.getHeightMap(), terrain.getLocalScale()), 0 );
		return n;


		//BELOW: the old way, keeping it here for a little bit as a reference (and so it gets into version control so I can always access it)
		/*Map<TerrainPatch,Vector3f> all = new HashMap<TerrainPatch,Vector3f>();
		terrain.getAllTerrainPatchesWithTranslation(all, terrain.getLocalTranslation());

		Node node = new Node();

		for (Entry<TerrainPatch,Vector3f> entry : all.entrySet()) {
			TerrainPatch tp = entry.getKey();
			Vector3f trans = entry.getValue();
			PhysicsNode n = new PhysicsNode(new HeightfieldCollisionShape(tp.getHeightmap(), trans, tp.getLocalScale()), 0 );
			n.setLocalTranslation(trans);
			node.attachChild(n);
		}*/
		
	}
}
