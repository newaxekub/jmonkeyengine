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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author normenhansen
 */
public class AbstractSceneExplorerNode extends AbstractNode implements SceneExplorerNode, PropertyChangeListener {

    protected Children jmeChildren;
    protected final InstanceContent lookupContents;
    protected SaveCookie saveCookie = null;

    public AbstractSceneExplorerNode() {
        super(Children.LEAF, new SceneExplorerLookup(new InstanceContent()));
        lookupContents = ((SceneExplorerLookup) getLookup()).getInstanceContent();
    }

    public AbstractSceneExplorerNode(Children children) {
        super(children, new SceneExplorerLookup(new InstanceContent()));
        this.jmeChildren = children;
        lookupContents = ((SceneExplorerLookup) getLookup()).getInstanceContent();
    }

    public InstanceContent getLookupContents() {
        return lookupContents;
    }

    public void fireSave(boolean modified) {
        fireSave(modified, false);
    }

    public void fireSave(boolean modified, boolean recursive) {
        if (modified) {
            if (saveCookie != null) {
                lookupContents.add(saveCookie);
            }
        } else {
            if (saveCookie != null) {
                lookupContents.remove(saveCookie);
            }
        }
        if (recursive) {
            Node[] children = getChildren().getNodes();
            for (int i = 0; i < children.length; i++) {
                Node node = children[i];
                if (node instanceof AbstractSceneExplorerNode) {
                    ((AbstractSceneExplorerNode) node).fireSave(modified, recursive);
                }
            }
        }
    }

    /**
     * @param saveCookie the saveCookie to set
     */
    public AbstractSceneExplorerNode setSaveCookie(SaveCookie saveCookie) {
        this.saveCookie = saveCookie;
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
            if (saveCookie == null) {
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

    public void propertyChange(PropertyChangeEvent evt) {
        if ((evt.getOldValue() == null && !(evt.getNewValue() == null)) || ((evt.getOldValue() != null) && !evt.getOldValue().equals(evt.getNewValue()))) {
            fireSave(true);
        }
        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }

    private class SaveCookieImpl implements SaveCookie {

        public void save() throws IOException {
            Confirmation msg = new NotifyDescriptor.Confirmation("Something went wrong!",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE);
//
            Object result = DialogDisplayer.getDefault().notify(msg);
            //When user clicks "Yes", indicating they really want to save,
            //we need to disable the Save button and Save menu item,
            //so that it will only be usable when the next change is made
            //to the text field:
            if (NotifyDescriptor.YES_OPTION.equals(result)) {
                fireSave(false);
//            Implement your save functionality here.
            }
        }
    }

    public Class getExplorerNodeClass() {
        return this.getClass();
    }

    public Class getExplorerObjectClass() {
        return Object.class;
    }

    public Node[] createNodes(Object key, Object key2, SaveCookie cookie) {
        return new Node[]{Node.EMPTY};
    }
}
