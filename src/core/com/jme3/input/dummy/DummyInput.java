package com.jme3.input.dummy;

import com.jme3.input.Input;
import com.jme3.input.MouseInput;
import com.jme3.input.RawInputListener;

public class DummyInput implements Input {

    protected boolean inited = false;

    public void initialize() {
        if (inited)
            throw new IllegalStateException("Input already initialized.");

        inited = true;
    }

    public void update() {
        if (!inited)
            throw new IllegalStateException("Input not initialized.");
    }

    public void destroy() {
        if (!inited)
            throw new IllegalStateException("Input not initialized.");

        inited = false;
    }

    public boolean isInitialized() {
        return inited;
    }

    public void setInputListener(RawInputListener listener) {
    }

}
