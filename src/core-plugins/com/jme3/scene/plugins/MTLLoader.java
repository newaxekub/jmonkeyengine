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

package com.jme3.scene.plugins;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.material.MaterialList;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Scanner;

public class MTLLoader implements AssetLoader {

    protected Scanner scan;
    protected MaterialList matList;
    protected Material material;
    protected AssetManager assetManager;
    protected String folderName;

    public void reset(){
        scan = null;
        matList = null;
        material = null;
    }

    protected ColorRGBA readColor(){
        ColorRGBA v = new ColorRGBA();
        v.set(scan.nextFloat(), scan.nextFloat(), scan.nextFloat(), 1.0f);
        return v;
    }

    protected void nextStatement(){
        scan.useDelimiter("\n");
        scan.next();
        scan.useDelimiter("\\p{javaWhitespace}+");
    }

    protected void startMaterial(String name){
        material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setBoolean("UseMaterialColors", true);
        material.setColor("Ambient",  ColorRGBA.DarkGray);
        material.setColor("Diffuse",  ColorRGBA.White);
        material.setColor("Specular", ColorRGBA.Black);
        material.setFloat("Shininess", 16f); // prevents "premature culling" bug
        matList.put(name, material);
    }

    protected boolean readLine(){
        if (!scan.hasNext()){
            return false;
        }

        String cmd = scan.next();
        if (cmd.startsWith("#")){
            // skip entire comment until next line
        }else if (cmd.equals("newmtl")){
            String name = scan.next();
            startMaterial(name);
        }else if (cmd.equals("Ka") || cmd.equals("Ke") || cmd.equals("Ni") || cmd.equals("illum")){
            // ignore it for now
        }else if (cmd.equals("Kd")){
            ColorRGBA color = readColor();
            MatParam param = material.getParam("Diffuse");
            if (param != null){
                color.a = ((ColorRGBA) param.getValue()).getAlpha();
            }
            material.setColor("Diffuse", color);
        }else if (cmd.equals("Ks")){
            material.setColor("Specular", readColor());
        }else if (cmd.equals("Ns")){
            material.setFloat("Shininess", scan.nextFloat() /* (128f / 1000f)*/ );
        }else if (cmd.equals("d")){
            float alpha = scan.nextFloat();
//            if (alpha < 1f){
//                MatParam param = material.getParam("Diffuse");
//                ColorRGBA color;
//                if (param != null)
//                    color = (ColorRGBA) param.getValue();
//                else
//                    color = new ColorRGBA(ColorRGBA.White);
//
//                color.a = scan.nextFloat();
//                material.setColor("Diffuse", color);
//                material.setBoolean("UseAlpha", true);
//                material.setTransparent(true);
//                material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
//            }
        }else if (cmd.equals("map_Ka")){
            // ignore it for now
        }else if (cmd.equals("map_Kd")){
            String path = scan.next();
            String name = new File(path).getName();
            TextureKey key = new TextureKey(folderName + name);
            key.setGenerateMips(true);
            Texture texture = assetManager.loadTexture(key);
            if (texture != null){
                texture.setWrap(WrapMode.Repeat);
                material.setTexture("DiffuseMap", texture);
            }
        }else if (cmd.equals("map_bump") || cmd.equals("bump")){
            if (material.getParam("NormalMap") == null){
                String path = scan.next();
                String name = new File(path).getName();
                TextureKey key = new TextureKey(folderName + name);
                key.setGenerateMips(true);
                Texture texture = assetManager.loadTexture(key);
                if (texture != null){
                    texture.setWrap(WrapMode.Repeat);
                    material.setTexture("NormalMap", texture);
                    if (texture.getImage().getFormat() == Format.LATC){
                        material.setBoolean("LATC", true);
                    }
                }
            }
        }else if (cmd.equals("map_Ks")){
            String path = scan.next();
            String name = new File(path).getName();
            TextureKey key = new TextureKey(folderName + name);
            key.setGenerateMips(true);
            Texture texture = assetManager.loadTexture(key);
            if (texture != null){
                texture.setWrap(WrapMode.Repeat);
                material.setTexture("SpecularMap", texture);

                // NOTE: since specular color is modulated with specmap
                // make sure we have it set
                material.setColor("Specular", ColorRGBA.White);
            }
        }else if (cmd.equals("map_d")){
            String path = scan.next();
            String name = new File(path).getName();
            TextureKey key = new TextureKey(folderName + name);
            key.setGenerateMips(true);
            Texture texture = assetManager.loadTexture(key);
            if (texture != null){
                texture.setWrap(WrapMode.Repeat);
                material.setTexture("AlphaMap", texture);
                material.setTransparent(true);
                material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
                material.getAdditionalRenderState().setAlphaTest(true);
                material.getAdditionalRenderState().setAlphaFallOff(0.01f);
            }
        }else{
            System.out.println("Unknown statement in MTL! "+cmd);
        }
        nextStatement();

        return true;
    }

    @SuppressWarnings("empty-statement")
    public Object load(AssetInfo info){
        this.assetManager = info.getManager();
        folderName = info.getKey().getFolder();

        InputStream in = info.openStream();
        scan = new Scanner(in);
        scan.useLocale(Locale.US);

        matList = new MaterialList();
        while (readLine());
        MaterialList list = matList;

        reset();

        try{
            in.close();
        }catch (IOException ex){
        }
        return list;
    }
}
