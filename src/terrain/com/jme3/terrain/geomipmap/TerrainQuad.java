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

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireBox;
import com.jme3.terrain.Terrain;
import com.jme3.terrain.geomipmap.lodcalc.LodCalculatorFactory;
import com.jme3.terrain.geomipmap.lodcalc.LodDistanceCalculatorFactory;
import com.jme3.terrain.geomipmap.picking.BresenhamTerrainPicker;
import com.jme3.terrain.geomipmap.picking.TerrainPickData;
import com.jme3.terrain.geomipmap.picking.TerrainPicker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A terrain quad is a node in the quad tree of the terrain system.
 * The root terrain quad will be the only one that receives the update() call every frame
 * and it will determine if there has been any LOD change.
 * 
 * The leaves of the terrain quad tree are Terrain Patches. These have the real geometry mesh.
 * 
 * @author Brent Owens
 */
public class TerrainQuad extends Node implements Terrain {

	protected Vector2f offset;

	protected int totalSize;

	protected int size;

	protected Vector3f stepScale;

	protected float offsetAmount;

	protected int quadrant = 1;
	
	protected LodCalculatorFactory lodCalculatorFactory;

    /* This heightmap is stored in the root quad only and is used for fast collision,
     * so we don't have to build up the heightmap array from all of the children
     */
	protected float[] heightMap;


	protected List<Vector3f> lastCameraLocations; // used for LOD calc
	private boolean lodCalcRunning = false;
	private boolean usingLOD = true;
	private int maxLod = -1;
	private HashMap<String,UpdatedTerrainPatch> updatedPatches;
	private Object updatePatchesLock = new Object();

