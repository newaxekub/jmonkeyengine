package com.jme3.niftygui;

import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioNode.Status;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.AudioStream;
import de.lessvoid.nifty.spi.sound.SoundHandle;

public class SoundHandleJme implements SoundHandle {

    private AudioNode node;
    private AudioRenderer ar;

    public SoundHandleJme(AudioRenderer ar, AudioNode node){
        this.ar = ar;
        this.node = node;
    }

    public void play() {
        if (node.getAudioData() instanceof AudioStream){
            ar.playSource(node);
        }else{
            ar.playSourceInstance(node);
        }
    }

    public void stop() {
        ar.stopSource(node);
    }

    public void setVolume(float f) {
        node.setVolume(f);
    }

    public float getVolume() {
        return node.getVolume();
    }

    public boolean isPlaying() {
        return node.getStatus() == Status.Playing;
    }

    public void dispose() {
    }
}
