package com.jme3.scene;

import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.material.Material;
import com.jme3.math.Matrix4f;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.TempVars;
import java.io.IOException;

public class Geometry extends Spatial {

    /**
     * The mesh contained herein
     */
    protected Mesh mesh;

    protected int lodLevel = 0;

    protected Material material;

    /**
     * When true, the geometry's transform will not be applied.
     */
    protected boolean ignoreTransform = false;

    protected transient Matrix4f cachedWorldMat = new Matrix4f();

    /**
     * Do not use this constructor. Serialization purposes only.
     */
    public Geometry(){
    }

    /**
     * Create a geometry node without any mesh data.
     * @param name The name of this geometry
     */
    public Geometry(String name){
        super(name);
    }

    /**
     * Create a geometry node with mesh data.
     *
     * @param name The name of this geometry
     * @param mesh The mesh data for this geometry
     */
    public Geometry(String name, Mesh mesh){
        this(name);
        if (mesh == null)
            throw new NullPointerException();

        this.mesh = mesh;
    }

    /**
     * @return If ignoreTransform mode is set.
     * @see Geometry#setIgnoreTransform(boolean) 
     */
    public boolean isIgnoreTransform() {
        return ignoreTransform;
    }

    /**
     * @param ignoreTransform If true, the geometry's transform will not be applied.
     */
    public void setIgnoreTransform(boolean ignoreTransform) {
        this.ignoreTransform = ignoreTransform;
    }

    @Override
    public void setLodLevel(int lod){
        if (mesh.getNumLodLevels() == 0)
            throw new IllegalStateException("LOD levels are not set on this mesh");

        if (lod < 0 || lod >= mesh.getNumLodLevels())
            throw new IllegalArgumentException("LOD level is out of range: "+lod);

        lodLevel = lod;
    }

    public int getLodLevel(){
        return lodLevel;
    }

    public int getVertexCount(){
        return mesh.getVertexCount();
    }

    public int getTriangleCount(){
        return mesh.getTriangleCount();
    }

    public void setMesh(Mesh mesh){
        if (mesh == null)
            throw new NullPointerException();

        this.mesh = mesh;
        setBoundRefresh();
    }

    public Mesh getMesh(){
        return mesh;
    }

    @Override
    public void setMaterial(Material material){
        this.material = material;
    }

    public Material getMaterial(){
        return material;
    }

    /**
     * @return The bounding volume of the mesh, in model space.
     */
    public BoundingVolume getModelBound(){
        return mesh.getBound();
    }

    /**
     * Updates the bounding volume of the mesh. Should be called when the
     * mesh has been modified.
     */
    public void updateModelBound() {
        mesh.updateBound();
        setBoundRefresh();
    }

    /**
     * <code>updateWorldBound</code> updates the bounding volume that contains
     * this geometry. The location of the geometry is based on the location of
     * all this node's parents.
     *
     * @see com.jme.scene.Spatial#updateWorldBound()
     */
    @Override
    protected void updateWorldBound() {
        super.updateWorldBound();
        if (mesh.getBound() != null) {
            worldBound = mesh.getBound().transform(worldTransform, worldBound);
        }
    }

    @Override
    protected void updateWorldTransforms(){
        super.updateWorldTransforms();

        cachedWorldMat.loadIdentity();
        cachedWorldMat.setRotationQuaternion(worldTransform.getRotation());
        cachedWorldMat.setTranslation(worldTransform.getTranslation());

        assert TempVars.get().lock();
        Matrix4f scaleMat = TempVars.get().tempMat4;
        scaleMat.loadIdentity();
        scaleMat.scale(worldTransform.getScale());
        cachedWorldMat.multLocal(scaleMat);
        assert TempVars.get().unlock();

        // geometry requires lights to be sorted
        worldLights.sort(true);
    }

    public Matrix4f getWorldMatrix(){
        return cachedWorldMat;
    }

    @Override
    public void setModelBound(BoundingVolume modelBound) {
        this.worldBound = null;
        mesh.setBound(modelBound);
        updateModelBound();
    }

    public int collideWith(Collidable other, CollisionResults results){
        if (refreshFlags != 0)
            throw new IllegalStateException("Scene graph must be updated" +
                                            " before checking collision");

        if (mesh != null){
            // NOTE: BIHTree in mesh already checks collision with the
            // mesh's bound
            return mesh.collideWith(other, cachedWorldMat, worldBound, results);
        }
        return 0;
    }

    /**
     * This version of clone is a shallow clone, in other words, the
     * same mesh is referenced as the original geometry.
     * Exception: if the mesh is marked as being a software
     * animated mesh, (bind pose is set) then the positions
     * and normals are deep copied.
     * @return
     */
    @Override
    public Geometry clone(){
        Geometry geomClone = (Geometry) super.clone();
        geomClone.cachedWorldMat = cachedWorldMat.clone();
        if (material != null)
            geomClone.material = material.clone();
        
        if (mesh.getBuffer(Type.BindPosePosition) != null){
            geomClone.mesh = mesh.cloneForAnim();
        }
        return geomClone;
    }

    /**
     * Creates a deep clone of the geometry,
     * this creates an identical copy of the mesh
     * with the vertexbuffer data duplicated.
     * @return
     */
    @Override
    public Spatial deepClone(){
        Geometry geomClone = clone();
        geomClone.mesh = mesh.deepClone();
        return geomClone;
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(mesh, "mesh", null);
        oc.write(material, "material", null);
        oc.write(ignoreTransform, "ignoreTransform", false);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        mesh = (Mesh) ic.readSavable("mesh", null);
        material = (Material) ic.readSavable("material", null);
        ignoreTransform = ic.readBoolean("ignoreTransform", false);
    }

}
