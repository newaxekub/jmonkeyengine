package com.jme3.audio;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>AudioStream</code> is an implementation of AudioData that
 * acquires the audio from an InputStream. Audio can be streamed
 * from network, hard drive etc. It is assumed the data coming
 * from the input stream is uncompressed.
 *
 * @author Kirill
 */
public class AudioStream extends AudioData implements Closeable{

    protected InputStream in;
    private float duration = -1f;
    private boolean open = false;
    private int[] ids;

    public AudioStream(){
    }

    public void updateData(InputStream in, float duration){
        if (id != -1 || this.in != null)
            throw new IllegalStateException("Data already set!");

        this.in = in;
        this.duration = duration;
        open = true;
    }

    /**
     * Reads samples from the stream. The format of the data
     * depends on the getSampleRate(), getChannels(), getBitsPerSample()
     * values.
     *
     * @param buf Buffer where to read the samples
     * @param offset The offset in the buffer where to read samples
     * @param length The length inside the buffer where to read samples
     * @return number of bytes read.
     */
    public int readSamples(byte[] buf, int offset, int length){
        if (!open)
            return -1;

        try{
            return in.read(buf, offset, length);
        }catch (IOException ex){
            return -1;
        }
    }

    /**
     * Reads samples from the stream.
     *
     * @see AudioStream#readSamples(byte[], int, int)
     * @param buf Buffer where to read the samples
     * @return number of bytes read.
     */
    public int readSamples(byte[] buf){
        return readSamples(buf, 0, buf.length);
    }

    public float getDuration(){
        return duration;
    }

    @Override
    public int getId(){
        throw new RuntimeException("Don't use getId() on streams");
    }

    public void setId(int id){
        throw new RuntimeException("Don't use setId() on streams");
    }

    public void initIds(int count){
        ids = new int[count];
    }

    public int getId(int index){
        return ids[index];
    }

    public void setId(int index, int id){
        ids[index] = id;
    }

    public int[] getIds(){
        return ids;
    }

    public void setIds(int[] ids){
        this.ids = ids;
    }

    @Override
    public DataType getDataType() {
        return DataType.Stream;
    }

    @Override
    public void resetObject() {
        id = -1;
        ids = null;
        setUpdateNeeded();
    }

    @Override
    public void deleteObject(AudioRenderer r) {
        r.deleteAudioData(this);
    }

    /**
     * @return Whether the stream is open or not. Reading from a closed
     * stream will always return eof.
     */
    public boolean isOpen(){
        return open;
    }

    /**
     * Closes the stream, releasing all data relating to it. Reading
     * from the stream will return eof.
     * @throws IOException
     */
    public void close() {
        if (in != null && open){
            try{
                in.close();
            }catch (IOException ex){
            }
            open = false;
        }else{
            throw new RuntimeException("AudioStream is already closed!");
        }
    }

}
