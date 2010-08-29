package com.jme3.system;

import com.jme3.system.JmeSystem.Platform;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class for extracting the natives (dll, so) from the jars.
 * This class should only be used internally.
 */
public class Natives {

    private static final Logger logger = Logger.getLogger(Natives.class.getName());
    private static final byte[] buf = new byte[1024];
    private static File workingDir = new File("").getAbsoluteFile();

    public static void setExtractionDir(String name){
        workingDir = new File(name).getAbsoluteFile();
    }

    protected static void extractNativeLib(String sysName, String name) throws IOException{
        String fullname = System.mapLibraryName(name);

        String path = "native/"+sysName+"/" + fullname;
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        //InputStream in = Natives.class.getResourceAsStream();
        if (in == null) {
            logger.log(Level.WARNING, "Cannot locate native library: {0}/{1}", 
                    new String[]{ sysName, fullname} );
            return;
        }
        File targetFile = new File(workingDir, fullname);
        OutputStream out = new FileOutputStream(targetFile);
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();

        logger.log(Level.FINE, "Copied {0} to {1}", new Object[]{fullname, targetFile});
    }

    private static String getExtractionDir(){
        URL temp = Natives.class.getResource("");
        if (temp != null) {
            StringBuilder sb = new StringBuilder(temp.toString());
            if (sb.indexOf("jar:") == 0) {
                sb.delete(0, 4);
                sb.delete(sb.indexOf("!"), sb.length());
                sb.delete(sb.lastIndexOf("/") + 1, sb.length());
            }
            try {
                return new URL(sb.toString()).toString();
            } catch (MalformedURLException ex) {
                return null;
            }
        }
        return null;
    }

