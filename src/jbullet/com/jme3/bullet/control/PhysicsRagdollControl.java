package com.jme3.bullet.control;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.joints.PhysicsConeJoint;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import com.jme3.util.TempVars;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PhysicsRagdollControl implements Control {

    private static final Logger logger = Logger.getLogger(PhysicsRagdollControl.class.getName());
    private List<PhysicsBoneLink> boneLinks;
    private Skeleton skeleton;
    private PhysicsSpace space;
    protected boolean enabled = false;
    //TODO:remove after testing
    private AssetManager manager = new DesktopAssetManager(true);
    private Camera offCamera = new Camera(512, 512);
    private Picture debugPic;
    private Node boneRoot = new Node("ragdolldebugnode");
    protected boolean physicsActive = true;

    public PhysicsRagdollControl(PhysicsSpace space) {
        this.space = space;
    }

    private List<PhysicsBoneLink> boneRecursion(Bone bone, PhysicsNode parent, List<PhysicsBoneLink> list , int reccount) {
        ArrayList<Bone> children = bone.getChildren();
        bone.setUserControl(true);
        for (Iterator<Bone> it = children.iterator(); it.hasNext();) {
            Bone childBone = it.next();
            Bone parentBone = bone;
            Vector3f parentPos = parentBone.getModelSpacePosition();
            Vector3f childPos = childBone.getModelSpacePosition();
            //get location between the two bones (physicscapsule center)
            Vector3f jointCenter = parentPos.add(childPos).multLocal(0.5f);
//            Quaternion jointRotation = parentBone.getModelSpaceRotation();
            Quaternion jointRotation = new Quaternion();
            jointRotation.lookAt(childPos.subtract(parentPos), Vector3f.UNIT_Y);
//            jointRotation.lookAt(Vector3f.UNIT_Z, childPos.subtract(parentPos));
            // length of the joint
            float height = parentPos.distance(childPos);

            // TODO: joints act funny when bone is too thin??
            CapsuleCollisionShape shape = new CapsuleCollisionShape(height*.18f, height*0.5f,2);
//            BoxCollisionShape shape = new BoxCollisionShape(new Vector3f(height * 0.2f, height * 0.2f, height * 0.5f));
//            CylinderCollisionShape shape = new CylinderCollisionShape(new Vector3f(height * .2f, height * .2f, height * 0.5f),2);

            PhysicsNode shapeNode = new PhysicsNode(shape, 10.0f/(float)reccount);
            shapeNode.setLocalTranslation(jointCenter);
            shapeNode.setLocalRotation(jointRotation);
            //TODO: only called to sync physics location with jme location
            shapeNode.updateGeometricState();
            shapeNode.attachDebugShape(manager);
//            shapeNode.getChild(0).setLocalScale(1, 1, 5);
            boneRoot.attachChild(shapeNode);

            if (physicsActive) {
                space.addQueued(shapeNode);
            }

            PhysicsBoneLink link = new PhysicsBoneLink();
            link.parentBone = parentBone;
            link.childBone = childBone;
            link.shapeNode = shapeNode;
            link.length = height;

            //TODO: ragdoll mass 1
            if (parent != null) {
                //get length of parent
                float parentHeight = 0.0f;
                if (bone.getParent() != null) {
                    parentHeight = bone.getParent().getLocalPosition().distance(parentPos);
                }
                //local position from parent
                link.pivotA = new Vector3f(0, 0, (parentHeight * .5f));
                //local position from child
                link.pivotB = new Vector3f(0, 0, -(height * .5f));

                PhysicsConeJoint joint = new PhysicsConeJoint(parent, shapeNode, link.pivotA, link.pivotB);
                joint.setLimit(FastMath.HALF_PI, FastMath.HALF_PI, 0.01f);
//                PhysicsPoint2PointJoint joint=new PhysicsPoint2PointJoint(parent, shapeNode, link.pivotA, link.pivotB);
//                Physics6DofJoint joint = new Physics6DofJoint(parent, shapeNode, link.pivotA, link.pivotB, true);
//                joint.getRotationalLimitMotor(0).setHiLimit(0.1f);
//                joint.getRotationalLimitMotor(0).setLoLimit(-0.1f);
//                joint.getTranslationalLimitMotor().setUpperLimit(Vector3f.ZERO);
//                joint.getTranslationalLimitMotor().setLowerLimit(Vector3f.ZERO);

                link.joint = joint;
                joint.setCollisionBetweenLinkedBodys(false);
                if (physicsActive) {
                    space.addQueued(joint);
                }
            }
            list.add(link);
            boneRecursion(childBone, shapeNode, list, reccount++);
        }
        return list;
    }

    public void update(float tpf) {
        if (!enabled) {
            return;
        }
        boneRoot.updateGeometricState();
        TempVars vars = TempVars.get();
        assert vars.lock();

        skeleton.reset();
        for (PhysicsBoneLink link : boneLinks) {
//            link.shapeNode.updateGeometricState();
            Vector3f p = link.shapeNode.getWorldTranslation();
            Quaternion q = link.shapeNode.getWorldRotation();

            q.toAxes(vars.tri);

            Vector3f dir = vars.tri[2];
            float len = link.length;

            Vector3f parentPos = new Vector3f(p).subtractLocal(dir.mult(len / 2f));
            Vector3f childPos = new Vector3f(p).addLocal(dir.mult(len / 2f));

            Quaternion q2 = q.clone();
            Quaternion rot = new Quaternion();
            rot.fromAngles(FastMath.HALF_PI, 0, 0);
            q2.multLocal(rot);
            q2.normalize();

            link.parentBone.setUserTransformsWorld(parentPos, q2);
            if (link.childBone.getChildren().size() == 0) {
                link.childBone.setUserTransformsWorld(childPos, q2.clone());
            }
        }

        assert vars.unlock();
//        skeleton.updateWorldVectors();
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSpatial(Spatial model) {
        space.removeAll(boneRoot);
        enabled = true;
        //TODO: cleanup when adding new
        AnimControl animControl = model.getControl(AnimControl.class);
        skeleton = animControl.getSkeleton();

        // put into bind pose and compute bone transforms in model space
        // maybe dont reset to ragdoll out of animations?
        skeleton.resetAndUpdate();

        logger.log(Level.INFO, "Create physics ragdoll for skeleton {0}", skeleton);
        List<PhysicsBoneLink> list = new LinkedList<PhysicsBoneLink>();
        for (int i = 0; i < skeleton.getBoneCount(); i++) {
            Bone childBone = skeleton.getBone(i);
            childBone.setUserControl(true);
            if (childBone.getParent() == null) {
                Vector3f parentPos = childBone.getModelSpacePosition();
                logger.log(Level.INFO, "Found root bone in skeleton {0}", skeleton);
                PhysicsNode shapeNode = new PhysicsNode(new BoxCollisionShape(Vector3f.UNIT_XYZ.multLocal(.1f)), 1);
                shapeNode.setLocalTranslation(parentPos);
                shapeNode.updateGeometricState();
                boneRoot.attachChild(shapeNode);
                if (physicsActive) {
                    space.addQueued(shapeNode);
                }
                boneLinks = boneRecursion(childBone, shapeNode, list,1);
                return;
            }

        }
    }

    public void setEnabled(boolean enabled) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isEnabled() {
        return true;
    }

    public void render(RenderManager rm, ViewPort vp) {
        if (debugPic != null) {
            Camera cam = vp.getCamera();
            offCamera.setLocation(cam.getLocation());
            offCamera.setRotation(cam.getRotation());
            rm.setCamera(cam, true);
            int h = cam.getHeight();
            int w = cam.getWidth();
            debugPic.setPosition(w - 256, h / 20f);
            debugPic.setWidth(256);
            debugPic.setHeight(256);
            debugPic.updateGeometricState();
            rm.renderGeometry(debugPic);
            rm.setCamera(cam, false);
        }
    }

    public void write(JmeExporter ex) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void read(JmeImporter im) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void createDebugView(RenderManager renderManager, AssetManager assetManager) {

        // create a pre-view. a view that is rendered before the main view
        ViewPort offView = renderManager.createPreView("Offscreen View", offCamera);
        offView.setClearEnabled(true);
        offView.setBackgroundColor(ColorRGBA.DarkGray);

        // create offscreen framebuffer
        FrameBuffer offBuffer = new FrameBuffer(512, 512, 0);

        //setup framebuffer's cam
        offCamera.setFrustumPerspective(45f, 1f, 1f, 1000f);
        offCamera.setLocation(new Vector3f(0f, 0f, -5f));
        offCamera.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        //setup framebuffer's texture
        Texture2D offTex = new Texture2D(512, 512, Format.RGB8);

        //setup framebuffer to use texture
        offBuffer.setDepthBuffer(Format.Depth);
        offBuffer.setColorTexture(offTex);

        //set viewport to render to offscreen framebuffer
        offView.setOutputFrameBuffer(offBuffer);

        // attach the scene to the viewport to be rendered
        offView.attachScene(boneRoot);

        debugPic = new Picture("pic");
        debugPic.setTexture(assetManager, offTex, false);
    }

    /**
     * @param physicsActive the physicsActive to set
     */
    public void setPhysicsActive(boolean physicsActive) {
        this.physicsActive = physicsActive;
    }

    private static class PhysicsBoneLink {

        Bone childBone;
        Bone parentBone;
        PhysicsJoint joint;
        PhysicsNode shapeNode;
        Vector3f pivotA;
        Vector3f pivotB;
        float length;
    }
}
