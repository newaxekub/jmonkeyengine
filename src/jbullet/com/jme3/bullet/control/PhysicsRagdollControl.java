package com.jme3.bullet.control;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.joints.Physics6DofJoint;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.util.TempVars;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3test.bullet.TestPhysicsRagdoll;

public class PhysicsRagdollControl implements Control {

    private static final Logger logger = Logger.getLogger(PhysicsRagdollControl.class.getName());
    private List<PhysicsBoneLink> boneLinks;
    private Skeleton skeleton;
    private PhysicsSpace space;
    //TODO:remove after testing
//    private TestPhysicsRagdoll root;

    public PhysicsRagdollControl(/*TestPhysicsRagdoll root, */PhysicsSpace space) {
        this.space = space;
//        this.root = root;
    }

    private List<PhysicsBoneLink> boneRecursion(Bone bone, PhysicsNode parent, List<PhysicsBoneLink> list) {
        ArrayList<Bone> children = bone.getChildren();
        bone.setUserControl(true);
        for (Iterator<Bone> it = children.iterator(); it.hasNext();) {
            Bone childBone = it.next();
            Bone parentBone = bone;
            Vector3f parentPos = parentBone.getWorldPosition();
            Vector3f childPos = childBone.getWorldPosition();
            //get location between the two bones (physicscapsule center)
            Vector3f jointCenter = parentPos.add(childPos).multLocal(0.5f);
            Quaternion jointRotation = new Quaternion();
            jointRotation.lookAt(childPos.subtract(parentPos),Vector3f.UNIT_Y );
//            jointRotation.lookAt(Vector3f.UNIT_Z, childPos.subtract(parentPos));
            // length of the joint
            float height = parentPos.distance(childPos);

            // TODO: still problems with overlapping bones..
//            CapsuleCollisionShape shape = new CapsuleCollisionShape(.2f, height*0.6f,1);
            BoxCollisionShape shape = new BoxCollisionShape(new Vector3f(.2f, 0.2f, .2f));
            PhysicsNode shapeNode = null;

            //TODO: remove cylinder when testing is over
            shapeNode = new PhysicsNode(shape, 10);
            shapeNode.setLocalTranslation(jointCenter);
            shapeNode.setLocalRotation(jointRotation);//parentBone.getWorldRotation());
            //TODO: only called to sync physics location with jme location
            shapeNode.updateGeometricState();

            //TODO: remove when testing is over, the shapeNode may not be in display tree (see update() method)
//            root.getRootNode().attachChild(shapeNode);
            space.addQueued(shapeNode);

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
                link.pivotA = new Vector3f(0,0,  (parentHeight * .5f));
                //local position from child
                link.pivotB = new Vector3f(0,0,  -(height * .5f));

//                PhysicsConeJoint joint = new PhysicsConeJoint(parent, shapeNode,
//                        link.pivotA,
//                        link.pivotB);
                //TODO:
//                joint.setAngularOnly(true);
//                joint.setLimit(FastMath.QUARTER_PI, FastMath.QUARTER_PI, 0);

                Physics6DofJoint joint = new Physics6DofJoint(parent,shapeNode,
                                                                            link.pivotA,
                                                                            link.pivotB,true);
                joint.getRotationalLimitMotor(0).setHiLimit(0);
                joint.getRotationalLimitMotor(0).setLoLimit(0);

                link.joint = joint;
                joint.setCollisionBetweenLinkedBodys(false);
                space.addQueued(joint);
            }
            list.add(link);
            boneRecursion(childBone, shapeNode, list);
        }
        return list;
    }

    private Quaternion tempRot = new Quaternion();
//    public void update(float tpf) {
//        for (PhysicsBoneLink link : boneLinks) {
//            //TODO: do updateGeometric here, remove nodes from scenegraph (is for debug now)
//            //Looks bad updating here but the shapeNode is not in a display tree
//            //maybe introduce new class or handle inside this class w/o PhysicsNodes
////            link.shapeNode.updateGeometricState();
//
//            //TODO: bone location is not shapeNode location but add 0.5*dist in child direction
//            //also rotation is different for bones..
//            Bone superParent = link.parentBone.getParent();
//            if (superParent != null) {
//                Vector3f superPosition = superParent.getWorldPosition();
//                Quaternion superRotation = superParent.getWorldRotation();
//
//                Vector3f localPosition = link.shapeNode.getWorldTranslation().subtract(superPosition);
//                //todo:scale
//                tempRot.set(superRotation).inverseLocal().multLocal(localPosition);
//
//                Quaternion localRotation = new Quaternion();
//                localRotation.set(link.shapeNode.getWorldRotation());
//                tempRot.set(superRotation).inverseLocal().mult(localRotation, localRotation);
//
//                link.parentBone.setUserTransformsWorld(localPosition, Quaternion.IDENTITY);
////                link.parentBone.setUserTransforms(localPosition, localRotation, null);
//            } else {
//                link.parentBone.setUserTransformsWorld(link.shapeNode.getWorldTranslation(),
//                                                       Quaternion.IDENTITY);
////                link.parentBone.setUserTransforms(link.shapeNode.getWorldTranslation(),
////                link.shapeNode.getWorldRotation(), null);
//            }
//        }
////        skeleton.updateWorldVectors();
//    }


    public void update(float tpf) {
        TempVars vars = TempVars.get();
        assert vars.lock();

        skeleton.reset();
        for (PhysicsBoneLink link : boneLinks) {
            link.shapeNode.updateGeometricState();
            Vector3f p   = link.shapeNode.getWorldTranslation();
            Quaternion q = link.shapeNode.getWorldRotation();

            q.toAxes(vars.tri);

            Vector3f dir = vars.tri[2];
            float len = link.length;

            Vector3f parentPos = new Vector3f(p).subtractLocal(dir.mult(len/2f));
            Vector3f childPos  = new Vector3f(p).addLocal(dir.mult(len/2f));

            Quaternion q2 = q.clone();
            Quaternion rot = new Quaternion();
            rot.fromAngles(FastMath.HALF_PI, 0, 0);
            q2.multLocal(rot);
            q2.normalize();
            
            link.parentBone.setUserTransformsWorld(parentPos, q2);
            if (link.childBone.getChildren().size() == 0){
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
                Vector3f parentPos = childBone.getWorldPosition();
                logger.log(Level.INFO, "Found root bone in skeleton {0}", skeleton);
                PhysicsNode shapeNode = new PhysicsNode(null, new SphereCollisionShape(.1f), 1);
                shapeNode.setLocalTranslation(parentPos);
                shapeNode.updateGeometricState();
//                root.getRootNode().attachChild(shapeNode);
                space.addQueued(shapeNode);
                boneLinks = boneRecursion(childBone, shapeNode, list);
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
    }

    public void write(JmeExporter ex) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void read(JmeImporter im) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
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
