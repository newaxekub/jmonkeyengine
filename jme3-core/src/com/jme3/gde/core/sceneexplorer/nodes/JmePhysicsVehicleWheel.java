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

import com.jme3.bullet.nodes.PhysicsVehicleWheel;
import com.jme3.math.Vector3f;
import java.awt.Image;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;

/**
 *
 * @author normenhansen
 */
@org.openide.util.lookup.ServiceProvider(service=SceneExplorerNode.class)
public class JmePhysicsVehicleWheel extends JmeNode {

    private static Image smallImage =
            ImageUtilities.loadImage("com/jme3/gde/core/sceneexplorer/nodes/icons/node.gif");
    private PhysicsVehicleWheel geom;

    public JmePhysicsVehicleWheel() {
    }

    public JmePhysicsVehicleWheel(PhysicsVehicleWheel spatial, SceneExplorerChildren children) {
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
        set.setDisplayName("PhysicsVehicleWheel");
        set.setName(PhysicsVehicleWheel.class.getName());
        PhysicsVehicleWheel obj = geom;//getLookup().lookup(Spatial.class);
        if (obj == null) {
            return sheet;
        }
        set.put(makeProperty(obj, boolean.class, "isFrontWheel", "setFrontWheel", "Front Wheel"));
        set.put(makeProperty(obj, Vector3f.class, "getLocation", "Location"));
        set.put(makeProperty(obj, Vector3f.class, "getDirection", "Direction"));
        set.put(makeProperty(obj, Vector3f.class, "getAxle", "Axle"));

        set.put(makeProperty(obj, float.class, "getMaxSuspensionForce", "setMaxSuspensionForce", "Max Suspension Force"));
        set.put(makeProperty(obj, float.class, "getSuspensionStiffness", "setSuspensionStiffness", "Suspension Stiffness"));
        set.put(makeProperty(obj, float.class, "getFrictionSlip", "setFrictionSlip", "Friction Slip"));

        set.put(makeProperty(obj, float.class, "getMaxSuspensionTravelCm", "setMaxSuspensionTravelCm", "Max Suspension Travel Cm"));
        set.put(makeProperty(obj, float.class, "getMaxSuspensionForce", "setMaxSuspensionForce", "Max Suspension Force"));
        set.put(makeProperty(obj, float.class, "getWheelsDampingCompression", "setWheelsDampingCompression", "Wheels Damping Compression"));
        set.put(makeProperty(obj, float.class, "getWheelsDampingRelaxation", "setWheelsDampingRelaxation", "Wheels Damping Relaxation"));

        set.put(makeProperty(obj, float.class, "getRollInfluence", "setRollInfluence", "Roll Influence"));
        set.put(makeProperty(obj, float.class, "getRadius", "setRadius", "Radius"));
        set.put(makeProperty(obj, float.class, "getRestLength", "setRestLength", "Rest Length"));

        sheet.put(set);
        return sheet;

    }

    public Class getExplorerObjectClass() {
        return PhysicsVehicleWheel.class;
    }

    public Class getExplorerNodeClass() {
        return JmePhysicsVehicleWheel.class;
    }

    public org.openide.nodes.Node[] createNodes(Object key, Object key2, SaveCookie cookie) {
        SceneExplorerChildren children=new SceneExplorerChildren((com.jme3.scene.Spatial)key);
        children.setCookie(cookie);
        return new org.openide.nodes.Node[]{new JmePhysicsVehicleWheel((PhysicsVehicleWheel) key, children).setSaveCookie(cookie)};
    }

}
