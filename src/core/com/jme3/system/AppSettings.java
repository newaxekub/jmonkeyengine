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

package com.jme3.system;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

public class AppSettings extends HashMap<String, Object> {

    private static final AppSettings defaults = new AppSettings(false);

    public static final String LWJGL_OPENGL2 = "LWJGL-OpenGL2",
                               LWJGL_OPENGL3 = "LWJGL-OpenGL3",
                               JOGL          = "JOGL",
                               NULL          = "NULL";
    public static final String LWJGL_OPENAL  = "LWJGL";

    private String settingsDialogImage="/com/jme3/app/Monkey.png";

    static {
        defaults.put("Width", 640);
        defaults.put("Height", 480);
        defaults.put("BitsPerPixel", 24);
        defaults.put("Frequency", 60);
        defaults.put("DepthBits", 24);
        defaults.put("StencilBits", 0);
        defaults.put("Samples", 0);
        defaults.put("Fullscreen", false);
        defaults.put("Title", "jMonkey Engine 3.0");
        defaults.put("Renderer", LWJGL_OPENGL2);
        defaults.put("AudioRenderer", LWJGL_OPENAL);
        defaults.put("DisableJoysticks", true);
        defaults.put("UseInput", true);        
        defaults.put("VSync", false);
        defaults.put("FrameRate", -1);

        // disable these settings to benchmark speed
//        defaults.put("VSync", true);
//        defaults.put("FrameRate", 60);
    }

    public AppSettings(boolean loadDefaults){
        if (loadDefaults){
            putAll(defaults);
        }
    }

    public void copyFrom(AppSettings other){
        this.putAll(other);
    }

    public void load(InputStream in) throws IOException{
        Properties props = new Properties();
        props.load(in);
        for (Map.Entry<Object, Object> entry : props.entrySet()){
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();
            if (key.endsWith("(int)")){
                key = key.substring(0, key.length()-5);
                int iVal = Integer.parseInt(val);
                putInteger(key, iVal);
            }else if (key.endsWith("(string)")){
                putString(key.substring(0, key.length()-8), val);
            }else if (key.endsWith("(bool)")){
                boolean bVal = Boolean.parseBoolean(val);
                putBoolean(key.substring(0, key.length()-6), bVal);
            }else{
                throw new IOException("Cannot parse key: " + key);
            }
        }
    }

    public void save(OutputStream out) throws IOException{
        Properties props = new Properties();
        for (Map.Entry<String, Object> entry : entrySet()){
            Object val = entry.getValue();
            String type;
            if (val instanceof Integer){
                type = "(int)";
            }else if (val instanceof String){
                type = "(string)";
            }else if (val instanceof Boolean){
                type = "(bool)";
            }else{
                throw new UnsupportedEncodingException();
            }
            props.setProperty(entry.getKey() + type, val.toString());
        }
        props.store(out, "jME3 AppSettings");
    }

    public void load(String preferencesKey){
        Preferences prefs = Preferences.userRoot().node(preferencesKey);
    }

    public int getInteger(String key){
        Integer i = (Integer) get(key);
        if (i == null)
            return 0;

        return i.intValue();
    }

    public boolean getBoolean(String key){
        Boolean b = (Boolean) get(key);
        if (b == null)
            return false;

        return b.booleanValue();
    }

    public String getString(String key){
        String s = (String) get(key);
        if (s == null)
            return null;

        return s;
    }

    public void putInteger(String key, int value){
        put(key, new Integer(value));
    }

    public void putBoolean(String key, boolean value){
        put(key, new Boolean(value));
    }

    public void putString(String key, String value){
        put(key, value);
    }

    public void setFrameRate(int frameRate){
        putInteger("FrameRate", frameRate);
    }

    public void setUseInput(boolean use){
        putBoolean("UseInput", use);
    }
    
    public void setUseJoysticks(boolean use){
        putBoolean("DisableJoysticks", !use);
    }

    public void setRenderer(String renderer){
        putString("Renderer", renderer);
    }

    public void setAudioRenderer(String audioRenderer){
        putString("AudioRenderer", audioRenderer);
    }

    public void setWidth(int value){
        putInteger("Width", value);
    }

    public void setHeight(int value){
        putInteger("Height", value);
    }

    public void setResolution(int width, int height){
        setWidth(width);
        setHeight(height);
    }

    public void setFrequency(int value){
        putInteger("Frequency", value);
    }

    public void setBitsPerPixel(int value){
        putInteger("BitsPerPixel", value);
    }

    public void setSamples(int value){
        putInteger("Samples", value);
    }

    public void setTitle(String title){
        putString("Title", title);
    }

    public void setFullscreen(boolean value){
        putBoolean("Fullscreen", value);
    }

    public void setVSync(boolean value){
        putBoolean("VSync", value);
    }

    public int getFrameRate(){
        return getInteger("FrameRate");
    }

    public boolean useInput(){
        return getBoolean("UseInput");
    }

    public String getRenderer(){
        return getString("Renderer");
    }

    public int getWidth(){
        return getInteger("Width");
    }

    public int getHeight(){
        return getInteger("Height");
    }

    public int getBitsPerPixel(){
        return getInteger("BitsPerPixel");
    }

    public int getFrequency(){
        return getInteger("Frequency");
    }

    public int getDepthBits(){
        return getInteger("DepthBits");
    }

    public int getStencilBits(){
        return getInteger("StencilBits");
    }

    public int getSamples(){
        return getInteger("Samples");
    }

    public String getTitle() {
        return getString("Title");
    }

    public boolean isVSync(){
        return getBoolean("VSync");
    }

    public boolean isFullscreen(){
        return getBoolean("Fullscreen");
    }

    public boolean useJoysticks() {
        return !getBoolean("DisableJoysticks");
    }

    public String getAudioRenderer() {
        return getString("AudioRenderer");
    }

    public void setSettingsDialogImage(String path){
       settingsDialogImage=path;
    }

    public String getSettingsDialogImage() {
        return settingsDialogImage;
    }
    
}
