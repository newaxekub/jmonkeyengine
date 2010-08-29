package com.jme3.input.controls;

public class KeyTrigger implements Trigger {

    private final int keyCode;

    public KeyTrigger(int keyCode){
        this.keyCode = keyCode;
    }

    public String getName() {
        return "KeyCode " + keyCode;
    }

    public int getKeyCode(){
        return keyCode;
    }

    public static final int keyHash(int keyCode){
        return keyCode & 0xff;
    }

    @Override
    public int hashCode(){
        return keyHash(keyCode);
    }

}
