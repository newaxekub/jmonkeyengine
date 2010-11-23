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

package com.jme3.terrain.geomipmap;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.terrain.BufferGeomap;
import com.jme3.util.BufferUtils;

/**
 * Produces the mesh for the TerrainPatch.
 * This LOD algorithm generates a single triangle strip by first building the center of the
 * mesh, minus one outer edge around it. Then it builds the edges in counter-clockwise order, 
 * starting at the bottom right and working up, then left across the top, then down across the
 * left, then right across the bottom.
 * It needs to know what its neighbour's LOD's are so it can stitch the edges. 
 * It creates degenerate polygons in order to keep the winding order of the polygons and to move
 * the strip to a new position while still maintaining the continuity of the overall mesh. These
 * degenerates are removed quickly by the video card.
 * 
 * @author Brent Owens
 */
public class LODGeomap extends BufferGeomap {

	private int maxLod = -1;
	
	
	public LODGeomap(int size, FloatBuffer heightMap) {
		super(heightMap, null, size, size, 1);
		
		maxLod = Math.max(1, (int) (FastMath.log(size-1)/FastMath.log(2)) -1);
	}
	
	public Mesh createMesh(Vector3f scale, Vector2f tcScale, Vector2f tcOffset, float offsetAmount, int totalSize, boolean center) {
		return this.createMesh(scale, tcScale, tcOffset, offsetAmount, totalSize, center, 1, false,false,false,false);
	}
	
	public Mesh createMesh(Vector3f scale, Vector2f tcScale, Vector2f tcOffset, float offsetAmount, int totalSize, boolean center, int lod, boolean rightLod, boolean topLod, boolean leftLod, boolean bottomLod){
		FloatBuffer pb = writeVertexArray(null, scale, center);
		FloatBuffer tb = writeTexCoordArray(null, tcOffset, tcScale, offsetAmount, totalSize);
		FloatBuffer nb = writeNormalArray(null, scale);
		IntBuffer ib = writeIndexArrayLodDiff(null, lod, rightLod, topLod, leftLod, bottomLod);
		Mesh m = new Mesh();
		m.setMode(Mode.TriangleStrip);
		m.setBuffer(Type.Position, 3, pb);
		m.setBuffer(Type.Normal, 3, nb);
		m.setBuffer(Type.TexCoord, 2, tb);
		m.setBuffer(Type.Index, 3, ib);
		m.setStatic();
		m.updateBound();
		return m;
	}
	
	public FloatBuffer writeTexCoordArray(FloatBuffer store, Vector2f offset, Vector2f scale, float offsetAmount, int totalSize){
		if (store!=null){
			if (store.remaining() < getWidth()*getHeight()*2)
				throw new BufferUnderflowException();
		}else{
			store = BufferUtils.createFloatBuffer(getWidth()*getHeight()*2);
		}

		if (offset == null)
			offset = new Vector2f();

		Vector2f tcStore = new Vector2f();
		
		for (int y = 0; y < getHeight(); y++){
			
			for (int x = 0; x < getWidth(); x++){
				getUV(x,y,tcStore, offset, offsetAmount, totalSize);
				float tx = tcStore.x * scale.x;
				float ty = tcStore.y * scale.y;
				store.put( tx );
				store.put( ty );
			}
		}
		
		return store;
	}
	
	public Vector2f getUV(int x, int y, Vector2f store, Vector2f offset, float offsetAmount, int totalSize){
		float offsetX = offset.x + (offsetAmount * 1.0f);//stepScale.x);
        float offsetY = offset.y + (offsetAmount * 1.0f);//stepScale.z);
        
        store.set( ( ((float)x)+offsetX) / (float)totalSize, // calculates percentage of texture here
                   ( ((float)y)+offsetY) / (float)totalSize );
        return store;
    }

