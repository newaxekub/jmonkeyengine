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

import java.io.InputStream;
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author normenhansen
 */
public class AssetsLookupProvider implements LookupProvider {

    public Lookup createAdditionalLookup(Lookup lookup) {
        Project prj = lookup.lookup(Project.class);
        FileObject assetsProperties = prj.getProjectDirectory().getFileObject("nbproject/assets.properties");
        if (assetsProperties != null) {
            FileLock lock = null;
            try {
                lock = assetsProperties.lock();
                InputStream in = assetsProperties.getInputStream();
                Properties properties = new Properties();
                properties.load(in);
                in.close();
                String assetsFolderName = properties.getProperty("assets.folder.name", "assets");
                if (prj.getProjectDirectory().getFileObject(assetsFolderName) != null) {
                    return Lookups.fixed(new ProjectAssetManager(prj, assetsFolderName));
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (lock != null) {
                    lock.releaseLock();
                }
            }
        } else if (prj.getProjectDirectory().getFileObject("assets") != null) {
            return Lookups.fixed(new ProjectAssetManager(prj, "assets"));
        }

        return Lookups.fixed();
    }
}
