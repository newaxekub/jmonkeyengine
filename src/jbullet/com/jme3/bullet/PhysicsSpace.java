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
package com.jme3.bullet;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.ContactAddedCallback;
import com.bulletphysics.ContactDestroyedCallback;
import com.bulletphysics.ContactProcessedCallback;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.AxisSweep3_32;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.SimpleBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.GhostObject;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.extras.gimpact.GImpactCollisionAlgorithm;
import com.jme3.app.AppTask;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Vector3f;
//import com.jme3.util.GameTaskQueue;
//import com.jme3.util.GameTaskQueueManager;
import com.jme3.bullet.collision.CollisionEvent;
import com.jme3.bullet.collision.CollisionListener;
import com.jme3.bullet.collision.CollisionObject;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.nodes.PhysicsGhostNode;
import com.jme3.bullet.nodes.PhysicsCharacterNode;
import com.jme3.bullet.nodes.PhysicsVehicleNode;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.bullet.util.Converter;
import com.jme3.export.Savable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>PhysicsSpace - The central jbullet-jme physics space</p>
 * @see com.jmex.jbullet.nodes.PhysicsNode
 * @author normenhansen
 */
public class PhysicsSpace implements Savable {

    public static ThreadLocal<ConcurrentLinkedQueue<AppTask<?>>> rQueueTL =
            new ThreadLocal<ConcurrentLinkedQueue<AppTask<?>>>() {

                @Override
                protected ConcurrentLinkedQueue<AppTask<?>> initialValue() {
                    return new ConcurrentLinkedQueue<AppTask<?>>();
                }
            };
    public static ThreadLocal<ConcurrentLinkedQueue<AppTask<?>>> pQueueTL =
            new ThreadLocal<ConcurrentLinkedQueue<AppTask<?>>>() {

                @Override
                protected ConcurrentLinkedQueue<AppTask<?>> initialValue() {
                    return new ConcurrentLinkedQueue<AppTask<?>>();
                }
            };
    private ConcurrentLinkedQueue<AppTask<?>> rQueue = new ConcurrentLinkedQueue<AppTask<?>>();
    private ConcurrentLinkedQueue<AppTask<?>> pQueue = new ConcurrentLinkedQueue<AppTask<?>>();
    private static ThreadLocal<PhysicsSpace> physicsSpaceTL = new ThreadLocal<PhysicsSpace>();
    private DynamicsWorld dynamicsWorld = null;
    private BroadphaseInterface broadphase;
    private BroadphaseType broadphaseType = BroadphaseType.DBVT;
    private CollisionDispatcher dispatcher;
    private ConstraintSolver solver;
    private DefaultCollisionConfiguration collisionConfiguration;
    private Map<GhostObject, PhysicsGhostNode> physicsGhostNodes = new ConcurrentHashMap<GhostObject, PhysicsGhostNode>();
    private Map<RigidBody, PhysicsNode> physicsNodes = new ConcurrentHashMap<RigidBody, PhysicsNode>();
    private List<PhysicsJoint> physicsJoints = new LinkedList<PhysicsJoint>();
    private List<CollisionListener> collisionListeners = new LinkedList<CollisionListener>();
    private List<CollisionEvent> collisionEvents = new LinkedList<CollisionEvent>();
    private Vector3f worldMin = new Vector3f(-10000f, -10000f, -10000f);
    private Vector3f worldMax = new Vector3f(10000f, 10000f, 10000f);
    private float accuracy = 1f / 60f;

    /**
     * Get the current PhysicsSpace <b>running on this thread</b>
     * or creates a new PhysicsSpace
     * @return the exising or created PhysicsSpace
     */
    public static PhysicsSpace getPhysicsSpace() {
        return physicsSpaceTL.get();
    }

    public PhysicsSpace() {
        this(new Vector3f(-10000f, -10000f, -10000f), new Vector3f(10000f, 10000f, 10000f), BroadphaseType.DBVT);
    }

    public PhysicsSpace(BroadphaseType broadphaseType) {
        this(new Vector3f(-10000f, -10000f, -10000f), new Vector3f(10000f, 10000f, 10000f), broadphaseType);
    }

    public PhysicsSpace(Vector3f worldMin, Vector3f worldMax) {
        this(worldMin, worldMax, BroadphaseType.AXIS_SWEEP_3);
    }

