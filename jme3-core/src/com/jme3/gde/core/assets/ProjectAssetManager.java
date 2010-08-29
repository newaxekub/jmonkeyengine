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
package com.jme3.gde.core.assets;

import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.system.JmeSystem;
import java.util.ArrayList;
import java.util.Enumeration;
import org.netbeans.api.project.Project;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;

/**
 *
 * @author normenhansen
 */
public class ProjectAssetManager {

    private Project project;
    //TODO: one assetmanager per project
    private AssetManager manager;
    private String folderName;

    public ProjectAssetManager(Project prj, String folderName) {
        this.project = prj;
        this.folderName = folderName;
        AssetManager manager = getManager();
        StatusDisplayer.getDefault().setStatusText("adding asset folder from " + prj.getProjectDirectory() + "/" + folderName + " to assetmanager");

        manager.registerLoader("com.jme3.export.binary.BinaryImporter", "j3s");

        manager.registerLocator(prj.getProjectDirectory().getPath() + "/" + folderName + "/",
                "com.jme3.asset.plugins.FileLocator");

    }

    public Project getProject() {
        return project;
    }

    public String getRelativeAssetPath(String absolutePath) {
        String prefix = project.getProjectDirectory().getFileObject(folderName + "/").getPath();
        int idx = absolutePath.indexOf(prefix);
        if (idx == 0) {
            return absolutePath.substring(prefix.length()+1);
        }
        return absolutePath;
    }

    public AssetManager getManager() {
        if (manager == null) {
            manager = new DesktopAssetManager(true);
        }
        return manager;
    }

    public String[] getMaterials() {
        FileObject assetsFolder = project.getProjectDirectory().getFileObject(folderName + "/");
        Enumeration<FileObject> assets = (Enumeration<FileObject>) assetsFolder.getChildren(true);
        ArrayList<String> list = new ArrayList<String>();
        while (assets.hasMoreElements()) {
            FileObject asset = assets.nextElement();
            if (asset.hasExt("j3m")) {
                list.add(getRelativeAssetPath(asset.getPath()));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public String[] getSounds() {
        FileObject assetsFolder = project.getProjectDirectory().getFileObject(folderName + "/");
        Enumeration<FileObject> assets = (Enumeration<FileObject>) assetsFolder.getChildren(true);
        ArrayList<String> list = new ArrayList<String>();
        while (assets.hasMoreElements()) {
            FileObject asset = assets.nextElement();
            if (asset.hasExt("wav")||asset.hasExt("ogg")) {
                list.add(getRelativeAssetPath(asset.getPath()));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public String[] getTextures() {
        FileObject assetsFolder = project.getProjectDirectory().getFileObject(folderName + "/");
        Enumeration<FileObject> assets = (Enumeration<FileObject>) assetsFolder.getChildren(true);
        ArrayList<String> list = new ArrayList<String>();
        while (assets.hasMoreElements()) {
            FileObject asset = assets.nextElement();
            if (asset.hasExt("jpg")||asset.hasExt("jpeg")||asset.hasExt("gif")||asset.hasExt("png")||asset.hasExt("dds")||asset.hasExt("pfm")||asset.hasExt("hdr")||asset.hasExt("tga")) {
                list.add(getRelativeAssetPath(asset.getPath()));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public String[] getMatDefs() {
        FileObject assetsFolder = project.getProjectDirectory().getFileObject(folderName + "/");
        Enumeration<FileObject> assets = (Enumeration<FileObject>) assetsFolder.getChildren(true);
        ArrayList<String> list = new ArrayList<String>();
        while (assets.hasMoreElements()) {
            FileObject asset = assets.nextElement();
            if (asset.hasExt("j3md")) {
                list.add(getRelativeAssetPath(asset.getPath()));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * @return the folderName
     */
    public String getFolderName() {
        return folderName;
    }

    /**
     * @param folderName the folderName to set
     */
    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}
