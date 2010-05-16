package com.jme3.audio;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import java.io.IOException;

public class Filter implements Savable {

    protected int id = -1;
    protected boolean updateNeeded = true;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void clearUpdateNeeded(){
        this.updateNeeded = false;
    }

    public boolean isUpdateNeeded() {
        return updateNeeded;
    }

    public void write(JmeExporter ex) throws IOException {
        // nothing to save
    }

    public void read(JmeImporter im) throws IOException {
        // nothing to read
    }

}
