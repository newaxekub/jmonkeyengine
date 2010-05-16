package com.jme3.input.dummy;

import com.jme3.input.MouseInput;

public class DummyMouseInput extends DummyInput implements MouseInput {

    public void setCursorVisible(boolean visible) {
        if (!inited)
            throw new IllegalStateException("Input not initialized.");
    }

    public int getButtonCount() {
        return 0;
    }

}
