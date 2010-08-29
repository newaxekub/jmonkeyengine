package com.jme3.audio;

public abstract class ALObject {

    protected int id = -1;
    protected Object handleRef = null;
    protected boolean updateNeeded = true;

    public void setId(int id){
        if (this.id != -1)
            throw new IllegalStateException("ID has already been set for this AL object.");

        this.id = id;
    }

    public int getId(){
        return id;
    }

    public void setUpdateNeeded(){
        updateNeeded = true;
    }

    public void clearUpdateNeeded(){
        updateNeeded = false;
    }

    public boolean isUpdateNeeded(){
        return updateNeeded;
    }

    @Override
    public String toString(){
        return getClass().getSimpleName() + " " + Integer.toHexString(hashCode());
    }

    public abstract void resetObject();

    public abstract void deleteObject(AudioRenderer r);

}