	/**
	 * Create the LOD index array that will seam its edges with its neighbour's LOD.
	 * This is a scary method!!! It will break your mind.
	 * 
	 * @param store to store the index buffer
	 * @param lod level of detail of the mesh 
	 * @param rightLod LOD of the right neighbour
	 * @param topLod LOD of the top neighbour
	 * @param leftLod LOD of the left neighbour
	 * @param bottomLod LOD of the bottom neighbour
	 * @return the LOD-ified index buffer
	 */
	public IntBuffer writeIndexArrayLodDiff(IntBuffer store, int lod, boolean rightLod, boolean topLod, boolean leftLod, boolean bottomLod){
		
		IntBuffer buffer2 = store;
		int numIndexes = calculateNumIndexesLodDiff(lod);
		if (store == null)
			buffer2 = BufferUtils.createIntBuffer(numIndexes);
		VerboseIntBuffer buffer = new VerboseIntBuffer(buffer2);
		
		
		// generate center squares minus the edges
		//System.out.println("for (x="+lod+"; x<"+(getWidth()-(2*lod))+"; x+="+lod+")");
		//System.out.println("	for (z="+lod+"; z<"+(getWidth()-(1*lod))+"; z+="+lod+")");
		for (int r=lod; r<getWidth()-(2*lod); r+=lod) { // row
			int rowIdx = r*getWidth();
			int nextRowIdx = (r+1*lod)*getWidth();
			for (int c=lod; c<getWidth()-(1*lod); c+=lod) { // column
				int idx = rowIdx+c;
				buffer.put(idx);
				idx = nextRowIdx+c;
				buffer.put(idx);
			}
			
			// add degenerate triangles
			if (r < getWidth()-(3*lod)) {
				int idx = nextRowIdx+getWidth()-(1*lod)-1;
				buffer.put(idx);
				idx = nextRowIdx+(1*lod); // inset by 1
				buffer.put(idx);
				//System.out.println("");
			}
		}
		//System.out.println("\nright:");
		
		//int runningBufferCount = buffer.getCount();
		//System.out.println("buffer start: "+runningBufferCount);
		
		
		// right
		int br = getWidth()*(getWidth()-lod)-1-lod;
		buffer.put(br); // bottom right -1
		int corner = getWidth()*getWidth()-1;
		buffer.put(corner);	// bottom right corner
		if (rightLod) { // if lower LOD
			for (int row=getWidth()-lod; row>=1+lod; row-=2*lod) {
				int idx = (row)*getWidth()-1-lod;
				buffer.put(idx);
				idx = (row-lod)*getWidth()-1;
				buffer.put(idx);
				if (row > lod+1) { //if not the last one 
					idx = (row-lod)*getWidth()-1-lod;
					buffer.put(idx);
					idx = (row-lod)*getWidth()-1;
					buffer.put(idx);
				} else {
					
				}
			}
		} else {
			buffer.put(corner);//br+1);//degenerate to flip winding order
			for (int row=getWidth()-lod; row>lod; row-=lod) {
				int idx = row*getWidth()-1; // mult to get row
				buffer.put(idx);
				buffer.put(idx-lod);
			}
			
		}
		
		buffer.put(getWidth()-1);
		
		
		//System.out.println("\nbuffer right: "+(buffer.getCount()-runningBufferCount));
		//runningBufferCount = buffer.getCount();
		
		
		//System.out.println("\ntop:");
		
		// top 			(the order gets reversed here so the diagonals line up)
		if (topLod) { // if lower LOD
			if (rightLod)
				buffer.put(getWidth()-1);
			for (int col=getWidth()-1; col>=lod; col-=2*lod) { 
				int idx = (lod*getWidth())+col-lod; // next row
				buffer.put(idx);
				idx = col-2*lod;
				buffer.put(idx);
				if (col > lod*2) { //if not the last one
					idx = (lod*getWidth())+col-2*lod;
					buffer.put(idx);
					idx = col-2*lod;
					buffer.put(idx);
				} else {
					
				}
			}
		} else {
			if (rightLod)
				buffer.put(getWidth()-1);
			for (int col=getWidth()-1-lod; col>0; col-=lod) {
				int idx = col + (lod*getWidth());
				buffer.put(idx);
				idx = col;
				buffer.put(idx);
			}
			buffer.put(0);
		}
		buffer.put(0);
		
		//System.out.println("\nbuffer top: "+(buffer.getCount()-runningBufferCount));
		//runningBufferCount = buffer.getCount();
		
		//System.out.println("\nleft:");
		
		// left
		if (leftLod) { // if lower LOD
			if (topLod)
				buffer.put(0);
			for (int row=0; row<getWidth()-lod; row+=2*lod) {
				int idx = (row+lod)*getWidth()+lod;
				buffer.put(idx);
				idx = (row+2*lod)*getWidth();
				buffer.put(idx);
				if (row < getWidth()-lod-2-1) { //if not the last one
					idx = (row+2*lod)*getWidth()+lod;
					buffer.put(idx);
					idx = (row+2*lod)*getWidth();
					buffer.put(idx);
				} else {
					
				}
			}
		} else {
			if (!topLod)
				buffer.put(0);
			//buffer.put(getWidth()+1); // degenerate
			//buffer.put(0); // degenerate winding-flip
			for (int row=lod; row<getWidth()-lod; row+=lod) {
				int idx = row*getWidth();
				buffer.put(idx);
				idx = row*getWidth()+lod;
				buffer.put(idx);
			}
			
		}
		buffer.put(getWidth()*(getWidth()-1));
		
		
		//System.out.println("\nbuffer left: "+(buffer.getCount()-runningBufferCount));
		//runningBufferCount = buffer.getCount();
		
		//if (true) return buffer.delegate;
		//System.out.println("\nbottom");
		
		// bottom
		if (bottomLod) { // if lower LOD
			if (leftLod)
				buffer.put(getWidth()*(getWidth()-1));
			// there was a slight bug here when really high LOD near maxLod
			// far right has extra index one row up and all the way to the right, need to skip last index entered
			// seemed to be fixed by making "getWidth()-1-2-lod" this: "getWidth()-1-2*lod", which seems more correct
			for (int col=0; col<getWidth()-lod; col+=2*lod) {
				int idx = getWidth()*(getWidth()-1-lod)+col+lod;
				buffer.put(idx);
				idx = getWidth()*(getWidth()-1)+col+2*lod;
				buffer.put(idx);
				if (col < getWidth()-1-2*lod) { //if not the last one
					idx = getWidth()*(getWidth()-1-lod)+col+2*lod;
					buffer.put(idx);
					idx = getWidth()*(getWidth()-1)+col+2*lod;
					buffer.put(idx);
				} else {
					
				}
			}
		} else {
			if (leftLod) {
				buffer.put(getWidth()*(getWidth()-1));
			}
			for (int col=lod; col<getWidth()-lod; col+=lod) {
				int idx = getWidth()*(getWidth()-1-lod) + col; // up
				buffer.put(idx);
				idx = getWidth()*(getWidth()-1) + col; // down
				buffer.put(idx);
			}
			//buffer.put(getWidth()*getWidth()-1-lod); // <-- THIS caused holes at the end!
		}
		
		buffer.put(getWidth()*getWidth()-1);
		
		//System.out.println("\nbuffer bottom: "+(buffer.getCount()-runningBufferCount));
		//runningBufferCount = buffer.getCount();
		
		//System.out.println("\nBuffer size: "+buffer.getCount());
		
		// fill in the rest of the buffer with degenerates, there should only be a couple
		for (int i=buffer.getCount(); i<numIndexes; i++)
			buffer.put(getWidth()*getWidth()-1);
		
		
		
		return buffer.delegate;
	}
	
	
	/*private int calculateNumIndexesNormal(int lod) {
		int length = getWidth()-1;
		int num = ((length/lod)+1)*((length/lod)+1)*2;
		System.out.println("num: "+num);
		num -= 2*((length/lod)+1);
		System.out.println("num2: "+num);
		// now get the degenerate indexes that exist between strip rows
		num += 2*(((length/lod)+1)-2); // every row except the first and last
		System.out.println("Index buffer size: "+num);
		return num;
	}*/
	
