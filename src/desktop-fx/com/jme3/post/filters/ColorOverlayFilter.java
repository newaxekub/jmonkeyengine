/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.post.filters;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.post.Filter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;

/** 
 *
 * @author nehon
 */
public class ColorOverlayFilter extends Filter {

    private ColorRGBA color = ColorRGBA.White;

    public ColorOverlayFilter() {
        super("Color Overlay");
    }

    public ColorOverlayFilter(ColorRGBA color) {
        this();
        this.color = color;
    }

    @Override
    public Material getMaterial() {

        material.setColor("m_Color", color);
        return material;
    }

    public ColorRGBA getColor() {
        return color;
    }

    public void setColor(ColorRGBA color) {
        this.color = color;
    }

    @Override
    public void preRender(RenderManager renderManager, ViewPort viewPort) {
    }

    @Override
    public void initMaterial(AssetManager manager) {
        material = new Material(manager, "Common/MatDefs/Gui/Gui.j3md");
    }
}
