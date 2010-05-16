package com.jme3.asset.plugins;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;

public class HttpZipLocator implements AssetLocator {

    private static final Logger logger = Logger.getLogger(HttpZipLocator.class.getName());

    private URL zipUrl;
    private String rootPath = "";
    private int numEntries;
    private int tableOffset;
    private int tableLength;
    private Hashtable<String, ZipEntry2> entries;

    private static class ZipEntry2 {
        String name;
        int length;
        int offset;
        int compSize;
        long crc;
        boolean deflate;
    }

    private static final int get16(byte[] b, int off) {
	return  (b[off++] & 0xff) |
               ((b[off]   & 0xff) << 8);
    }

    private static final int get32(byte[] b, int off) {
	return  (b[off++] & 0xff) |
               ((b[off++] & 0xff) << 8) |
               ((b[off++] & 0xff) << 16) |
               ((b[off] & 0xff) << 24);
    }

    private static final long getu32(byte[] b, int off) throws IOException{
        return (b[off++]&0xff) |
              ((b[off++]&0xff) << 8) |
              ((b[off++]&0xff) << 16) |
             (((long)(b[off]&0xff)) << 24);
    }

    private static String getUTF8String(byte[] b, int off, int len) {
	// First, count the number of characters in the sequence
	int count = 0;
	int max = off + len;
	int i = off;
	while (i < max) {
	    int c = b[i++] & 0xff;
	    switch (c >> 4) {
	    case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
		// 0xxxxxxx
		count++;
		break;
	    case 12: case 13:
		// 110xxxxx 10xxxxxx
		if ((int)(b[i++] & 0xc0) != 0x80) {
		    throw new IllegalArgumentException();
		}
		count++;
		break;
	    case 14:
		// 1110xxxx 10xxxxxx 10xxxxxx
		if (((int)(b[i++] & 0xc0) != 0x80) ||
		    ((int)(b[i++] & 0xc0) != 0x80)) {
		    throw new IllegalArgumentException();
		}
		count++;
		break;
	    default:
		// 10xxxxxx, 1111xxxx
		throw new IllegalArgumentException();
	    }
	}
	if (i != max) {
	    throw new IllegalArgumentException();
	}
	// Now decode the characters...
	char[] cs = new char[count];
	i = 0;
	while (off < max) {
	    int c = b[off++] & 0xff;
	    switch (c >> 4) {
	    case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
		// 0xxxxxxx
		cs[i++] = (char)c;
		break;
	    case 12: case 13:
		// 110xxxxx 10xxxxxx
		cs[i++] = (char)(((c & 0x1f) << 6) | (b[off++] & 0x3f));
		break;
	    case 14:
		// 1110xxxx 10xxxxxx 10xxxxxx
		int t = (b[off++] & 0x3f) << 6;
		cs[i++] = (char)(((c & 0x0f) << 12) | t | (b[off++] & 0x3f));
		break;
	    default:
		// 10xxxxxx, 1111xxxx
		throw new IllegalArgumentException();
	    }
	}
	return new String(cs, 0, count);
    }

