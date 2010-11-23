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
package com.jme3.gde.core.sceneexplorer.nodes;

import com.jme3.gde.core.sceneexplorer.nodes.properties.SceneExplorerProperty;
import com.jme3.gde.core.sceneexplorer.nodes.properties.ScenePropertyChangeListener;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author normenhansen
 */
public abstract class AbstractSceneExplorerNode extends AbstractNode implements SceneExplorerNode, ScenePropertyChangeListener{

    protected Children jmeChildren;
    protected final InstanceContent lookupContents;
    protected boolean readOnly = false;

    public AbstractSceneExplorerNode() {
        super(Children.LEAF, new SceneExplorerLookup(new InstanceContent()));
        lookupContents = ((SceneExplorerLookup) getLookup()).getInstanceContent();
    }

    public AbstractSceneExplorerNode(Children children) {
        //TODO: OMG!
        super(children, children instanceof SceneExplorerChildren
                ? (((SceneExplorerChildren) children).getDataObject() != null
                ? new ProxyLookup(((SceneExplorerChildren) children).getDataObject().getLookup(), new SceneExplorerLookup(new InstanceContent()))
                : new SceneExplorerLookup(new InstanceContent()))
                : new SceneExplorerLookup(new InstanceContent()));
        this.jmeChildren = children;
        lookupContents = getLookup().lookup(SceneExplorerLookup.class).getInstanceContent();
    }

    public InstanceContent getLookupContents() {
        return lookupContents;
    }

    public AbstractSceneExplorerNode addToLookup(Object obj) {
        lookupContents.add(obj);
        return this;
    }

    protected void fireSave(boolean modified) {
        if(jmeChildren instanceof SceneExplorerChildren){
            DataObject dobj = ((SceneExplorerChildren)jmeChildren).getDataObject();
            if(dobj!=null){
                dobj.setModified(modified);
            }
        }
    }

    /**
     * @param saveCookie the saveCookie to set
     */
    public AbstractSceneExplorerNode setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    //TODO: refresh does not work
    public void refresh(boolean immediate) {
        if (jmeChildren instanceof SceneExplorerChildren) {
            ((SceneExplorerChildren) jmeChildren).refreshChildren(immediate);
        }
    }

    protected Property makeProperty(Object obj, Class returntype, String method, String name) {
        Property prop = null;
        try {
            prop = new SceneExplorerProperty(getExplorerObjectClass().cast(obj), returntype, method, null);
            prop.setName(name);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return prop;
    }

    protected Property makeProperty(Object obj, Class returntype, String method, String setter, String name) {
        Property prop = null;
        try {
            if (readOnly) {
                prop = new SceneExplorerProperty(getExplorerObjectClass().cast(obj), returntype, method, null);
            } else {
                prop = new SceneExplorerProperty(getExplorerObjectClass().cast(obj), returntype, method, setter, this);
            }
            prop.setName(name);

        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return prop;
    }

    public void propertyChange(String name, Object before, Object after) {
        fireSave(true);
        firePropertyChange(name, before, after);
    }

    public Class getExplorerNodeClass() {
        return this.getClass();
    }

    public abstract Class getExplorerObjectClass();

    public Node[] createNodes(Object key, DataObject key2, boolean cookie) {
        return new Node[]{Node.EMPTY};
    }
}
