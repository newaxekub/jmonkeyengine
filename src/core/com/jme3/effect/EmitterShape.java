package com.jme3.effect;

import com.jme3.export.Savable;
import com.jme3.math.Vector3f;

public interface EmitterShape extends Savable {
    public void getRandomPoint(Vector3f store);
}
