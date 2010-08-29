package com.jme3.terrain;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public interface Geomap {

    /**
     * Returns true if this Geomap has a normalmap associated with it
     */
    public boolean hasNormalmap();

    /**
     * Returns true if the Geomap data is loaded in memory
     * If false, then the data is unavailable- must be loaded with load()
     * before the methods getHeight/getNormal can be used
     *
     * @returns wether the geomap data is loaded in system memory
     */
    public boolean isLoaded();

    /**
     * @return The maximum possible value that <code>getValue()</code> can 
     * return. Mostly depends on the source data format (byte, short, int, etc).
     */
    public int getMaximumValue();

    /**
     * Returns the height value for a given point.
     *
     * MUST return the same value as getHeight(y*getWidth()+x)
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @returns an arbitary height looked up from the heightmap
     *
     * @throws NullPointerException If isLoaded() is false
     */
    public int getValue(int x, int y);

    /**
     * Returns the height value at the given index.
     *
     * zero index is top left of map,
     * getWidth()*getHeight() index is lower right
     *
     * @param i The index
     * @returns an arbitary height looked up from the heightmap
     *
     * @throws NullPointerException If isLoaded() is false
     */
    public int getValue(int i);

    /**
     * Returns the normal at a point
     *
     * If store is null, then a new vector is returned,
     * otherwise, the result is stored in the provided vector
     * and then returned from this method
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param store A preallocated vector for storing the normal data, optional
     * @returns store, or a new vector with the normal data if store is null
     *
     * @throws NullPointerException If isLoaded() or hasNormalmap() is false
     */
    public Vector3f getNormal(int x, int y, Vector3f store);

    /**
     * Returns the normal at an index
     *
     * If store is null, then a new vector is returned,
     * otherwise, the result is stored in the provided vector
     * and then returned from this method
     *
     * See getHeight(int) for information about index lookup
     *
     * @param i the index
     * @param store A preallocated vector for storing the normal data, optional
     * @returns store, or a new vector with the normal data if store is null
     *
     * @throws NullPointerException If isLoaded() or hasNormalmap() is false
     */
    public Vector3f getNormal(int i, Vector3f store);

    public Vector2f getUV(int x, int y, Vector2f store);
    public Vector2f getUV(int i, Vector2f store);

    /**
     * Returns the width of this Geomap
     *
     * @returns the width of this Geomap
     */
    public int getWidth();

    /**
     * Returns the height of this Geomap
     *
     * @returns the height of this Geomap
     */
    public int getHeight();

    /**
     * Returns a section of this geomap as a new geomap
     *
     * The created geomap references data from this geomap
     * If part of the geomap is contained within the original,
     * then that part is returned
     */
    public SharedGeomap getSubGeomap(int x, int y, int w, int h);

    /**
     * Copies a section of this geomap as a new geomap
     */
    public Geomap copySubGeomap(int x, int y, int w, int h);

    /**
     * Creates a normal array from the normal data in this Geomap
     *
     * @param store A preallocated FloatBuffer where to store the data (optional), size must be >= getWidth()*getHeight()*3
     * @returns store, or a new FloatBuffer if store is null
     *
     * @throws NullPointerException If isLoaded() or hasNormalmap() is false
     */
    public FloatBuffer writeNormalArray(FloatBuffer store, Vector3f scale);

    /**
     * Creates a vertex array from the height data in this Geomap
     *
     * The scale argument specifies the scale to use for the vertex buffer.
     * For example, if scale is 10,1,10, then the greatest X value is getWidth()*10
     *
     * @param store A preallocated FloatBuffer where to store the data (optional), size must be >= getWidth()*getHeight()*3
     * @param scale Created vertexes are scaled by this vector
     *
     * @returns store, or a new FloatBuffer if store is null
     *
     * @throws NullPointerException If isLoaded() is false
     */
    public FloatBuffer writeVertexArray(FloatBuffer store, Vector3f scale, boolean center);

    public FloatBuffer writeTexCoordArray(FloatBuffer store, Vector2f offset, Vector2f scale);

    public IntBuffer writeIndexArray(IntBuffer store);

    public Mesh createMesh(Vector3f scale, Vector2f tcScale, boolean center);
}
