/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.gde.terraineditor;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

/**
 * A custom toggle button group that allows you to deselect
 * a button that is already selected.
 * 
 * @author bowens
 */
public class ToggleButtonGroup extends ButtonGroup {
    private ButtonModel modifiedSelection;

    public void add(AbstractButton b) {
        if (b == null) {
            return;
        }
        buttons.addElement(b);

        if (b.isSelected()) {
            if (modifiedSelection == null) {
                modifiedSelection = b.getModel();
            } else {
                b.setSelected(false);
            }
        }

        b.getModel().setGroup(this);
    }

    public void remove(AbstractButton b) {
        if (b == null) {
            return;
        }
        buttons.removeElement(b);
        if (b.getModel() == modifiedSelection) {
            modifiedSelection = null;
        }
        b.getModel().setGroup(null);
    }

    public ButtonModel getSelection() {
        return modifiedSelection;
    }

    public void setSelected(ButtonModel m, boolean b) {
        if (!b && m == modifiedSelection) {
            modifiedSelection = null;
            return;
        }
        if (b && m != null && m != modifiedSelection) {
            ButtonModel oldSelection = modifiedSelection;
            modifiedSelection = m;
            if (oldSelection != null) {
                oldSelection.setSelected(false);
            }
            m.setSelected(true);
        }
    }

    public boolean isSelected(ButtonModel m) {
        return (m == modifiedSelection);
    }
}
