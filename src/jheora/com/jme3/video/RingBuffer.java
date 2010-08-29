package com.jme3.video;

public final class RingBuffer {
    
    private final int bufSize;
    private final byte[] buf;
    private final Object lock = new Object();

    private volatile int writePos;
    private volatile int readPos;
    
//    private volatile boolean eof;
    private volatile long totalRead = 0,
                          totalWritten = 0;

    public RingBuffer(int size) {
        bufSize = size;
        buf = new byte[size];
    }

    public int remainingForWrite() {
        if (writePos < readPos) return readPos - writePos - 1;
        if (writePos == readPos) return bufSize - 1;
        return bufSize - (writePos - readPos) - 1;
    }

    public void write(byte[] data, int offset, int length) {
        if (length == 0) return;

        synchronized (lock) {
            while (remainingForWrite() < length) {
                System.out.println("Warning: Audio decoder starved, waiting for player");
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                }
            }

            // copy data
            if (writePos >= readPos) {
                int l = Math.min(length, bufSize - writePos);
                System.arraycopy(data, offset, buf, writePos, l);
                writePos += l;
                if (writePos >= bufSize) writePos = 0;
                if (length > l) write(data, offset + l, length - l);
            } else {
                int l = Math.min(length, readPos - writePos - 1);
                System.arraycopy(data, offset, buf, writePos, l);
                writePos += l;
                if (writePos >= bufSize) writePos = 0;
            }

            totalWritten += length;
        }
    }

    public int remainingForRead() {
        if (writePos < readPos) return bufSize - (readPos - writePos);
        if (writePos == readPos) return 0;
        return writePos - readPos;
    }

    public int skip(int amount){
        if (amount <= 0) return 0;
        int dataLen = 0;

        synchronized (lock){
            if (remainingForRead() <= 0)
                return 0;

            amount = Math.min(amount, remainingForRead());

            // copy data
            if (readPos < writePos) {
                int l = Math.min(amount, writePos - readPos);
                readPos += l;
                if (readPos >= bufSize) readPos = 0;
                dataLen = l;
            } else {
                int l = Math.min(amount, bufSize - readPos);
                readPos += l;
                if (readPos >= bufSize) readPos = 0;
                dataLen = l;
                if (amount > l) dataLen += skip(amount - l);
            }
            lock.notifyAll();
        }

        totalRead += dataLen;
        return dataLen;
    }

    public int read(byte[] data, int offset, int len) {
        if (len == 0) return 0;
        int dataLen = 0;

        synchronized (lock) {
            // see if we have enough data
            if (remainingForRead() <= 0){
                System.out.println("Warning: Audio starved. Not enough data.");
                return 0;
            }
            
            len = Math.min(len, remainingForRead());

            // copy data
            if (readPos < writePos) {
                int l = Math.min(len, writePos - readPos);
                System.arraycopy(buf, readPos, data, offset, l);
                readPos += l;
                if (readPos >= bufSize) readPos = 0;
                dataLen = l;
            } else {
                int l = Math.min(len, bufSize - readPos);
                System.arraycopy(buf, readPos, data, offset, l);
                readPos += l;
                if (readPos >= bufSize) readPos = 0;
                dataLen = l;
                if (len > l) dataLen += read(data, offset + l, len - l);
            }
            lock.notifyAll();
        }

        totalRead += dataLen;
        return dataLen;
    }

    public long getTotalRead(){
        return totalRead;
    }

    public long getTotalWritten(){
        return totalWritten;
    }

//    public boolean isEOF() {
//        return eof;
//    }
//
//    public void setEOF(boolean eof) {
//        this.eof = eof;
//    }

}
