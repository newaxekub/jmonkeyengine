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

import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.scene.nodes.properties.JmeProperty;
import com.jme3.light.Light;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import java.awt.Image;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author normenhansen
 */
public class JmeMesh extends AbstractNode {

    private final InstanceContent lookupContents;
    private Geometry geom;
    private Mesh mesh;
    private static Image smallImage =
            ImageUtilities.loadImage("com/jme3/gde/core/scene/nodes/icons/mesh.gif");

    public JmeMesh(Geometry geom, Mesh mesh) {
        super(Children.LEAF, new JmeLookup(new InstanceContent()));
        this.geom = geom;
        this.mesh = mesh;
        lookupContents = ((JmeLookup) getLookup()).getInstanceContent();
        lookupContents.add(geom);
        lookupContents.add(mesh);
        lookupContents.add(this);
        setName("Mesh");
    }

    @Override
    public Image getIcon(int type) {
        return smallImage;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return smallImage;
    }

    @Override
    protected Sheet createSheet() {
        //TODO: multithreading..
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();
        set.setDisplayName("Mesh");
        set.setName(Light.class.getName());
        Mesh obj = mesh;
        if (obj == null) {
            return sheet;
        }

        set.put(makeProperty(obj, int.class, "getId", "setId", "Id"));
        set.put(makeProperty(obj, Mesh.Mode.class, "getMode", "setMode", "Mode"));
        set.put(makeProperty(obj, float.class, "getPointSize", "setPointSize", "Point Size"));

        sheet.put(set);
        return sheet;

    }

    protected SystemAction[] createActions() {
        return new SystemAction[]{
//                    SystemAction.get(CopyAction.class),
//                    SystemAction.get(CutAction.class),
//                    SystemAction.get(PasteAction.class),
                    SystemAction.get(DeleteAction.class)
                };
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public void destroy() throws IOException {
        try {
            SceneApplication.getApplication().enqueue(new Callable<Void>() {

                public Void call() throws Exception {
//                    spatial.removeLight(light);
                    return null;
                }
            }).get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private Property makeProperty(Mesh obj, Class returntype, String method, String name) {
        Property prop = null;
        try {
            prop = new JmeProperty(obj, returntype, method, null);
            prop.setName(name);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return prop;
    }

    private Property makeProperty(Mesh obj, Class returntype, String method, String setter, String name) {
        Property prop = null;
        try {
            prop = new JmeProperty(obj, returntype, method, setter);
            prop.setName(name);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return prop;
    }
}
