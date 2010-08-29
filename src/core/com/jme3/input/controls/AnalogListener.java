package com.jme3.input.controls;

public interface AnalogListener extends InputListener {
    public void onAnalog(String name, float value, float tpf);
}