    public PhysicsSpace(Vector3f worldMin, Vector3f worldMax, BroadphaseType broadphaseType) {
        this.worldMin.set(worldMin);
        this.worldMax.set(worldMax);
        this.broadphaseType = broadphaseType;
        create();
    }

    /**
     * has to be called from the (designated) physics thread
     */
    public void create() {
        rQueueTL.set(rQueue);
        pQueueTL.set(pQueue);

        collisionConfiguration = new DefaultCollisionConfiguration();
        dispatcher = new CollisionDispatcher(collisionConfiguration);
        switch (broadphaseType) {
            case SIMPLE:
                broadphase = new SimpleBroadphase();
                break;
            case AXIS_SWEEP_3:
                broadphase = new AxisSweep3(Converter.convert(worldMin), Converter.convert(worldMax));
                break;
            case AXIS_SWEEP_3_32:
                broadphase = new AxisSweep3_32(Converter.convert(worldMin), Converter.convert(worldMax));
                break;
            case DBVT:
                broadphase = new DbvtBroadphase();
                break;
        }

        solver = new SequentialImpulseConstraintSolver();

        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        dynamicsWorld.setGravity(new javax.vecmath.Vector3f(0, -9.81f, 0));

        broadphase.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
        GImpactCollisionAlgorithm.registerAlgorithm(dispatcher);

        physicsSpaceTL.set(this);
        setContactCallbacks();
    }

    private void setContactCallbacks() {
        BulletGlobals.setContactAddedCallback(new ContactAddedCallback() {

            public boolean contactAdded(ManifoldPoint cp, com.bulletphysics.collision.dispatch.CollisionObject colObj0,
                    int partId0, int index0, com.bulletphysics.collision.dispatch.CollisionObject colObj1, int partId1,
                    int index1) {
                System.out.println("contact added");
                return true;
            }
        });

        BulletGlobals.setContactProcessedCallback(new ContactProcessedCallback() {

            public boolean contactProcessed(ManifoldPoint cp, Object body0, Object body1) {
                CollisionObject node = null, node1 = null;
                if (body0 instanceof RigidBody) {
                    RigidBody rBody = (RigidBody) body0;
                    node = (PhysicsNode) rBody.getUserPointer();
                } else if (body0 instanceof GhostObject) {
                    GhostObject rBody = (GhostObject) body0;
                    node = physicsGhostNodes.get(rBody);
                }
                if (body1 instanceof RigidBody) {
                    RigidBody rBody = (RigidBody) body1;
                    node1 = (PhysicsNode) rBody.getUserPointer();
                } else if (body1 instanceof GhostObject) {
                    GhostObject rBody = (GhostObject) body1;
                    node1 = physicsGhostNodes.get(rBody);
                }
                if (node != null && node1 != null) {
                    collisionEvents.add(new CollisionEvent(CollisionEvent.TYPE_PROCESSED, node, node1, cp));
                } else {
                    System.out.println("error finding node during collision");
                }
                return true;
            }
        });

        BulletGlobals.setContactDestroyedCallback(new ContactDestroyedCallback() {

            public boolean contactDestroyed(Object userPersistentData) {
                System.out.println("contact destroyed");
                return true;
            }
        });
    }

    /**
     * updates the physics space
     * @param time the current time value
     */
    public void update(float time) {
        int subSteps=1;
        if(time>accuracy){
            subSteps=(int) (Math.ceil(time / accuracy));
        }
        update(time, subSteps);
    }

    /**
     * updates the physics space, uses maxSteps<br>
     * @param time the current time value
     * @param maxSteps
     */
    public void update(float time, int maxSteps) {
        if (getDynamicsWorld() == null) {
            return;
        }

        //add recurring events
        AppTask task = rQueue.poll();
        while (task != null) {
            pQueue.add(task);
            task = rQueue.poll();
        }
        //execute task list
        task = pQueue.poll();
        while (task != null) {
            while (task.isCancelled()) {
                task = pQueue.poll();
            }
            try {
                task.invoke();
            } catch (Exception ex) {
                Logger.getLogger(PhysicsSpace.class.getName()).log(Level.SEVERE, null, ex);
            }
            task = pQueue.poll();
        }

        //sync physicsNodes
        for (PhysicsNode physicsNode : physicsNodes.values()) {
            physicsNode.updatePhysicsState();
        }
        //sync ghostnodes TODO!
        for (PhysicsGhostNode node : physicsGhostNodes.values()) {
            node.updatePhysicsState();
        }
        //step simulation
        getDynamicsWorld().stepSimulation(time, maxSteps, accuracy);

        //distribute events
        distributeEvents();
    }

