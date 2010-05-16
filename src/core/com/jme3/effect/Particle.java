package com.jme3.effect;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

public class Particle {

    final Vector3f velocity = new Vector3f();
    final Vector3f position = new Vector3f();
    final ColorRGBA color = new ColorRGBA(0,0,0,0);
    float size = 0f;
    float life;
    float startlife;
    float angle;
    float rotateSpeed;
    int imageIndex = 0;

}
