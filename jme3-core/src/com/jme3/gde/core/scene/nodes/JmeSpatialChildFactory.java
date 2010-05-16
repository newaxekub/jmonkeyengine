/*
 *  Copyright (c) 2009-2010 jMonkeyEngine
 *  All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 * 
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 
 *  * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.core.scene.nodes;

import com.jme3.effect.ParticleEmitter;
import com.jme3.font.BitmapText;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.light.PointLight;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author normenhansen
 */
public class JmeSpatialChildFactory extends ChildFactory<Object> {

    private com.jme3.scene.Spatial spatial;
    private boolean doLights = true;
    private boolean doMesh = true;

    public JmeSpatialChildFactory(com.jme3.scene.Spatial spatial) {
        this.spatial = spatial;
    }

    public JmeSpatialChildFactory(com.jme3.scene.Spatial spatial, boolean doLights) {
        this.spatial = spatial;
        this.doLights = doLights;
    }

    public void refreshChildren(boolean immediate) {
        super.refresh(immediate);
    }

    @Override
    protected boolean createKeys(final List<Object> toPopulate) {
        try {
            return SceneApplication.getApplication().enqueue(new Callable<Boolean>() {

                public Boolean call() throws Exception {
                    if (spatial != null && spatial instanceof com.jme3.scene.Node) {
                        toPopulate.addAll(((com.jme3.scene.Node) spatial).getChildren());
                        return true;
                    }
                    if (spatial instanceof Geometry && doMesh) {
                        Geometry geom = (Geometry) spatial;
                        Mesh mesh = geom.getMesh();
                        if (mesh != null) {
                            toPopulate.add(new MeshGeometryPair(mesh, geom));
                        }
                    }
                    if (doLights) {
                        LightList lights = spatial.getWorldLightList();
                        for (int i = 0; i < lights.size(); i++) {
                            Light light = lights.get(i);
                            if (light != null) {
                                toPopulate.add(new LightSpatialPair(light, spatial));
                            }
                        }
                    }
                    return true;
                }
            }).get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(Object key) {
        //TODO: add way for plugins to add their own spatial types, probably
        //      best via registering some object in the global lookup
        if (key instanceof Spatial) {
            JmeSpatialChildFactory factory = new JmeSpatialChildFactory((Spatial) key);
            if (key instanceof com.jme3.audio.AudioNode) {
                return new JmeAudioNode((com.jme3.audio.AudioNode) key, factory);
            }
            if (key instanceof com.jme3.scene.Node) {
                return new JmeNode((com.jme3.scene.Node) key, factory);
            }
            if (key instanceof BitmapText) {
                return new JmeBitmapText((BitmapText) key, factory);
            }
            if (key instanceof Picture) {
                return new JmePicture((Picture) key, factory);
            }
            if (key instanceof ParticleEmitter) {
                return new JmeParticleEmitter((ParticleEmitter) key, factory);
            }
            if (key instanceof com.jme3.scene.Geometry) {
                return new JmeGeometry((Geometry) key, factory);
            }
            return new JmeSpatial((Spatial) key, factory);
        } else if (key instanceof LightSpatialPair) {
            LightSpatialPair pair = (LightSpatialPair) key;
            if (pair.getLight() instanceof PointLight) {
                return new JmePointLight(pair.getSpatial(), (PointLight) pair.getLight());
            }
            if (pair.getLight() instanceof DirectionalLight) {
                return new JmeDirectionalLight(pair.getSpatial(), (DirectionalLight) pair.getLight());
            }
            return new JmeLight(pair.getSpatial(), pair.getLight());
        } else if (key instanceof MeshGeometryPair) {
            MeshGeometryPair pair = (MeshGeometryPair) key;
            return new JmeMesh(pair.getGeometry(),pair.getMesh());
        }
        return null;
    }

    @Override
    protected Node[] createNodesForKey(Object key) {
        Node[] nodes = new Node[1];
        nodes[0] = createNodeForKey(key);
        return nodes;
    }
}
