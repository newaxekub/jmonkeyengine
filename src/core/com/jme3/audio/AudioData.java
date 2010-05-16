package com.jme3.audio;

/**
 * <code>AudioData</code> is an abstract representation
 * of audio data. There are two ways to handle audio data, short audio files
 * are to be stored entirely in memory, while long audio files (music) is
 * streamed from the hard drive as it is played.
 *
 * @author Kirill
 */
public abstract class AudioData extends ALObject {

    protected int sampleRate;
    protected int channels;
    protected int bitsPerSample;

    public enum DataType {
        Buffer,
        Stream
    }

    /**
     * @return The data type, either <code>Buffer</code> or <code>Stream</code>.
     */
    public abstract DataType getDataType();

    /**
     * @return the duration in seconds of the audio clip.
     */
    public abstract float getDuration();

    /**
     * @return Bits per single sample from a channel.
     */
    public int getBitsPerSample() {
        return bitsPerSample;
    }

    /**
     * @return Number of channels. 1 for mono, 2 for stereo, etc.
     */
    public int getChannels() {
        return channels;
    }

    /**
     * @return The sample rate, or how many samples per second.
     */
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * Setup the format of the audio data.
     * @param channels # of channels, 1 = mono, 2 = stereo
     * @param bitsPerSample Bits per sample, e.g 8 bits, 16 bits.
     * @param sampleRate Sample rate, 44100, 22050, etc.
     */
    public void setupFormat(int channels, int bitsPerSample, int sampleRate){
        if (id != -1)
            throw new IllegalStateException("Already set up");

        this.channels = channels;
        this.bitsPerSample = bitsPerSample;
        this.sampleRate = sampleRate;
    }

}
