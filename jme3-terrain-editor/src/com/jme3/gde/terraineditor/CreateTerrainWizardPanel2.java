/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.terraineditor;

import com.jme3.asset.TextureKey;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jme3tools.converters.ImageToAwt;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

public class CreateTerrainWizardPanel2 implements WizardDescriptor.Panel {

    private int terrainTotalSize;
    private AbstractHeightMap heightmap;

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new CreateTerrainVisualPanel2();
        }
        return component;
    }

    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }

    // If it is always OK to press Next or Finish, then:
    public boolean isValid() {
        CreateTerrainVisualPanel2 comp = (CreateTerrainVisualPanel2) getComponent();

        if ("Image Based".equals(comp.getHeightmapTypeComboBox().getSelectedItem())) {
            //new File(comp.getImageBrowseTextField().getText())
        }

        return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    }

    /*public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }*/
    
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
    public final void addChangeListener(ChangeListener l) {
    synchronized (listeners) {
    listeners.add(l);
    }
    }
    public final void removeChangeListener(ChangeListener l) {
    synchronized (listeners) {
    listeners.remove(l);
    }
    }
    protected final void fireChangeEvent() {
    Iterator<ChangeListener> it;
    synchronized (listeners) {
    it = new HashSet<ChangeListener>(listeners).iterator();
    }
    ChangeEvent ev = new ChangeEvent(this);
    while (it.hasNext()) {
    it.next().stateChanged(ev);
    }
    }
    

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        WizardDescriptor wiz = (WizardDescriptor) settings;
        terrainTotalSize = (Integer)wiz.getProperty("totalSize");
    }

    public void storeSettings(Object settings) {

        CreateTerrainVisualPanel2 comp = (CreateTerrainVisualPanel2) getComponent();

        if ("Flat".equals(comp.getHeightmapTypeComboBox().getSelectedItem()) ) {
            heightmap = new FlatHeightmap(terrainTotalSize);
        }
        else if ("Image Based".equals(comp.getHeightmapTypeComboBox().getSelectedItem()) ) {
            
            BufferedImage bi = null;
            try {
                bi = ImageIO.read(new File(comp.getImageBrowseTextField().getText()));
            } catch (IOException e) {
                e.printStackTrace();
            }
                ImageBasedHeightMap ibhm = new ImageBasedHeightMap(bi, 1f);

            heightmap = ibhm;
        }
        else if ("Hill".equals(comp.getHeightmapTypeComboBox().getSelectedItem()) ) {
            int iterations = new Integer(comp.getHillIterationsTextField().getText());
            byte flattening = new Byte(comp.getHillFlatteningTextField().getText());
            float min = new Float(comp.getHillMinRadiusTextField().getText());
            float max = new Float(comp.getHillMaxRadiusTextField().getText());
            try {
                heightmap = new HillHeightMap(terrainTotalSize, iterations, min, max, flattening);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        WizardDescriptor wiz = (WizardDescriptor) settings;
        wiz.putProperty("abstractHeightMap", heightmap);
    }
}
