package jme3test.asset;

import com.jme3.asset.AssetCache;
import com.jme3.asset.AssetKey;

public class TestAssetCache {

    private static class MyAsset {
        
        private String name;
        private byte[] bytes = new byte[100];

        public MyAsset(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
        
    } 
    
//    private static final long memoryUsage(){
//        return Runtime.getRuntime().
//    }
//
    public static void main(String[] args){
        AssetCache cache = new AssetCache();

        System.gc();
        System.gc();
        System.gc();
        System.gc();

        long startMem = Runtime.getRuntime().freeMemory();

        for (int i = 0; i < 10000; i++){
            MyAsset asset = new MyAsset("asset"+i);
            AssetKey key = new AssetKey(asset.getName());
        }

        long endMem = Runtime.getRuntime().freeMemory();
        System.out.println("No cache    diff:\t"+(startMem-endMem));

        System.gc();
        System.gc();
        System.gc();
        System.gc();

        endMem = Runtime.getRuntime().freeMemory();
        System.out.println("No cache gc diff:\t"+(startMem-endMem));
        startMem = endMem;

        for (int i = 0; i < 10000; i++){
            MyAsset asset = new MyAsset("asset"+i);
            AssetKey key = new AssetKey(asset.getName());
            cache.addToCache(key, asset);
        }

        endMem = Runtime.getRuntime().freeMemory();
        System.out.println("Cache       diff:\t"+(startMem-endMem));

        System.gc();
        System.gc();
        System.gc();
        System.gc();

        endMem = Runtime.getRuntime().freeMemory();
        System.out.println("Cache gc    diff:\t"+(startMem-endMem));
//        System.out.println("Estimated usage: "+)
    }

}