    private void distributeEvents() {
        //add collision callbacks
        for (CollisionListener listener : collisionListeners) {
            for (CollisionEvent event : collisionEvents) {
                listener.collision(event);
            }
        }
        collisionEvents.clear();
    }

    public static <V> Future<V> enqueueOnThisThread(Callable<V> callable) {
        AppTask<V> task = new AppTask<V>(callable);
        System.out.println("created apptask");
        pQueueTL.get().add(task);
        return task;
    }

    public static <V> Future<V> requeueOnThisThread(Callable<V> callable) {
        AppTask<V> task = new AppTask<V>(callable);
        rQueueTL.get().add(task);
        return task;
    }

    public <V> Future<V> enqueue(Callable<V> callable) {
        AppTask<V> task = new AppTask<V>(callable);
        pQueue.add(task);
        return task;
    }

    public <V> Future<V> requeue(Callable<V> callable) {
        AppTask<V> task = new AppTask<V>(callable);
        rQueue.add(task);
        return task;
    }

    /**
     * adds an object to the physics space
     * <br>this is normally only needed for detached physics
     * @param obj the PhyiscsNode, PhysicsGhostNode or PhysicsJoint to add
     */
    public void addQueued(final Object obj) {
        enqueue(new Callable() {

            public Object call() throws Exception {
                if (obj instanceof PhysicsGhostNode) {
                    addGhostNode((PhysicsGhostNode) obj);
                } else if (obj instanceof PhysicsNode) {
                    addNode((PhysicsNode) obj);
                } else if (obj instanceof PhysicsJoint) {
                    addJoint((PhysicsJoint) obj);
                } else {
                    throw (new UnsupportedOperationException("Cannot add this kind of object to the physics space."));
                }
                return null;
            }
        });
    }

    /**
     * adds an object to the physics space
     * <br>this is normally only needed for detached physics
     * @param obj the PhyiscsNode, PhysicsGhostNode or PhysicsJoint to remove
     */
    public void removeQueued(final Object obj) {
        enqueue(new Callable() {

            public Object call() throws Exception {
                if (obj instanceof PhysicsGhostNode) {
                    removeGhostNode((PhysicsGhostNode) obj);
                } else if (obj instanceof PhysicsNode) {
                    removeNode((PhysicsNode) obj);
                } else if (obj instanceof PhysicsJoint) {
                    removeJoint((PhysicsJoint) obj);
                } else {
                    throw (new UnsupportedOperationException("Cannot remove this kind of object from the physics space."));
                }
                return null;
            }
        });
    }

    /**
     * adds an object to the physics space
     * @param obj the PhyiscsNode, PhysicsGhostNode or PhysicsJoint to add
     */
    public void add(Object obj) {
        if (obj instanceof PhysicsGhostNode) {
            addGhostNode((PhysicsGhostNode) obj);
        } else if (obj instanceof PhysicsNode) {
            addNode((PhysicsNode) obj);
        } else if (obj instanceof PhysicsJoint) {
            addJoint((PhysicsJoint) obj);
        } else {
            throw (new UnsupportedOperationException("Cannot add this kind of object to the physics space."));
        }
    }

    /**
     * adds an object to the physics space
     * @param obj the PhyiscsNode, PhysicsGhostNode or PhysicsJoint to remove
     */
    public void remove(Object obj) {
        if (obj instanceof PhysicsGhostNode) {
            removeGhostNode((PhysicsGhostNode) obj);
        } else if (obj instanceof PhysicsNode) {
            removeNode((PhysicsNode) obj);
        } else if (obj instanceof PhysicsJoint) {
            removeJoint((PhysicsJoint) obj);
        } else {
            throw (new UnsupportedOperationException("Cannot remove this kind of object from the physics space."));
        }
    }

