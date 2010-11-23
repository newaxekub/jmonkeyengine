/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.gde.materials.multiview.widgets;

import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.materials.MaterialProperty;

/**
 *
 * @author normenhansen
 */
public class WidgetFactory {

    public static MaterialPropertyWidget getWidget(MaterialProperty prop, ProjectAssetManager manager){
        MaterialPropertyWidget widget;
        //TODO: remove startswith
        if("Texture2D".equals(prop.getType())){
            widget=new SelectionPanel();
            ((SelectionPanel)widget).setSelectionList(manager.getTextures());
            widget.setProperty(prop);
            return widget;
        }
        else if("Boolean".equals(prop.getType())){
            widget=new BooleanPanel();
            widget.setProperty(prop);
            return widget;
        }
        else if("Color".equals(prop.getType())){
            widget=new ColorPanel();
            widget.setProperty(prop);
            return widget;
        }
        widget = new TextPanel();
        widget.setProperty(prop);
        return widget;
    }

}
