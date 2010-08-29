package com.jme3.system;

import com.jme3.app.SettingsDialog;
import com.jme3.app.SettingsDialog.SelectionListener;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.util.JmeFormatter;
import com.jme3.audio.AudioRenderer;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

public class JmeSystem {

    public static enum Platform {

        /**
         * Microsoft Windows 32 bit
         */
        Windows32,

        /**
         * Microsoft Windows 64 bit
         */
        Windows64,

        /**
         * Linux 32 bit
         */
        Linux32,


        /**
         * Linux 64 bit
         */
        Linux64,

        /**
         * Apple Mac OS X 32 bit
         */
        MacOSX32,

        /**
         * Apple Mac OS X 64 bit
         */
        MacOSX64,

        /**
         * Apple Mac OS X 32 bit PowerPC
         */
        MacOSX_PPC32,

        /**
         * Apple Mac OS X 64 bit PowerPC
         */
        MacOSX_PPC64,

        /**
         * Google Android Smartphone OS
         */
        Android
    }

    private static final Logger logger = Logger.getLogger(JmeSystem.class.getName());

    private static boolean initialized = false;
    private static boolean lowPermissions = false;
    
    public static boolean trackDirectMemory(){
        return false;
    }

    public static void setLowPermissions(boolean lowPerm){
        lowPermissions = lowPerm;
    }

    public static boolean isLowPermissions() {
        return lowPermissions;
    }

    public static AssetManager newAssetManager(URL configFile){
        return new DesktopAssetManager(configFile);
    }
    
    public static AssetManager newAssetManager(){
        return new DesktopAssetManager(null);
    }

