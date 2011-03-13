/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.terraineditor.sky;

import com.jme3.asset.AssetManager;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.sceneexplorer.nodes.actions.AbstractNewSpatialAction;
import com.jme3.gde.core.sceneexplorer.nodes.actions.NewSpatialAction;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import java.awt.Component;
import java.awt.Dialog;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;

/**
 *
 * @author normenhansen
 */
@org.openide.util.lookup.ServiceProvider(service = NewSpatialAction.class)
public class AddSkyboxAction extends AbstractNewSpatialAction {

    private WizardDescriptor.Panel[] panels;

    public AddSkyboxAction() {
        name = "Skybox..";
    }

    @Override
    protected Spatial doCreateSpatial(Node parent) {
        try {
            final WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
            java.awt.EventQueue.invokeAndWait(new Runnable() {

                public void run() {
                    // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
                    wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
                    wizardDescriptor.setTitle("Skybox Wizard");
                    Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
                    dialog.setVisible(true);
                    dialog.toFront();
                }
            });
            boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
            if (!cancelled) {
                return generateSkybox(wizardDescriptor);
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public Spatial generateSkybox(WizardDescriptor wiz) {
        AssetManager assetManager = SceneApplication.getApplication().getAssetManager();
        if ((Boolean) wiz.getProperty("multipleTextures")) {
            Texture south = (Texture) wiz.getProperty("textureSouth");
            Texture north = (Texture) wiz.getProperty("textureNorth");
            Texture east = (Texture) wiz.getProperty("textureEast");
            Texture west = (Texture) wiz.getProperty("textureWest");
            Texture top = (Texture) wiz.getProperty("textureTop");
            Texture bottom = (Texture) wiz.getProperty("textureBottom");
            Vector3f normalScale = (Vector3f) wiz.getProperty("normalScale");
            return SkyFactory.createSky(assetManager, west, east, north, south, top, bottom, normalScale);
        } else {
            Texture textureSingle = (Texture) wiz.getProperty("textureSingle");
            Vector3f normalScale = (Vector3f) wiz.getProperty("normalScale");
            boolean useSpheremap = (Boolean) wiz.getProperty("useSpheremap");
            return SkyFactory.createSky(assetManager, textureSingle, normalScale, useSpheremap);
        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                        new SkyboxWizardPanel1(),
                        new SkyboxWizardPanel2()
                    };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return panels;
    }
}
