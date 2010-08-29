/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.scene.mesh;

import java.nio.Buffer;

/**
 *
 * @author lex
 */
public abstract class IndexBuffer {
    public abstract int get(int i);
    public abstract void put(int i, int value);
    public abstract int size();
    public abstract Buffer getBuffer();
}
