package com.jme3.input.event;

public class KeyInputEvent extends InputEvent {

    private int keyCode;
    private char keyChar;
    private boolean pressed;
    private boolean repeating;

    public KeyInputEvent(int keyCode, char keyChar, boolean pressed, boolean repeating) {
        this.keyCode = keyCode;
        this.keyChar = keyChar;
        this.pressed = pressed;
        this.repeating = repeating;
    }

    public char getKeyChar() {
        return keyChar;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public boolean isPressed() {
        return pressed;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public boolean isReleased() {
        return !pressed;
    }

    public String toString(){
        String str = "Key(CODE="+keyCode;
        if (keyChar != '\0')
            str = str + ", CHAR=" + keyChar;
            
        if (repeating){
            return str + ", REPEATING)";
        }else if (pressed){
            return str + ", PRESSED)";
        }else{
            return str + ", RELEASED)";
        }
    }
}
