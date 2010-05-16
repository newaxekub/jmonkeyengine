package com.jme3.asset;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Loads a config file to configure the asset manager.
 *
 * ====  Binary format:  ====
 *
 * struct AssetConfig
 * {
 *     UBYTE[4] signature = 'J3AS'
 *     UINT version = 0x1
 *     USHORT numLocators
 *     struct LocatorEntry[numLocators]
 *     {
 *         STRING locatorClass
 *         STRING rootPath
 *         UBYTE  numExtensions
 *         STRING[numExtensions] extensions
 *     }
 *     USHORT numLoaders
 *     struct LoaderEntry[numLoaders]
 *     {
 *         STRING loaderClass
 *         UBYTE numExtensions
 *         STRING[numExtensions] extensions
 *     }
 * }
 *
 * ====  Text format:  ====
 *
 * "LOADER" <class> : (<extension> ",")* <extension>
 * "LOCATOR" <path> <class> : (<extension> ",")* <extension>
 *
 *
 * @author Kirill Vainer
 */
public class AssetConfig {

    private AssetManager manager;

    public AssetConfig(AssetManager manager){
        this.manager = manager;
    }

    public void loadText(InputStream in) throws IOException{
        Scanner scan = new Scanner(in);
        while (scan.hasNext()){
            String cmd = scan.next();
            if (cmd.equals("LOADER")){
                String loaderClass = scan.next();
                String colon = scan.next();
                if (!colon.equals(":")){
                    throw new IOException("Expected ':', got '"+colon+"'");
                }
                String extensionsList = scan.nextLine();
                String[] extensions = extensionsList.split(",");
                for (int i = 0; i < extensions.length; i++){
                    extensions[i] = extensions[i].trim();
                }
                manager.registerLoader(loaderClass, extensions);
            }else if (cmd.equals("LOCATOR")){
                String rootPath = scan.next();
                String locatorClass = scan.nextLine().trim();
                manager.registerLocator(rootPath, locatorClass);
            }else{
                throw new IOException("Expected command, got '"+cmd+"'");
            }
        }
    }

    private static final String readString(DataInput dataIn) throws IOException{
        int length = dataIn.readUnsignedShort();
        char[] chrs = new char[length];
        for (int i = 0; i < length; i++){
            chrs[i] = (char) dataIn.readUnsignedByte();
        }
        return String.valueOf(chrs);
    }

    public void loadBinary(DataInput dataIn) throws IOException{
        // read signature and version

        // how many locator entries?
        int locatorEntries = dataIn.readUnsignedShort();
        for (int i = 0; i < locatorEntries; i++){
            String locatorClazz = readString(dataIn);
            String rootPath = readString(dataIn);
            manager.registerLocator(rootPath, locatorClazz);
        }

        int loaderEntries = dataIn.readUnsignedShort();
        for (int i = 0; i < loaderEntries; i++){
            String loaderClazz = readString(dataIn);
            int numExtensions = dataIn.readUnsignedByte();
            String[] extensions = new String[numExtensions];
            for (int j = 0; j < numExtensions; j++){
                extensions[j] = readString(dataIn);
            }

            manager.registerLoader(loaderClazz, extensions);
        }
    }

}