    private TerrainPicker picker;

	
	private ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		           public Thread newThread(Runnable r) {
		             Thread th = new Thread(r);
		             th.setDaemon(true);
		             return th;
		           }
		    });
	
	
	public TerrainQuad() {
		super("Terrain");
	}
	
	public TerrainQuad(String name, int blockSize, int size, float[] heightMap) {
		this(name, blockSize, size, Vector3f.UNIT_XYZ, heightMap, size, new Vector2f(), 0, null);
	}
	
	public TerrainQuad(String name, int blockSize, int size, Vector3f scale, float[] heightMap, LodCalculatorFactory lodCalculatorFactory) {
		this(name, blockSize, size, scale, heightMap, size, new Vector2f(), 0, lodCalculatorFactory);
	}
	
	protected TerrainQuad(String name, int blockSize, int size,
				Vector3f stepScale, float[] heightMap, int totalSize,
				Vector2f offset, float offsetAmount,
				LodCalculatorFactory lodCalculatorFactory)
	{
		super(name);
		if (!FastMath.isPowerOfTwo(size - 1)) {
			throw new RuntimeException("size given: " + size + "  Terrain quad sizes may only be (2^N + 1)");
		}
		
		if (heightMap == null)
			heightMap = generateDefaultHeightMap(size);

		this.offset = offset;
		this.offsetAmount = offsetAmount;
		this.totalSize = totalSize;
		this.size = size;
		this.stepScale = stepScale;
		this.lodCalculatorFactory = lodCalculatorFactory;
		split(blockSize, heightMap);
		
		//fixNormals();
	}
	 
	public void setLodCalculatorFactory(LodCalculatorFactory lodCalculatorFactory) {
		if (children != null) {
			for (int i = children.size(); --i >= 0;) {
				Spatial child = children.get(i);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).setLodCalculatorFactory(lodCalculatorFactory);
				} else if (child instanceof TerrainPatch) {
					((TerrainPatch) child).setLodCalculator(lodCalculatorFactory.createCalculator((TerrainPatch) child));
				}
			}
		}
	}
	 
	
	/**
	 * Create just a flat heightmap
	 */
	private float[] generateDefaultHeightMap(int size) {
		float[] heightMap = new float[size*size];
		return heightMap;
	}

	 /**
	  * Call from the update() method of your gamestate to update
	  * the LOD values of each patch.
	  * This will perform the geometry calculation in a background thread and 
	  * do the actual update on the opengl thread.
	  */
	public void update(List<Vector3f> locations) {
		
		// update any existing ones that need updating
		updateQuadLODs();
		
		if (lastCameraLocations != null) {
			if (lastCameraLocationsTheSame(locations))
				return; // don't update if in same spot
			else
				lastCameraLocations = cloneVectorList(locations);
		}
		else {
			lastCameraLocations = cloneVectorList(locations);
			return;
		}
		
		if (isLodCalcRunning()) {
			return;
		}
		
		if (getParent() instanceof TerrainQuad) {
			return; // we just want the root quad to perform this.
		}
		
		UpdateLOD updateLodThread = new UpdateLOD(locations);
		executor.execute(updateLodThread);
		
	}
	
	private synchronized boolean isLodCalcRunning() {
		return lodCalcRunning;
	}
	
	private synchronized void setLodCalcRunning(boolean running) {
		lodCalcRunning = running;
	}

    private List<Vector3f> cloneVectorList(List<Vector3f> locations) {
            List<Vector3f> cloned = new ArrayList<Vector3f>();
            for(Vector3f l : locations)
                cloned.add(l.clone());
            return cloned;
    }

    private boolean lastCameraLocationsTheSame(List<Vector3f> locations) {
            boolean theSame = true;
            for (Vector3f l : locations) {
                for (Vector3f v : lastCameraLocations) {
                    if (!v.equals(l) ) {
                        theSame = false;
                        return false;
                    }
                }
            }
            return theSame;
    }

    private int collideWithRay(Ray ray, CollisionResults results) {
        if (picker == null)
            picker = new BresenhamTerrainPicker(this);

        Vector3f intersection = picker.getTerrainIntersection(ray, results);
        if (intersection != null)
            return 1;
        else
            return 0;
    }
	
	/**
	 * Calculates the LOD of all child terrain patches.
	 */
	private class UpdateLOD implements Runnable {
		private List<Vector3f> camLocations;
		
		UpdateLOD(List<Vector3f> location) {
			camLocations = location;
		}
		
		public void run() {
			long start = System.currentTimeMillis();
			if (isLodCalcRunning()) {
				//System.out.println("thread already running");
				return;
			}
			//System.out.println("spawned thread "+toString());
			setLodCalcRunning(true);
			
			// go through each patch and calculate its LOD based on camera distance
			HashMap<String,UpdatedTerrainPatch> updated = new HashMap<String,UpdatedTerrainPatch>();
			boolean lodChanged = calculateLod(camLocations, updated); // 'updated' gets populated here
			
			if (!lodChanged) {
				// not worth updating anything else since no one's LOD changed
				setLodCalcRunning(false);
				return;
			}
			// then calculate its neighbour LOD values for seaming in the shader
			findNeighboursLod(updated);
			
			fixEdges(updated); // 'updated' can get added to here
			
			reIndexPages(updated);
			
			setUpdateQuadLODs(updated); // set back to main ogl thread
			
			setLodCalcRunning(false);
			//double duration = (System.currentTimeMillis()-start);
			//System.out.println("terminated in "+duration);
		}

		
		
	}
	
	private void setUpdateQuadLODs(HashMap<String,UpdatedTerrainPatch> updated) {
		synchronized (updatePatchesLock) {
			updatedPatches = updated;
		}
	}
	
	/**
	 * Back on the ogl thread: update the terrain patch geometries
	 * @param updatedPatches to be updated
	 */
	private void updateQuadLODs() {
		synchronized (updatePatchesLock) {
			//if (true)
			//	return;
			if (updatedPatches == null || updatedPatches.size() == 0)
				return;
			
			//TODO do the actual geometry update here
			for (UpdatedTerrainPatch utp : updatedPatches.values()) {
				utp.updateAll();
			}
			
			updatedPatches.clear();
		}
	}
	
	protected boolean calculateLod(List<Vector3f> location, HashMap<String,UpdatedTerrainPatch> updates) {
		
		boolean lodChanged = false;
		
		if (children != null) {
			for (int i = children.size(); --i >= 0;) {
				Spatial child = children.get(i);
				if (child instanceof TerrainQuad) {
					boolean b = ((TerrainQuad) child).calculateLod(location, updates);
					if (b)
						lodChanged = true;
				} else if (child instanceof TerrainPatch) {
					boolean b = ((TerrainPatch) child).calculateLod(location, updates);
					if (b)
						lodChanged = true;
				}
			}
		}
		
		return lodChanged;
	}
	
	protected synchronized void findNeighboursLod(HashMap<String,UpdatedTerrainPatch> updated) {
		if (children != null) {
			for (int x = children.size(); --x >= 0;) {
				Spatial child = children.get(x);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).findNeighboursLod(updated);
				} else if (child instanceof TerrainPatch) {

					TerrainPatch patch = (TerrainPatch) child;
					if (!patch.searchedForNeighboursAlready) {
						// set the references to the neighbours
						patch.rightNeighbour = findRightPatch(patch);
						patch.bottomNeighbour = findDownPatch(patch);
						patch.leftNeighbour = findLeftPatch(patch);
						patch.topNeighbour = findTopPatch(patch);
						patch.searchedForNeighboursAlready = true;
					}
					TerrainPatch right = patch.rightNeighbour;
					TerrainPatch down = patch.bottomNeighbour;
					
					UpdatedTerrainPatch utp = updated.get(patch.getName());
					if (utp == null) {
						utp = new UpdatedTerrainPatch(patch, patch.lod);
						updated.put(utp.getName(), utp);
					}
					
					if (right != null) {
						UpdatedTerrainPatch utpR = updated.get(right.getName());
						if (utpR == null) {
							utpR = new UpdatedTerrainPatch(right, right.lod);
							updated.put(utpR.getName(), utpR);
						}
						
						utp.setRightLod(utpR.getNewLod());
						utpR.setLeftLod(utp.getNewLod());
					}
					if (down != null) {
						UpdatedTerrainPatch utpD = updated.get(down.getName());
						if (utpD == null) {
							utpD = new UpdatedTerrainPatch(down, down.lod);
							updated.put(utpD.getName(), utpD);
						}
						
						utp.setBottomLod(utpD.getNewLod());
						utpD.setTopLod(utp.getNewLod());
					}
					
				}
			}
		}
	}
	
	/**
	 * Find any neighbours that should have their edges seamed because another neighbour
	 * changed its LOD to a greater value (less detailed)
	 */
	protected synchronized void fixEdges(HashMap<String,UpdatedTerrainPatch> updated) {
		if (children != null) {
			for (int x = children.size(); --x >= 0;) {
				Spatial child = children.get(x);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).fixEdges(updated);
				} else if (child instanceof TerrainPatch) {
					TerrainPatch patch = (TerrainPatch) child;
					UpdatedTerrainPatch utp = updated.get(patch.getName());
					
					if(utp.lodChanged()) {
						if (!patch.searchedForNeighboursAlready) {
							// set the references to the neighbours
							patch.rightNeighbour = findRightPatch(patch);
							patch.bottomNeighbour = findDownPatch(patch);
							patch.leftNeighbour = findLeftPatch(patch);
							patch.topNeighbour = findTopPatch(patch);
							patch.searchedForNeighboursAlready = true;
						}
						TerrainPatch right = patch.rightNeighbour;
						TerrainPatch down = patch.bottomNeighbour;
						TerrainPatch top = patch.topNeighbour;
						TerrainPatch left = patch.leftNeighbour;
						if (right != null) {
							UpdatedTerrainPatch utpR = updated.get(right.getName());
							if (utpR == null) {
								utpR = new UpdatedTerrainPatch(right, right.lod);
								updated.put(utpR.getName(), utpR);
							}
							utpR.setFixEdges(true);
						}
						if (down != null) {
							UpdatedTerrainPatch utpD = updated.get(down.getName());
							if (utpD == null) {
								utpD = new UpdatedTerrainPatch(down, down.lod);
								updated.put(utpD.getName(), utpD);
							}
							utpD.setFixEdges(true);
						}
						if (top != null){
							UpdatedTerrainPatch utpT = updated.get(top.getName());
							if (utpT == null) {
								utpT = new UpdatedTerrainPatch(top, top.lod);
								updated.put(utpT.getName(), utpT);
							}
							utpT.setFixEdges(true);
						}
						if (left != null){
							UpdatedTerrainPatch utpL = updated.get(left.getName());
							if (utpL == null) {
								utpL = new UpdatedTerrainPatch(left, left.lod);
								updated.put(utpL.getName(), utpL);
							}
							utpL.setFixEdges(true);
						}
					}
				}
			}
		}
	}
	
	protected synchronized void reIndexPages(HashMap<String,UpdatedTerrainPatch> updated) {
		if (children != null) {
			for (int i = children.size(); --i >= 0;) {
				Spatial child = children.get(i);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).reIndexPages(updated);
				} else if (child instanceof TerrainPatch) {
					((TerrainPatch) child).reIndexGeometry(updated);
				}
			}
		}
	}

	/**
	 * <code>split</code> divides the heightmap data for four children. The
	 * children are either pages or blocks. This is dependent on the size of the
	 * children. If the child's size is less than or equal to the set block
	 * size, then blocks are created, otherwise, pages are created.
	 * 
	 * @param blockSize
	 *			the blocks size to test against.
	 * @param heightMap
	 *			the height data.
	 */
	protected void split(int blockSize, float[] heightMap) {
		if ((size >> 1) + 1 <= blockSize) {
			createQuadPatch(heightMap);
		} else {
			createQuad(blockSize, heightMap);
		}

	}

	/**
	 * <code>createQuadPage</code> generates four new pages from this page.
	 */
	protected void createQuad(int blockSize, float[] heightMap) {
		// create 4 terrain pages
		int quarterSize = size >> 2;

		int split = (size + 1) >> 1;

		Vector2f tempOffset = new Vector2f();
		offsetAmount += quarterSize;

		if (lodCalculatorFactory == null)
			lodCalculatorFactory = new LodDistanceCalculatorFactory(); // set a default one

		if (getParent() == null || !(getParent() instanceof TerrainQuad))
			this.heightMap = heightMap; // save the overall heightmap for the root quad only

		// 1 upper left
		float[] heightBlock1 = createHeightSubBlock(heightMap, 0, 0, split);

		Vector3f origin1 = new Vector3f(-quarterSize * stepScale.x, 0,
				-quarterSize * stepScale.z);

		tempOffset.x = offset.x;
		tempOffset.y = offset.y;
		tempOffset.x += origin1.x;
		tempOffset.y += origin1.z;

		TerrainQuad page1 = new TerrainQuad(getName() + "Quad1", blockSize,
				split, stepScale, heightBlock1, totalSize, tempOffset,
				offsetAmount, lodCalculatorFactory);
		page1.setLocalTranslation(origin1);
		page1.quadrant = 1;
		this.attachChild(page1);

		// 2 lower left
		float[] heightBlock2 = createHeightSubBlock(heightMap, 0, split - 1,
				split);

		Vector3f origin2 = new Vector3f(-quarterSize * stepScale.x, 0,
				quarterSize * stepScale.z);

		tempOffset = new Vector2f();
		tempOffset.x = offset.x;
		tempOffset.y = offset.y;
		tempOffset.x += origin2.x;
		tempOffset.y += origin2.z;

		TerrainQuad page2 = new TerrainQuad(getName() + "Quad2", blockSize,
				split, stepScale, heightBlock2, totalSize, tempOffset,
				offsetAmount, lodCalculatorFactory);
		page2.setLocalTranslation(origin2);
		page2.quadrant = 2;
		this.attachChild(page2);

		// 3 upper right
		float[] heightBlock3 = createHeightSubBlock(heightMap, split - 1, 0,
				split);

		Vector3f origin3 = new Vector3f(quarterSize * stepScale.x, 0,
				-quarterSize * stepScale.z);

		tempOffset = new Vector2f();
		tempOffset.x = offset.x;
		tempOffset.y = offset.y;
		tempOffset.x += origin3.x;
		tempOffset.y += origin3.z;

		TerrainQuad page3 = new TerrainQuad(getName() + "Quad3", blockSize,
				split, stepScale, heightBlock3, totalSize, tempOffset,
				offsetAmount, lodCalculatorFactory);
		page3.setLocalTranslation(origin3);
		page3.quadrant = 3;
		this.attachChild(page3);
		// //
		// 4 lower right
		float[] heightBlock4 = createHeightSubBlock(heightMap, split - 1,
				split - 1, split);

		Vector3f origin4 = new Vector3f(quarterSize * stepScale.x, 0,
				quarterSize * stepScale.z);

		tempOffset = new Vector2f();
		tempOffset.x = offset.x;
		tempOffset.y = offset.y;
		tempOffset.x += origin4.x;
		tempOffset.y += origin4.z;

		TerrainQuad page4 = new TerrainQuad(getName() + "Quad4", blockSize,
				split, stepScale, heightBlock4, totalSize, tempOffset,
				offsetAmount, lodCalculatorFactory);
		page4.setLocalTranslation(origin4);
		page4.quadrant = 4;
		this.attachChild(page4);

	}

	/**
	 * <code>createQuadBlock</code> creates four child blocks from this page.
	 */
	protected void createQuadPatch(float[] heightMap) {
		// create 4 terrain blocks
		int quarterSize = size >> 2;
		int halfSize = size >> 1;
		int split = (size + 1) >> 1;

		if (lodCalculatorFactory == null)
			lodCalculatorFactory = new LodDistanceCalculatorFactory(); // set a default one

		offsetAmount += quarterSize;

		// 1 upper left
		float[] heightBlock1 = createHeightSubBlock(heightMap, 0, 0, split);

		Vector3f origin1 = new Vector3f(-halfSize * stepScale.x, 0, -halfSize
				* stepScale.z);

		Vector2f tempOffset1 = new Vector2f();
		tempOffset1.x = offset.x;
		tempOffset1.y = offset.y;
		tempOffset1.x += origin1.x / 2;
		tempOffset1.y += origin1.z / 2;

		TerrainPatch patch1 = new TerrainPatch(getName() + "Patch1", split,
				stepScale, heightBlock1, origin1, totalSize, tempOffset1,
				offsetAmount);
		patch1.setQuadrant((short) 1);
		this.attachChild(patch1);
		patch1.setModelBound(new BoundingBox());
		patch1.updateModelBound();
		patch1.setLodCalculator(lodCalculatorFactory.createCalculator(patch1));

		// 2 lower left
		float[] heightBlock2 = createHeightSubBlock(heightMap, 0, split - 1,
				split);

		Vector3f origin2 = new Vector3f(-halfSize * stepScale.x, 0, 0);

		Vector2f tempOffset2 = new Vector2f();
		tempOffset2.x = offset.x;
		tempOffset2.y = offset.y;
		tempOffset2.x += origin1.x / 2;
		tempOffset2.y += quarterSize * stepScale.z;

		TerrainPatch patch2 = new TerrainPatch(getName() + "Patch2", split,
				stepScale, heightBlock2, origin2, totalSize, tempOffset2,
				offsetAmount);
		patch2.setQuadrant((short) 2);
		this.attachChild(patch2);
		patch2.setModelBound(new BoundingBox());
		patch2.updateModelBound();
		patch2.setLodCalculator(lodCalculatorFactory.createCalculator(patch2));

		// 3 upper right
		float[] heightBlock3 = createHeightSubBlock(heightMap, split - 1, 0,
				split);

		Vector3f origin3 = new Vector3f(0, 0, -halfSize * stepScale.z);

		Vector2f tempOffset3 = new Vector2f();
		tempOffset3.x = offset.x;
		tempOffset3.y = offset.y;
		tempOffset3.x += quarterSize * stepScale.x;
		tempOffset3.y += origin3.z / 2;

		TerrainPatch patch3 = new TerrainPatch(getName() + "Patch3", split,
				stepScale, heightBlock3, origin3, totalSize, tempOffset3,
				offsetAmount);
		patch3.setQuadrant((short) 3);
		this.attachChild(patch3);
		patch3.setModelBound(new BoundingBox());
		patch3.updateModelBound();
		patch3.setLodCalculator(lodCalculatorFactory.createCalculator(patch3));

		// 4 lower right
		float[] heightBlock4 = createHeightSubBlock(heightMap, split - 1,
				split - 1, split);

		Vector3f origin4 = new Vector3f(0, 0, 0);

		Vector2f tempOffset4 = new Vector2f();
		tempOffset4.x = offset.x;
		tempOffset4.y = offset.y;
		tempOffset4.x += quarterSize * stepScale.x;
		tempOffset4.y += quarterSize * stepScale.z;

		TerrainPatch patch4 = new TerrainPatch(getName() + "Patch4", split,
				stepScale, heightBlock4, origin4, totalSize, tempOffset4,
				offsetAmount);
		patch4.setQuadrant((short) 4);
		this.attachChild(patch4);
		patch4.setModelBound(new BoundingBox());
		patch4.updateModelBound();
		patch4.setLodCalculator(lodCalculatorFactory.createCalculator(patch4));
	}
	
	public float[] createHeightSubBlock(float[] heightMap, int x,
			int y, int side) {
		float[] rVal = new float[side * side];
		int bsize = (int) FastMath.sqrt(heightMap.length);
		int count = 0;
		for (int i = y; i < side + y; i++) {
			for (int j = x; j < side + x; j++) {
				if (j < bsize && i < bsize)
					rVal[count] = heightMap[j + (i * bsize)];
				count++;
			}
		}
		return rVal;
	}

    /**
     * A handy method that will attach all bounding boxes of this terrain
     * to the node you supply.
     * Useful to visualize the bounding boxes when debugging.
     * 
     * @param parent that will get the bounding box shapes of the terrain attached to
     */
    public void attachBoundChildren(Node parent) {
        for (int i = 0; i < this.getQuantity(); i++) {
			if (this.getChild(i) instanceof TerrainQuad) {
				((TerrainQuad) getChild(i)).attachBoundChildren(parent);
			} else if (this.getChild(i) instanceof TerrainPatch) {
				BoundingVolume bv = getChild(i).getWorldBound();
                if (bv instanceof BoundingBox) {
                    attachBoundingBox((BoundingBox)bv, parent);
                }
			}
		}
        BoundingVolume bv = getWorldBound();
        if (bv instanceof BoundingBox) {
            attachBoundingBox((BoundingBox)bv, parent);
        }
    }

    /**
     * used by attachBoundChildren()
     */
    private void attachBoundingBox(BoundingBox bb, Node parent) {
        WireBox wb = new WireBox(bb.getXExtent(), bb.getYExtent(), bb.getZExtent());
        Geometry g = new Geometry();
        g.setMesh(wb);
        g.setLocalTranslation(bb.getCenter());
        parent.attachChild(g);
    }
	 
