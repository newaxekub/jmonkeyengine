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

import com.jme3.bullet.nodes.PhysicsCharacterNode;
import java.awt.Image;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;

/**
 *
 * @author normenhansen
 */
@org.openide.util.lookup.ServiceProvider(service=SceneExplorerNode.class)
public class JmePhysicsCharacterNode extends JmePhysicsGhostNode {

    private static Image smallImage =
            ImageUtilities.loadImage("com/jme3/gde/core/sceneexplorer/nodes/icons/player.gif");
    private PhysicsCharacterNode geom;

    public JmePhysicsCharacterNode() {
    }

    public JmePhysicsCharacterNode(PhysicsCharacterNode spatial, SceneExplorerChildren children) {
        super(spatial, children);
        getLookupContents().add(spatial);
        this.geom = spatial;
        setName(spatial.getName());
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
        Sheet sheet = super.createSheet();
        Sheet.Set set = Sheet.createPropertiesSet();
        set.setDisplayName("PhysicsCharacterNode");
        set.setName(PhysicsCharacterNode.class.getName());
        PhysicsCharacterNode obj = geom;//getLookup().lookup(Spatial.class);
        if (obj == null) {
            return sheet;
        }

        set.put(makeProperty(obj, int.class, "getUpAxis", "setUpAxis", "Up Axis"));
        set.put(makeProperty(obj, float.class, "getFallSpeed", "setFallSpeed", "Fall Speed"));
        set.put(makeProperty(obj, float.class, "getJumpSpeed", "setJumpSpeed", "Jump Speed"));
        set.put(makeProperty(obj, float.class, "getGravity", "setGravity", "Gravity"));
        set.put(makeProperty(obj, float.class, "getMaxSlope", "setMaxSlope", "Max Slope"));

        sheet.put(set);
        return sheet;

    }

    public Class getExplorerObjectClass() {
        return PhysicsCharacterNode.class;
    }

    public Class getExplorerNodeClass() {
        return JmePhysicsCharacterNode.class;
    }

    public org.openide.nodes.Node[] createNodes(Object key, Object key2, SaveCookie cookie) {
        SceneExplorerChildren children=new SceneExplorerChildren((com.jme3.scene.Spatial)key);
        children.setCookie(cookie);
        return new org.openide.nodes.Node[]{new JmePhysicsCharacterNode((PhysicsCharacterNode) key, children).setSaveCookie(cookie)};
    }
}
