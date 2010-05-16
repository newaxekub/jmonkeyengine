package com.jme3.asset.pack;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PackerInputStream extends FilterInputStream {

    private ProgressListener listener;

    public PackerInputStream(InputStream in, ProgressListener listener){
        super(in);
        this.listener = listener;
    }

    public int read(byte[] buf, int off, int len) throws IOException{
        int read = super.read(buf, off, len);
        if (read > 0)
            listener.onProgress(read);
        return read;
    }

    public int read(byte[] buf) throws IOException{
        int read = super.read(buf);
        if (read > 0)
            listener.onProgress(read);
        return read;
    }

    @Override
    public int read() throws IOException{
        int read = super.read();
        if (read != -1)
            listener.onProgress(1);
        return read;
    }

    public long skip(long bytes) throws IOException{
        long skipped = super.skip(bytes);
        if (skipped > 0)
            listener.onProgress((int)skipped);
        return skipped;
    }

}
