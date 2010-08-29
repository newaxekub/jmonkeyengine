package com.jme3.audio;

import com.jme3.audio.AudioData.DataType;
import java.nio.ByteBuffer;

/**
 * An <code>AudioBuffer</code> is an implementation of AudioData
 * where the audio is buffered (stored in memory). All parts of it
 * are accessable at any time. <br/>
 * AudioBuffers are useful for short sounds, like effects, etc.
 *
 * @author Kirill
 */
public class AudioBuffer extends AudioData {

    /**
     * The audio data buffer. Should be direct and native ordered.
     */
    protected ByteBuffer audioData;

    public AudioBuffer(){
    }

    public DataType getDataType() {
        return DataType.Buffer;
    }

    /**
     * @return The duratiion of the audio in seconds. It is expected
     * that audio is uncompressed.
     */
    public float getDuration(){
        int bytesPerSec = (bitsPerSample / 8) * channels * sampleRate;
        if (audioData != null)
            return (float) audioData.capacity() / bytesPerSec;
        else
            return Float.NaN; // unknown
    }

    @Override
    public String toString(){
        return getClass().getSimpleName() +
               "[id="+id+", ch="+channels+", bits="+bitsPerSample +
               ", rate="+sampleRate+", duration="+getDuration()+"]";
    }

    /**
     * Update the data in the buffer with new data.
     * @param data
     */
    public void updateData(ByteBuffer data){
        this.audioData = data;
        updateNeeded = true;
    }

    /**
     * @return The buffered audio data.
     */
    public ByteBuffer getData(){
        return audioData;
    }

    public void resetObject() {
        id = -1;
        setUpdateNeeded();
    }

    public void deleteObject(AudioRenderer ar) {
        ar.deleteAudioData(this);
    }

}