    private InputStream readData(int offset, int length) throws IOException{
        HttpURLConnection conn = (HttpURLConnection) zipUrl.openConnection();
        conn.setDoOutput(false);
        conn.setUseCaches(false);
        conn.setInstanceFollowRedirects(false);
        String range = "-";
        if (offset != Integer.MAX_VALUE){
            range = offset + range;
        }
        if (length != Integer.MAX_VALUE){
            if (offset != Integer.MAX_VALUE){
                range = range + (offset + length - 1);
            }else{
                range = range + length;
            }
        }

        conn.setRequestProperty("Range", "bytes=" + range);
        conn.connect();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL){
            return conn.getInputStream();
        }else if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
            throw new IOException("Your server does not support HTTP feature Content-Range. Please contact your server administrator.");
        }else{
            throw new IOException(conn.getResponseCode() + " " + conn.getResponseMessage());
        }
    }

    private int readTableEntry(byte[] table, int offset) throws IOException{
        if (get32(table, offset) != ZipEntry.CENSIG){
            throw new IOException("Central directory error, expected 'PK12'");
        }

        int nameLen = get16(table, offset + ZipEntry.CENNAM);
        int extraLen = get16(table, offset + ZipEntry.CENEXT);
        int commentLen = get16(table, offset + ZipEntry.CENCOM);
        int newOffset = offset + ZipEntry.CENHDR + nameLen + extraLen + commentLen;

        int flags = get16(table, offset + ZipEntry.CENFLG);
        if ((flags & 1) == 1){
            // ignore this entry, it uses encryption
            return newOffset;
        }
            
        int method = get16(table, offset + ZipEntry.CENHOW);
        if (method != ZipEntry.DEFLATED && method != ZipEntry.STORED){
            // ignore this entry, it uses unknown compression method
            return newOffset;
        }

        String name = getUTF8String(table, offset + ZipEntry.CENHDR, nameLen);
        if (name.charAt(name.length()-1) == '/'){
            // ignore this entry, it is directory node
            return newOffset;
        }

        ZipEntry2 entry = new ZipEntry2();
        entry.name     = name;
        entry.deflate  = (method == ZipEntry.DEFLATED);
        entry.crc      = getu32(table, offset + ZipEntry.CENCRC);
        entry.length   = get32(table, offset + ZipEntry.CENLEN);
        entry.compSize = get32(table, offset + ZipEntry.CENSIZ);
        entry.offset   = get32(table, offset + ZipEntry.CENOFF);

        // we want offset directly into file data ..
        // move the offset forward to skip the LOC header
        entry.offset += ZipEntry.LOCHDR + nameLen + extraLen;

        entries.put(entry.name, entry);
        
        return newOffset;
    }

    private void readCentralDirectory() throws IOException{
        InputStream in = readData(tableOffset, tableLength);
        byte[] header = new byte[tableLength];
        in.read(header);
        in.close();

        entries = new Hashtable<String, ZipEntry2>(numEntries);
        int offset = 0;
        for (int i = 0; i < numEntries; i++){
            offset = readTableEntry(header, offset);
        }
    }

    private void readEndHeader() throws IOException{
        InputStream in = readData(Integer.MAX_VALUE, ZipEntry.ENDHDR);
        byte[] header = new byte[ZipEntry.ENDHDR];
        in.read(header);
        in.close();

        if (get32(header, 0) != ZipEntry.ENDSIG){
            throw new IOException("End header error, expected 'PK56'");
        }

        numEntries  = get16(header, ZipEntry.ENDTOT);
        tableLength = get32(header, ZipEntry.ENDSIZ);
        tableOffset = get32(header, ZipEntry.ENDOFF);
    }

    public void load(URL url) throws IOException {
        if (!url.getProtocol().equals("http"))
            throw new UnsupportedOperationException();

        zipUrl = url;
        readEndHeader();
        readCentralDirectory();
    }

    private InputStream openStream(ZipEntry2 entry) throws IOException{
        InputStream in = readData(entry.offset, entry.compSize);
        if (entry.deflate){
            return new InflaterInputStream(in, new Inflater(true));
        }
        return in;
    }

    public InputStream openStream(String name) throws IOException{
        ZipEntry2 entry = entries.get(name);
        if (entry == null)
            throw new RuntimeException("Entry not found: "+name);

        return openStream(entry);
    }

    public void setRootPath(String path){
        if (!rootPath.equals(path)){
            rootPath = path;
            try {
                load(new URL(path));
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Failed to set root path "+path, ex);
            }
        }
    }

    public AssetInfo locate(AssetManager manager, AssetKey key){
        final ZipEntry2 entry = entries.get(key.getName());
        if (entry == null)
            return null;

        return new AssetInfo(manager, key){
            @Override
            public InputStream openStream() {
                try {
                    return HttpZipLocator.this.openStream(entry);
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "Error retrieving "+entry.name, ex);
                    return null;
                }
            }
        };
    }

}