/*    @Override
	public void setModelBound(BoundingVolume v) {
		for (int i = 0; i < this.getQuantity(); i++) {
			if (this.getChild(i) instanceof TerrainQuad) {
				((TerrainQuad) getChild(i)).setModelBound(v.clone(null));
			} else if (this.getChild(i) instanceof TerrainPatch) {
				((TerrainPatch) getChild(i)).setModelBound(v.clone(null));

			}
		}
	}
	 
    @Override
	public void updateModelBound() {
		for (int i = 0; i < this.getQuantity(); i++) {
			if (this.getChild(i) instanceof TerrainQuad) {
				((TerrainQuad) getChild(i)).updateModelBound();
			} else if (this.getChild(i) instanceof TerrainPatch) {
				((TerrainPatch) getChild(i)).updateModelBound();
			}
		}
	}
 */
	
	public float getHeight(float x, float z) {
		// determine which quadrant this is in.
		Spatial child = null;
		int split = (size - 1) >> 1;
		float halfmapx = split * stepScale.x, halfmapz = split * stepScale.z;
		float newX = 0, newZ = 0;
		if (x == 0)
			x += .001f;
		if (z == 0)
			z += .001f;
		if (x > 0) {
			if (z > 0) {
				// upper right
				child = getChild(3);
				newX = x;
				newZ = z;
			} else {
				// lower right
				child = getChild(2);
				newX = x;
				newZ = z + halfmapz;
			}
		} else {
			if (z > 0) {
				// upper left
				child = getChild(1);
				newX = x + halfmapx;
				newZ = z;
			} else {
				// lower left...
				child = getChild(0);
				if (x == 0)
					x -= .1f;
				if (z == 0)
					z -= .1f;
				newX = x + halfmapx;
				newZ = z + halfmapz;
			}
		}
		if (child instanceof TerrainPatch)
			return ((TerrainPatch) child).getHeight(newX, newZ);
		else if (child instanceof TerrainQuad)
			return ((TerrainQuad) child).getHeight(x
					- ((TerrainQuad) child).getLocalTranslation().x, z
					- ((TerrainQuad) child).getLocalTranslation().z);
		return Float.NaN;
	}
	
	
	public int getQuadrant() {
		return quadrant;
	}

	public void setQuadrant(short quadrant) {
		this.quadrant = quadrant;
	}
	

	protected TerrainPatch getPatch(int quad) {
		if (children != null)
			for (int x = children.size(); --x >= 0;) {
				Spatial child = children.get(x);
				if (child instanceof TerrainPatch) {
					TerrainPatch tb = (TerrainPatch) child;
					if (tb.getQuadrant() == quad)
						return tb;
				}
			}
		return null;
	}

	protected TerrainQuad getQuad(int quad) {
		if (children != null)
			for (int x = children.size(); --x >= 0;) {
				Spatial child = children.get(x);
				if (child instanceof TerrainQuad) {
					TerrainQuad tq = (TerrainQuad) child;
					if (tq.getQuadrant() == quad)
						return tq;
				}
			}
		return null;
	}

	protected TerrainPatch findRightPatch(TerrainPatch tp) {
		if (tp.getQuadrant() == 1)
			return getPatch(3);
		else if (tp.getQuadrant() == 2)
			return getPatch(4);
		else if (tp.getQuadrant() == 3) {
			// find the page to the right and ask it for child 1.
			TerrainQuad quad = findRightQuad();
			if (quad != null)
				return quad.getPatch(1);
		} else if (tp.getQuadrant() == 4) {
			// find the page to the right and ask it for child 2.
			TerrainQuad quad = findRightQuad();
			if (quad != null)
				return quad.getPatch(2);
		}

		return null;
	}

	protected TerrainPatch findDownPatch(TerrainPatch tp) {
		if (tp.getQuadrant() == 1)
			return getPatch(2);
		else if (tp.getQuadrant() == 3)
			return getPatch(4);
		else if (tp.getQuadrant() == 2) {
			// find the page below and ask it for child 1.
			TerrainQuad quad = findDownQuad();
			if (quad != null)
				return quad.getPatch(1);
		} else if (tp.getQuadrant() == 4) {
			TerrainQuad quad = findDownQuad();
			if (quad != null)
				return quad.getPatch(3);
		}

		return null;
	}
	
	
	protected TerrainPatch findTopPatch(TerrainPatch tp) {
		if (tp.getQuadrant() == 2)
			return getPatch(1);
		else if (tp.getQuadrant() == 4)
			return getPatch(3);
		else if (tp.getQuadrant() == 1) {
			// find the page above and ask it for child 2.
			TerrainQuad quad = findTopQuad();
			if (quad != null)
				return quad.getPatch(2);
		} else if (tp.getQuadrant() == 3) {
			TerrainQuad quad = findTopQuad();
			if (quad != null)
				return quad.getPatch(4);
		}

		return null;
	}
	
	protected TerrainPatch findLeftPatch(TerrainPatch tp) {
		if (tp.getQuadrant() == 3)
			return getPatch(1);
		else if (tp.getQuadrant() == 4)
			return getPatch(2);
		else if (tp.getQuadrant() == 1) {
			// find the page above and ask it for child 2.
			TerrainQuad quad = findLeftQuad();
			if (quad != null)
				return quad.getPatch(3);
		} else if (tp.getQuadrant() == 2) {
			TerrainQuad quad = findLeftQuad();
			if (quad != null)
				return quad.getPatch(4);
		}

		return null;
	}

	protected TerrainQuad findRightQuad() {
		if (getParent() == null || !(getParent() instanceof TerrainQuad))
			return null;

		TerrainQuad pQuad = (TerrainQuad) getParent();

		if (quadrant == 1)
			return pQuad.getQuad(3);
		else if (quadrant == 2)
			return pQuad.getQuad(4);
		else if (quadrant == 3) {
			TerrainQuad quad = pQuad.findRightQuad();
			if (quad != null)
				return quad.getQuad(1);
		} else if (quadrant == 4) {
			TerrainQuad quad = pQuad.findRightQuad();
			if (quad != null)
				return quad.getQuad(2);
		}

		return null;
	}

	protected TerrainQuad findDownQuad() {
		if (getParent() == null || !(getParent() instanceof TerrainQuad))
			return null;

		TerrainQuad pQuad = (TerrainQuad) getParent();

		if (quadrant == 1)
			return pQuad.getQuad(2);
		else if (quadrant == 3)
			return pQuad.getQuad(4);
		else if (quadrant == 2) {
			TerrainQuad quad = pQuad.findDownQuad();
			if (quad != null)
				return quad.getQuad(1);
		} else if (quadrant == 4) {
			TerrainQuad quad = pQuad.findDownQuad();
			if (quad != null)
				return quad.getQuad(3);
		}

		return null;
	}
	
	protected TerrainQuad findTopQuad() {
		if (getParent() == null || !(getParent() instanceof TerrainQuad))
			return null;

		TerrainQuad pQuad = (TerrainQuad) getParent();

		if (quadrant == 2)
			return pQuad.getQuad(1);
		else if (quadrant == 4)
			return pQuad.getQuad(3);
		else if (quadrant == 1) {
			TerrainQuad quad = pQuad.findTopQuad();
			if (quad != null)
				return quad.getQuad(2);
		} else if (quadrant == 3) {
			TerrainQuad quad = pQuad.findTopQuad();
			if (quad != null)
				return quad.getQuad(4);
		}

		return null;
	}
	
	protected TerrainQuad findLeftQuad() {
		if (getParent() == null || !(getParent() instanceof TerrainQuad))
			return null;

		TerrainQuad pQuad = (TerrainQuad) getParent();

		if (quadrant == 3)
			return pQuad.getQuad(1);
		else if (quadrant == 4)
			return pQuad.getQuad(2);
		else if (quadrant == 1) {
			TerrainQuad quad = pQuad.findLeftQuad();
			if (quad != null)
				return quad.getQuad(3);
		} else if (quadrant == 2) {
			TerrainQuad quad = pQuad.findLeftQuad();
			if (quad != null)
				return quad.getQuad(4);
		}

		return null;
	}
	
	/**
	 * Ignoring the normals for now. The lighting just makes the terrain "pop" noticeably.
	 * Use a lightmap instead.
	 */
	public void fixNormals() {
	/*	if (children != null) {
			for (int x = children.size(); --x >= 0;) {
				Spatial child = children.get(x);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).fixNormals();
				} else if (child instanceof TerrainPatch) {
					TerrainPatch tb = (TerrainPatch) child;
					TerrainPatch right = findRightPatch(tb);
					TerrainPatch down = findDownPatch(tb);
					int tbSize = tb.getSize();
					if (right != null) {
						float[] normData = new float[3];
						for (int y = 0; y < tbSize; y++) {
							int index1 = ((y + 1) * tbSize) - 1;
							int index2 = (y * tbSize);
							right.getNormalBuffer().position(index2 * 3);
							right.getNormalBuffer().get(normData);
							tb.getNormalBuffer().position(index1 * 3);
							tb.getNormalBuffer().put(normData);
						}
						deleteNormalVBO(right);

					}
					if (down != null) {
						int rowStart = ((tbSize - 1) * tbSize);
						float[] normData = new float[3];
						for (int z = 0; z < tbSize; z++) {
							int index1 = rowStart + z;
							int index2 = z;
							down.getNormalBuffer().position(index2 * 3);
							down.getNormalBuffer().get(normData);
							tb.getNormalBuffer().position(index1 * 3);
							tb.getNormalBuffer().put(normData);
						}
						deleteNormalVBO(down);
					}
					deleteNormalVBO(tb);
				}
			}
		}
		*/
	}

    @Override
    public int collideWith(Collidable other, CollisionResults results){
        int total = 0;

        if (other instanceof Ray)
            return collideWithRay((Ray)other, results);

        // if it didn't collide with this bbox, return
        if (other.collideWith(this.getWorldBound(), results) == 0)
            return total;

        for (Spatial child : children){
            total += child.collideWith(other, results);
        }
        return total;
    }
	
    /**
     * Gather the terrain patches that intersect the given ray (toTest).
     * This only tests the bounding boxes
     * @param toTest
     * @param results
     */
    public void findPick(Ray toTest, List<TerrainPickData> results) {
        
        if (getWorldBound() != null) {
            if (getWorldBound().intersects(toTest)) {
                // further checking needed.
                for (int i = 0; i < getQuantity(); i++) {
                    if (children.get(i) instanceof TerrainPatch) {
                        TerrainPatch tp = (TerrainPatch) children.get(i);
                        if (tp.getWorldBound().intersects(toTest)) {
                            CollisionResults cr = new CollisionResults();
                            toTest.collideWith(tp.getWorldBound(), cr);
                            cr.getClosestCollision().getDistance();
                            results.add(new TerrainPickData(tp, cr.getClosestCollision()));
                        }
                    }
                    else
                        ((TerrainQuad) children.get(i)).findPick(toTest, results);
                }
            }
        }
    }


	/**
	 * Retrieve all Terrain Patches from all children and store them
	 * in the 'holder' list
	 * @param holder must not be null, will be populated when returns
	 */
	public void getAllTerrainPatches(List<TerrainPatch> holder) {
		if (children != null) {
			for (int i = children.size(); --i >= 0;) {
				Spatial child = children.get(i);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).getAllTerrainPatches(holder);
				} else if (child instanceof TerrainPatch) {
					holder.add((TerrainPatch)child);
				}
			}
		}
	}

	public void getAllTerrainPatchesWithTranslation(Map<TerrainPatch,Vector3f> holder, Vector3f translation) {
		if (children != null) {
			for (int i = children.size(); --i >= 0;) {
				Spatial child = children.get(i);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).getAllTerrainPatchesWithTranslation(holder, translation.clone().add(child.getLocalTranslation()));
				} else if (child instanceof TerrainPatch) {
					//if (holder.size() < 4)
					holder.put((TerrainPatch)child, translation.clone().add(child.getLocalTranslation()));
				}
			}
		}
	}
	
	@Override
	public void read(JmeImporter e) throws IOException {
		super.read(e);
		InputCapsule c = e.getCapsule(this);
		size = c.readInt("size", 0);
		stepScale = (Vector3f) c.readSavable("stepScale", null);
		offset = (Vector2f) c.readSavable("offset", new Vector2f(0,0));
		offsetAmount = c.readFloat("offsetAmount", 0);
		quadrant = c.readInt("quadrant", 0);
		totalSize = c.readInt("totalSize", 0);
		lodCalculatorFactory = (LodCalculatorFactory) c.readSavable("lodCalculatorFactory", null);
        heightMap = c.readFloatArray("heightMap", heightMap);
        TerrainLodControl lodControl = getControl(TerrainLodControl.class);
        if (lodControl != null && !(getParent() instanceof TerrainQuad))
            lodControl.setTerrain(this);
	}

	@Override
	public void write(JmeExporter e) throws IOException {
		super.write(e);
		OutputCapsule c = e.getCapsule(this);
		c.write(size, "size", 0);
        c.write(totalSize, "totalSize", 0);
		c.write(stepScale, "stepScale", null);
		c.write(offset, "offset", new Vector2f(0,0));
		c.write(offsetAmount, "offsetAmount", 0);
		c.write(quadrant, "quadrant", 0);
        c.write(lodCalculatorFactory, "lodCalculatorFactory", null);
        c.write(heightMap, "heightMap", null);
	}
	
	@Override
        public Node clone(){
		//TODO use importer and exporter to clone it
		return null;
	}


	public int getMaxLod() {
		if (maxLod < 0)
			maxLod = Math.max(1, (int) (FastMath.log(size-1)/FastMath.log(2)) -1); // -1 forces our minimum of 4 triangles wide

		return maxLod;
	}

	public void useLOD(boolean useLod) {
		usingLOD = useLod;
	}

	public boolean isUsingLOD() {
		return usingLOD;
	}

	public void setHeight(Vector2f xzCoordinate, float height) {
		// TODO Auto-generated method stub

	}

	public float getHeight(Vector2f xz) {
		// TODO Auto-generated method stub
		return 0;
	}

	public float[] getHeightMap() {
		return heightMap;
	}
}

