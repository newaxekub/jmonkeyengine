package com.jme3.ui;

/**
 * A <code>UIElement</code> represents a single user interface component.
 * UIElements can be buttons, labels, backgrounds and other ui widgets.
 *
 * @author Momoko_Fan
 */
public interface UIElement {
    public void setZOrder(int zOrder);
    public int getZOrder();
}
