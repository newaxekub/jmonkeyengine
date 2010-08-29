package com.jme3.audio.plugins;

import de.jarnbjo.ogg.LogicalOggStream;
import de.jarnbjo.ogg.LogicalOggStreamImpl;
import de.jarnbjo.ogg.OggFormatException;
import de.jarnbjo.ogg.OggPage;
import de.jarnbjo.ogg.PhysicalOggStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Single-threaded physical ogg stream. Decodes audio in the same thread
 * that reads.
 */
public class UncachedOggStream implements PhysicalOggStream {

    private boolean closed = false;
    private boolean eos = false;
    private boolean bos = false;
    private InputStream sourceStream;
    private LinkedList<OggPage> pageCache = new LinkedList<OggPage>();
    private HashMap<Integer, LogicalOggStream> logicalStreams = new HashMap();

    public UncachedOggStream(InputStream in) throws OggFormatException, IOException {
        this.sourceStream = in;

        // read until beginning of stream
        while (!bos){
            readNextOggPage();
        }
    }

    private void readNextOggPage() throws IOException {
        OggPage op = OggPage.create(sourceStream);
        if (!op.isBos()){
            bos = true;
        }
        if (op.isEos()){
            eos = true;
        }

        LogicalOggStreamImpl los = (LogicalOggStreamImpl) getLogicalStream(op.getStreamSerialNumber());
        if (los == null){
            los = new LogicalOggStreamImpl(this, op.getStreamSerialNumber());
            logicalStreams.put(op.getStreamSerialNumber(), los);
            los.checkFormat(op);
        }

        pageCache.add(op);
    }

    public OggPage getOggPage(int index) throws IOException {
        if (eos){
            return null;
        }

        if (pageCache.size() == 0){
            readNextOggPage();
        }

        return pageCache.removeFirst();
    }

    private LogicalOggStream getLogicalStream(int serialNumber) {
        return logicalStreams.get(new Integer(serialNumber));
    }

    public Collection<LogicalOggStream> getLogicalStreams() {
        return logicalStreams.values();
    }

    public void setTime(long granulePosition) throws IOException {
    }

    public boolean isSeekable() {
        return false;
    }

    public boolean isOpen() {
        return !closed;
    }

    public void close() throws IOException {
        closed = true;
        sourceStream.close();
    }
}