    public static boolean showSettingsDialog(AppSettings sourceSettings){
        if (SwingUtilities.isEventDispatchThread())
            throw new IllegalStateException("Cannot run from EDT");

        final URL iconUrl = JmeSystem.class.getResource("/com/jme3/app/Monkey.png");
        final AppSettings settings = new AppSettings(false);
        settings.copyFrom(sourceSettings);

        final AtomicBoolean done = new AtomicBoolean();
        final AtomicInteger result = new AtomicInteger();
        final Object lock = new Object();

        final SelectionListener selectionListener = new SelectionListener(){
            public void onSelection(int selection){
                synchronized (lock){
                    done.set(true);
                    result.set(selection);
                    lock.notifyAll();
                }
            }
        };
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (lock) {
                    SettingsDialog dialog = new SettingsDialog(settings, iconUrl);
                    dialog.setSelectionListener(selectionListener);
                    dialog.showDialog();
                }
            }
        });
     
        synchronized (lock){
            while (!done.get())
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                }
        }

        sourceSettings.copyFrom(settings);

        return result.get() == SettingsDialog.APPROVE_SELECTION;
    }

    private static boolean is64Bit(String arch){
        if (arch.equals("x86"))
            return false;
        else if (arch.equals("amd64"))
            return true;
        else if (arch.equals("x86_64"))
            return true;
        else if (arch.equals("ppc") || arch.equals("PowerPC"))
            return false;
        else if (arch.equals("ppc64"))
            return true;
        else if (arch.equals("i386") || arch.equals("i686"))
            return false;
        else
            throw new UnsupportedOperationException("Unsupported architecture: "+arch);
    }

    public static Platform getPlatform(){
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        boolean is64 = is64Bit(arch);
        if (os.contains("windows")){
            return is64 ? Platform.Windows64 : Platform.Windows32;
        }else if (os.contains("linux") || os.contains("freebsd") || os.contains("sunos")){
            return is64 ? Platform.Linux64 : Platform.Linux32;
        }else if (os.contains("mac os x")){
            if (arch.startsWith("ppc")){
                return is64 ? Platform.MacOSX_PPC64 : Platform.MacOSX_PPC32;
            }else{
                return is64 ? Platform.MacOSX64 : Platform.MacOSX32;
            }
        }else{
            throw new UnsupportedOperationException("The specified platform: "+os+" is not supported.");
        }
    }

    private static JmeContext newContextLwjgl(AppSettings settings, JmeContext.Type type){
        try{
            Class<? extends JmeContext> ctxClazz = null;
            switch (type){
                case Canvas:
                    ctxClazz = (Class<? extends JmeContext>) Class.forName("com.jme3.system.lwjgl.LwjglCanvas");
                    break;
                case Display:
                    ctxClazz = (Class<? extends JmeContext>) Class.forName("com.jme3.system.lwjgl.LwjglDisplay");
                    break;
                case OffscreenSurface:
                    ctxClazz = (Class<? extends JmeContext>) Class.forName("com.jme3.system.lwjgl.LwjglOffscreenBuffer");
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported context type " + type);
            }

            return ctxClazz.newInstance();
        }catch (InstantiationException ex){
            logger.log(Level.SEVERE, "Failed to create context", ex);
        }catch (IllegalAccessException ex){
            logger.log(Level.SEVERE, "Failed to create context", ex);
        }catch (ClassNotFoundException ex){
            logger.log(Level.SEVERE, "CRITICAL ERROR: Context class is missing!\n" +
                                     "Make sure jme3_lwjgl-ogl is on the classpath.", ex);
        }
        
        return null;
    }

    public static JmeContext newContextJogl(AppSettings settings, JmeContext.Type type){
        try{
            Class<? extends JmeContext> ctxClazz = null;
            switch (type){
                case Display:
                    ctxClazz = (Class<? extends JmeContext>) Class.forName("com.jme3.system.jogl.JoglDisplay");
                    break;
                case Canvas:
                    ctxClazz = (Class<? extends JmeContext>) Class.forName("com.jme3.system.jogl.JoglCanvas");
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported context type " + type);
            }

            return ctxClazz.newInstance();
        }catch (InstantiationException ex){
            logger.log(Level.SEVERE, "Failed to create context", ex);
        }catch (IllegalAccessException ex){
            logger.log(Level.SEVERE, "Failed to create context", ex);
        }catch (ClassNotFoundException ex){
            logger.log(Level.SEVERE, "CRITICAL ERROR: Context class is missing!\n" +
                                     "Make sure jme3_jogl is on the classpath.", ex);
        }

        return null;
    }

    public static JmeContext newContext(AppSettings settings, JmeContext.Type contextType) {
        initialize(settings);
        JmeContext ctx;
        if (settings.getRenderer().startsWith("LWJGL")){
            ctx = newContextLwjgl(settings, contextType);
            ctx.setSettings(settings);
        }else if (settings.getRenderer().startsWith("JOGL")){
            ctx = newContextJogl(settings, contextType);
            ctx.setSettings(settings);
        }else if (settings.getRenderer().equals("NULL")){
            ctx = new NullContext();
            ctx.setSettings(settings);
        }else{
            throw new UnsupportedOperationException(
                            "Unrecognizable renderer specified: "+
                            settings.getRenderer());
        }
        return ctx;
    }

    public static AudioRenderer newAudioRenderer(AppSettings settings){
        initialize(settings);
        Class<? extends AudioRenderer> clazz = null;
        try {
            if (settings.getAudioRenderer().startsWith("LWJGL")){
                clazz = (Class<? extends AudioRenderer>) Class.forName("com.jme3.audio.lwjgl.LwjglAudioRenderer");
            }else if (settings.getAudioRenderer().startsWith("JOAL")){
                clazz = (Class<? extends AudioRenderer>) Class.forName("com.jme3.audio.joal.JoalAudioRenderer");
            }else{
                throw new UnsupportedOperationException(
                                "Unrecognizable audio renderer specified: "+
                                settings.getAudioRenderer());
            }

            AudioRenderer ar = clazz.newInstance();
//            ar = new QueuedAudioRenderer(ar);
            return ar;
        }catch (InstantiationException ex){
            logger.log(Level.SEVERE, "Failed to create context", ex);
        }catch (IllegalAccessException ex){
            logger.log(Level.SEVERE, "Failed to create context", ex);
        }catch (ClassNotFoundException ex){
            logger.log(Level.SEVERE, "CRITICAL ERROR: Audio implementation class is missing!\n" +
                                     "Make sure jme3_lwjgl-oal or jm3_joal is on the classpath.", ex);
        }
        return null;
    }

    public static void initialize(AppSettings settings){
        if (initialized)
            return;
        
        initialized = true;
//        try {
//            if (!lowPermissions){
//                // can only modify logging settings
//                // if permissions are available
//
//                JmeFormatter formatter = new JmeFormatter();
//                Handler fileHandler = new FileHandler("jme.log");
//                fileHandler.setFormatter(formatter);
//                Logger.getLogger("").addHandler(fileHandler);
//
//                Handler consoleHandler = new ConsoleHandler();
//                consoleHandler.setFormatter(formatter);
//                Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
//                Logger.getLogger("").addHandler(consoleHandler);
//
//                Logger.getLogger("com.jme3").setLevel(Level.FINEST);
//            }
//        } catch (IOException ex){
//            logger.log(Level.SEVERE, "I/O Error while creating log file", ex);
//        } catch (SecurityException ex){
//            logger.log(Level.SEVERE, "Security error in creating log file", ex);
//        }
        logger.log(Level.INFO, "Running on {0}", getFullName());

        
        if (!lowPermissions){
            try {
                Natives.extractNativeLibs(getPlatform(), settings);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error while copying native libraries", ex);
            }
        }
    }

    public static String getFullName(){
        return "jMonkey Engine 3 Alpha 0.6";
    }
    
}
