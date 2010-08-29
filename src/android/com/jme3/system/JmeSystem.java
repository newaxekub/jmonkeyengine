package com.jme3.system;

import android.content.res.Resources;
import com.jme3.util.AndroidLogHandler;
import com.jme3.asset.AndroidAssetManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.DummyAudioRenderer;
import com.jme3.system.JmeContext.Type;
import com.jme3.system.android.OGLESContext;
import com.jme3.util.JmeFormatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;



public class JmeSystem {

    private static final Logger logger = Logger.getLogger(JmeSystem.class.getName());

    private static boolean initialized = false;
    private static Resources res;

    public static void initialize(AppSettings settings){
        if (initialized)
            return;

        initialized = true;
        try {
            JmeFormatter formatter = new JmeFormatter();

            Handler consoleHandler = new AndroidLogHandler();
            consoleHandler.setFormatter(formatter);
//            Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
//            Logger.getLogger("").addHandler(consoleHandler);

//            Logger.getLogger("com.g3d").setLevel(Level.FINEST);
        } catch (SecurityException ex){
            logger.log(Level.SEVERE, "Security error in creating log file", ex);
        }
        logger.info("Running on "+getFullName());
    }

    public static String getFullName(){
        return "jMonkey Engine 3 ALPHA 0.50";
    }
    
    public static JmeContext newContext(AppSettings settings, Type contextType) {
        initialize(settings);
        return new OGLESContext();
    }

    public static AudioRenderer newAudioRenderer(AppSettings settings) {
        return new DummyAudioRenderer();
    }

    public static void setResources(Resources res){
        JmeSystem.res = res;
    }

    public static Resources getResources(){
        return res;
    }

    public static AssetManager newAssetManager(){
        return new AndroidAssetManager(true);
    }

    public static boolean showSettingsDialog(AppSettings settings) {
        return true;
    }

}