    protected static void extractNativeLibs(Platform platform, AppSettings settings) throws IOException{
        String renderer = settings.getRenderer();
        String audioRenderer = settings.getAudioRenderer();
        boolean needLWJGL = false;
        boolean needGG = false;
        boolean needJOGL = false;
        boolean needJOAL = false;
        boolean needOAL = false;
        boolean needJInput = false;
        if (renderer != null){
            if (renderer.startsWith("LWJGL")){
                needLWJGL = true;
            }else if (renderer.startsWith("JOGL")){
                needGG = true;
                needJOGL = true;
            }
        }
        if (audioRenderer != null){
            if (audioRenderer.equals("LWJGL")){
                needLWJGL = true;
                needOAL = true;
            }else if (audioRenderer.equals("JOAL")){
                needJOAL = true;
                needGG = true;
                needOAL = true;
            }
        }
        needJInput = settings.useJoysticks();

        if (needLWJGL){
            logger.log(Level.INFO, "Extraction Directory #1: {0}", getExtractionDir());
            logger.log(Level.INFO, "Extraction Directory #2: {0}", workingDir.toString());
            logger.log(Level.INFO, "Extraction Directory #3: {0}", System.getProperty("user.dir"));
            // LWJGL supports this feature where
            // it can load libraries from this path.
            // This is a fallback method in case the OS doesn't load
            // native libraries from the working directory (e.g Linux).
            System.setProperty("org.lwjgl.librarypath", workingDir.toString());
        }

        switch (platform){
            case Windows64:
                if (needLWJGL){
                    extractNativeLib("windows", "lwjgl64");
                }
                if (needJOGL){
                    extractNativeLib("win64", "jogl_awt");
                    extractNativeLib("win64", "jogl");
                }

                if (needJOAL)
                    extractNativeLib("win64", "joal_native");

                if (needGG)
                    extractNativeLib("win64", "gluegen-rt");

                if (needOAL)
                    extractNativeLib("windows", "OpenAL64");
                
                if (needJInput){
                    extractNativeLib("windows", "jinput-dx8_64");
                    extractNativeLib("windows", "jinput-raw_64");
                }
                break;
            case Windows32:
                if (needLWJGL){
                    extractNativeLib("windows", "lwjgl");
                }
                if (needJOGL){
                    extractNativeLib("win32", "jogl_awt");
                    extractNativeLib("win32", "jogl");
                }

                if (needJOAL)
                    extractNativeLib("win32", "joal_native");

                if (needGG)
                    extractNativeLib("win32", "gluegen-rt");

                if (needOAL)
                    extractNativeLib("windows", "OpenAL32");

                if (needJInput){
                    extractNativeLib("windows", "jinput-dx8");
                    extractNativeLib("windows", "jinput-raw");
                }
                break;
            case Linux64:
                if (needLWJGL){
                    extractNativeLib("linux", "lwjgl64");
                }
                if (needJOGL){
                    extractNativeLib("linux64", "jogl_awt");
                    extractNativeLib("linux64", "jogl");
                }

                if (needJOAL)
                    extractNativeLib("linux64", "joal_native");

                if (needGG)
                    extractNativeLib("linux64", "gluegen-rt");

                if (needJInput)
                    extractNativeLib("linux", "jinput-linux64");

                if (needOAL)    
                    extractNativeLib("linux", "openal64");

                break;
            case Linux32:
                if (needLWJGL){
                    extractNativeLib("linux", "lwjgl");
                }
                if (needJOGL){
                    extractNativeLib("linux32", "jogl_awt");
                    extractNativeLib("linux32", "jogl");
                }

                if (needJOAL)
                    extractNativeLib("linux32", "joal_native");

                if (needGG)
                    extractNativeLib("linux32", "gluegen-rt");

                if (needJInput)
                    extractNativeLib("linux", "jinput-linux");

                if (needOAL)
                    extractNativeLib("linux", "openal");

                break;
            case MacOSX_PPC32:
                if (needLWJGL){
                    extractNativeLib("macosx", "lwjgl");
                }
                if (needJOGL){
                    extractNativeLib("macosx_ppc", "jogl_awt");
                    extractNativeLib("macosx_ppc", "jogl");
                }

                if (needJOAL)
                    throw new UnsupportedOperationException("JOAL not available on Mac OS PPC");

                if (needGG)
                    extractNativeLib("macosx_ppc", "gluegen-rt");

//                if (needOAL)
//                    extractNativeLib("macosx", "openal");

                if (needJInput)
                    extractNativeLib("macosx", "jinput-osx");

                break;
            case MacOSX32:
                if (needLWJGL){
                    extractNativeLib("macosx", "lwjgl");
                }
                if (needJOGL){
                    extractNativeLib("macosx_universal", "jogl_awt");
                    extractNativeLib("macosx_universal", "jogl");
                }

                if (needJOAL)
                    extractNativeLib("macosx_universal", "joal_native");

                if (needGG)
                    extractNativeLib("macosx_universal", "gluegen-rt");

//                if (needOAL)
//                    extractNativeLib("macosx", "openal");

                if (needJInput)
                    extractNativeLib("macosx", "jinput-osx");

                break;
            case MacOSX_PPC64:
                if (needLWJGL){
                    extractNativeLib("macosx", "lwjgl");
                }
                if (needJOGL){
                    extractNativeLib("macosx_ppc", "jogl_awt");
                    extractNativeLib("macosx_ppc", "jogl");
                }

                if (needJOAL)
                    throw new UnsupportedOperationException("JOAL not available on Mac OS 64 bit");

                if (needGG)
                    extractNativeLib("macosx_ppc", "gluegen-rt");

//                if (needOAL)
//                    extractNativeLib("macosx", "openal");

                if (needJInput)
                    extractNativeLib("macosx", "jinput-osx");

                break;
            case MacOSX64:
                if (needLWJGL){
                    extractNativeLib("macosx", "lwjgl");
                }
                if (needJOGL){
                    extractNativeLib("macosx_universal", "jogl_awt");
                    extractNativeLib("macosx_universal", "jogl");
                }

                if (needJOAL)
                    throw new UnsupportedOperationException("JOAL not available on Mac OS 64 bit");

                if (needGG)
                    extractNativeLib("macosx_universal", "gluegen-rt");

//                if (needOAL)
//                    extractNativeLib("macosx", "openal");

                if (needJInput)
                    extractNativeLib("macosx", "jinput-osx");

                break;
            
                
        }
    }

}
