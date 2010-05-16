package com.jme3.input.dummy;

import com.jme3.input.KeyInput;

public class DummyKeyInput extends DummyInput implements KeyInput {

    public int getKeyCount() {
        if (!inited)
            throw new IllegalStateException("Input not initialized.");

        return 0;
    }

}