    private void addGhostNode(PhysicsGhostNode node) {
        physicsGhostNodes.put(node.getGhostObject(), node);
        if (node instanceof PhysicsCharacterNode) {
//            dynamicsWorld.addCollisionObject(node.getGhostObject(), CollisionFilterGroups.CHARACTER_FILTER, (short)(CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.DEFAULT_FILTER));
            getDynamicsWorld().addCollisionObject(node.getGhostObject());
            dynamicsWorld.addAction(((PhysicsCharacterNode) node).getCharacterController());
        } else {
            getDynamicsWorld().addCollisionObject(node.getGhostObject());
        }
    }

    private void removeGhostNode(PhysicsGhostNode node) {
        physicsGhostNodes.remove(node.getGhostObject());
        getDynamicsWorld().removeCollisionObject(node.getGhostObject());
        if (node instanceof PhysicsCharacterNode) {
            dynamicsWorld.removeAction(((PhysicsCharacterNode) node).getCharacterController());
        }
    }

    private void addNode(PhysicsNode node) {
        node.updatePhysicsState();
        physicsNodes.put(node.getRigidBody(), node);
        getDynamicsWorld().addRigidBody(node.getRigidBody());
        if (node instanceof PhysicsVehicleNode) {
            dynamicsWorld.addVehicle(((PhysicsVehicleNode) node).getVehicle());
        }
    }

    private void removeNode(PhysicsNode node) {
        physicsNodes.remove(node.getRigidBody());
        getDynamicsWorld().removeRigidBody(node.getRigidBody());
        if (node instanceof PhysicsVehicleNode) {
            dynamicsWorld.removeVehicle(((PhysicsVehicleNode) node).getVehicle());
        }
    }

    private void addJoint(PhysicsJoint joint) {
        physicsJoints.add(joint);
        getDynamicsWorld().addConstraint(joint.getConstraint(), !joint.isCollisionBetweenLinkedBodys());
    }

    private void removeJoint(PhysicsJoint joint) {
        physicsJoints.remove(joint);
        getDynamicsWorld().removeConstraint(joint.getConstraint());
    }

    /**
     * sets the gravity of the PhysicsSpace
     * @param gravity
     */
    public void setGravity(Vector3f gravity) {
        dynamicsWorld.setGravity(Converter.convert(gravity));
    }

    /**
     * adds a CollisionListener that will be informed about collision events
     * @param listener the CollisionListener to add
     */
    public void addCollisionListener(CollisionListener listener) {
        collisionListeners.add(listener);
    }

    /**
     * removes a CollisionListener from the list
     * @param listener the CollisionListener to remove
     */
    public void removeCollisionListener(CollisionListener listener) {
        collisionListeners.remove(listener);
    }

    /**
     * destroys the current PhysicsSpace so that a new one can be created
     */
    public void destroy() {
        physicsNodes.clear();
        physicsJoints.clear();
        physicsGhostNodes.clear();

        dynamicsWorld.destroy();
        dynamicsWorld = null;
    }

    /**
     * used internally
     * @return the dynamicsWorld
     */
    public DynamicsWorld getDynamicsWorld() {
        return dynamicsWorld;
    }

    public BroadphaseType getBroadphaseType() {
        return broadphaseType;
    }

    public void setBroadphaseType(BroadphaseType broadphaseType) {
        this.broadphaseType = broadphaseType;
    }

    /**
     * get the current accuracy of the physics computation
     * @return the current accuracy
     */
    public float getAccuracy() {
        return accuracy;
    }

    /**
     * sets the accuracy of the physics computation, default=1/60s<br>
     * @param accuracy
     */
    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public Vector3f getWorldMin() {
        return worldMin;
    }

    public void setWorldMin(Vector3f worldMin) {
        this.worldMin.set(worldMin);
    }

    public Vector3f getWorldMax() {
        return worldMax;
    }

    public void setWorldMax(Vector3f worldMax) {
        this.worldMax.set(worldMax);
    }

    public void write(JmeExporter ex) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void read(JmeImporter im) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * interface with Broadphase types
     */
    public enum BroadphaseType {

        /**
         * basic Broadphase
         */
        SIMPLE,
        /**
         * better Broadphase, needs worldBounds , max Object number = 16384
         */
        AXIS_SWEEP_3,
        /**
         * better Broadphase, needs worldBounds , max Object number = 65536
         */
        AXIS_SWEEP_3_32,
        /**
         * Broadphase allowing quicker adding/removing of physics objects
         */
        DBVT;
    }
}
