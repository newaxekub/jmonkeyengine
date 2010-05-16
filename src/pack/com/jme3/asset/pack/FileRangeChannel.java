package com.jme3.asset.pack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class FileRangeChannel implements ReadableByteChannel {

    private FileChannel channel;
    private long position;
    private long limit;

    public FileRangeChannel(FileChannel channel, long position, int length){
        if (channel == null)
            throw new NullPointerException();

        if (position < 0 || length <= 0)
            throw new IllegalArgumentException();

        this.channel = channel;
        this.position = position;
        this.limit = position + length;
    }

    public int read(ByteBuffer dst) throws IOException {
        if (!channel.isOpen())
            throw new ClosedChannelException();

        if (dst == null || !dst.hasRemaining())
            return 0;

        int prevLim = dst.limit();
        int toRead = (int) Math.min(dst.remaining(), limit - position);
        dst.limit(dst.position() + toRead);
        int read = channel.read(dst, position);
        position += read;
        dst.limit(prevLim);

        return read;
    }

    public boolean isOpen() {
        return channel.isOpen();
    }

    public void close() throws IOException {
        channel.close();
    }

}
