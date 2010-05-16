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
package com.jme3.gde.core.scene;

import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.scene.nodes.JmeNode;
import org.openide.util.Lookup;

/**
 *
 * @author normenhansen
 */
public class SceneRequest {

    private String windowTitle = "";
    private String mimeType = "";
    private Object requester;
    private JmeNode rootNode;
    private ProjectAssetManager manager;
    private boolean displayed = false;

    public SceneRequest(Object requester, JmeNode rootNode, ProjectAssetManager manager) {
        this.requester = requester;
        this.rootNode = rootNode;
        this.manager = manager;
    }

    public Lookup getLookup() {
        return rootNode.getLookup();
    }

    /**
     * @return the windowTitle
     */
    public String getWindowTitle() {
        return windowTitle;
    }

    /**
     * @param windowTitle the windowTitle to set
     */
    public void setWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @return the requester
     */
    public Object getRequester() {
        return requester;
    }

    /**
     * @return the rootNode
     */
    public JmeNode getRootNode() {
        return rootNode;
    }

    /**
     * @return the displayed
     */
    public boolean isDisplayed() {
        return displayed;
    }

    /**
     * @param displayed the displayed to set
     */
    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }

    /**
     * @return the manager
     */
    public ProjectAssetManager getManager() {
        return manager;
    }
}
