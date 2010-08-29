package com.jme3.input.controls;

public interface ActionListener extends InputListener {
    public void onAction(String name, boolean value, float tpf);
}