	/**
	 * calculate how many indexes there will be.
	 * This isn't that precise and there might be a couple extra.
	 */
	private int calculateNumIndexesLodDiff(int lod) {
		
		int length = getWidth()-1; // make it even for lod calc
		int side = (length/lod)+1 -(2);
		//System.out.println("side: "+side);
		int num = side*side*2;
		//System.out.println("num: "+num);
		num -= 2*side;	// remove one first row and one last row (they are only hit once each)
		//System.out.println("num2: "+num);
		// now get the degenerate indexes that exist between strip rows
		int degenerates = 2*(side-(2)); // every row except the first and last
		num += degenerates;
		//System.out.println("degenerates: "+degenerates);
		
		//System.out.println("center, before edges: "+num);
		
		num += (getWidth()/lod)*2 *4;
		num++;
		
		num+=10;// TODO remove me: extra
		//System.out.println("Index buffer size: "+num);
		return num;
	}
	
	/**
	 * Keeps a count of the number of indexes, good for debugging
	 */
	public class VerboseIntBuffer {
		private IntBuffer delegate;
		int count = 0;
		public VerboseIntBuffer(IntBuffer d) {
			delegate = d;
		}
		public void put(int value) {
			try {
				//System.out.print(value+",");
				delegate.put(value);
				count++;
			} catch (BufferOverflowException e) {
				//System.out.println("err buffer size: "+delegate.capacity());
			}
		}
		public int getCount() {
			return count;
		}
	}
	
}

