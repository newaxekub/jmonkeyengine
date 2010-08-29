package com.jme3.audio.plugins;

import com.jme3.asset.AssetInfo;
import com.jme3.audio.AudioBuffer;
import com.jme3.audio.AudioStream;
import com.jme3.asset.AssetLoader;
import com.jme3.audio.AudioKey;
import com.jme3.util.BufferUtils;
import com.jme3.util.TempVars;
import de.jarnbjo.ogg.EndOfOggStreamException;
import de.jarnbjo.ogg.LogicalOggStream;
import de.jarnbjo.ogg.PhysicalOggStream;
import de.jarnbjo.vorbis.IdentificationHeader;
import de.jarnbjo.vorbis.VorbisStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;

public class OGGLoader implements AssetLoader {

//    private static int BLOCK_SIZE = 4096*64;

    private PhysicalOggStream oggStream;
    private LogicalOggStream loStream;
    private VorbisStream vorbisStream;

//    private CommentHeader commentHdr;
    private IdentificationHeader streamHdr;

    private static class JOggInputStream extends InputStream {

        private boolean endOfStream = false;
        private final VorbisStream vs;

        public JOggInputStream(VorbisStream vs){
            this.vs = vs;
        }

        @Override
        public int read() throws IOException {
            return 0;
        }

        @Override
        public int read(byte[] buf) throws IOException{
            return read(buf,0,buf.length);
        }

        @Override
        public int read(byte[] buf, int offset, int length) throws IOException{
            if (endOfStream)
                return -1;

            int bytesRead = 0, cnt = 0;
            assert length % 2 == 0; // read buffer should be even
            
            while (bytesRead < buf.length) {
                if ((cnt = vs.readPcm(buf, offset + bytesRead, buf.length - bytesRead)) <= 0) {
                    endOfStream = true;
                    break;
                }
                bytesRead += cnt;
            }

            swapBytes(buf, offset, bytesRead);
            return bytesRead;

        }

        @Override
        public void close() throws IOException{
            vs.close();
        }

    }

    private ByteBuffer readToBuffer() throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buf = new byte[512];
        int read = 0;

        try {
            while ( (read = vorbisStream.readPcm(buf, 0, buf.length)) > 0){
                baos.write(buf, 0, read);
            }
        } catch (EndOfOggStreamException ex){
        }

        byte[] dataBytes = baos.toByteArray();
        swapBytes(dataBytes, 0, dataBytes.length);
        ByteBuffer data = BufferUtils.createByteBuffer(dataBytes.length);
        data.put(dataBytes).flip();

        vorbisStream.close();
        loStream.close();
        oggStream.close();

        return data;
    }

    private static final void swapBytes(byte[] b, int off, int len) {
        byte tempByte;
        for (int i = off; i < (off+len); i+=2) {
            tempByte = b[i];
            b[i] = b[i+1];
            b[i+1] = tempByte;
        }
    }

    private InputStream readToStream(){
        return new JOggInputStream(vorbisStream);
    }

    public Object load(AssetInfo info) throws IOException {
        InputStream in = info.openStream();
        oggStream = new UncachedOggStream(in);

        Collection<LogicalOggStream> streams = oggStream.getLogicalStreams();
        loStream = streams.iterator().next();

//        if (loStream == null){
//            throw new IOException("OGG File does not contain vorbis audio stream");
//        }

        vorbisStream = new VorbisStream(loStream);
        streamHdr = vorbisStream.getIdentificationHeader();
//        commentHdr = vorbisStream.getCommentHeader();

        boolean readStream = ((AudioKey)info.getKey()).isStream();
        
        if (!readStream){
            AudioBuffer audioBuffer = new AudioBuffer();
            audioBuffer.setupFormat(streamHdr.getChannels(), 16, streamHdr.getSampleRate());
            audioBuffer.updateData(readToBuffer());
            return audioBuffer;
        }else{
            AudioStream audioStream = new AudioStream();
            audioStream.setupFormat(streamHdr.getChannels(), 16, streamHdr.getSampleRate());
            audioStream.updateData(readToStream(), -1);
            return audioStream;
        }
    }

}
