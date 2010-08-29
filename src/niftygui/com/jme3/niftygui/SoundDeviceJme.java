package com.jme3.niftygui;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioRenderer;
import de.lessvoid.nifty.sound.SoundSystem;
import de.lessvoid.nifty.spi.sound.SoundDevice;
import de.lessvoid.nifty.spi.sound.SoundHandle;

public class SoundDeviceJme implements SoundDevice {

    protected AssetManager assetManager;
    protected AudioRenderer ar;

    public SoundDeviceJme(AssetManager assetManager, AudioRenderer ar){
        this.assetManager = assetManager;
        this.ar = ar;
    }

    public SoundHandle loadSound(SoundSystem soundSystem, String filename) {
        AudioNode an = new AudioNode(assetManager, filename, false);
        an.setPositional(false);
        return new SoundHandleJme(ar, an);
    }

    public SoundHandle loadMusic(SoundSystem soundSystem, String filename) {
        AudioNode an = new AudioNode(assetManager, filename, true);
        an.setPositional(false);
        return new SoundHandleJme(ar, an);
    }

    public void update(int delta) {
    }
    
}
