/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LwjglAppletCustomizerPanel.java
 *
 * Created on 11.11.2010, 16:56:53
 */
package com.jme3.gde.desktop.executables;

import com.jme3.gde.core.j2seproject.ProjectExtensionProperties;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.util.HelpCtx;

/**
 *
 * @author normenhansen
 */
public class DesktopExeCustomizerPanel extends javax.swing.JPanel implements ActionListener {

    private ProjectExtensionProperties properties;

    /** Creates new form LwjglAppletCustomizerPanel */
    public DesktopExeCustomizerPanel(ProjectExtensionProperties properties) {
        this.properties = properties;
        initComponents();
        loadProperties();
        HelpCtx.setHelpIDString(this, "jme3.jmonkeyplatform.model_loader_and_viewer");
    }

    private void loadProperties() {
        String str = properties.getProperty("launch4j.exe.enabled");
        if ("true".equals(str)) {
            jCheckBox1.setSelected(true);
        } else {
            jCheckBox1.setSelected(false);
        }
        String str2 = properties.getProperty("mac.app.enabled");
        if ("true".equals(str2)) {
            jCheckBox2.setSelected(true);
        } else {
            jCheckBox2.setSelected(false);
        }
        String str3 = properties.getProperty("linux.launcher.enabled");
        if ("true".equals(str3)) {
            jCheckBox3.setSelected(true);
        } else {
            jCheckBox3.setSelected(false);
        }
    }

    private void saveProperties() {
        if (jCheckBox1.isSelected()) {
            properties.setProperty("launch4j.exe.enabled", "true");
        } else {
            properties.setProperty("launch4j.exe.enabled", "");
        }
        if (jCheckBox2.isSelected()) {
            properties.setProperty("mac.app.enabled", "true");
        } else {
            properties.setProperty("mac.app.enabled", "");
        }
        if (jCheckBox3.isSelected()) {
            properties.setProperty("linux.launcher.enabled", "true");
        } else {
            properties.setProperty("linux.launcher.enabled", "");
        }
    }

    public void actionPerformed(ActionEvent e) {
        saveProperties();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();

        jCheckBox1.setText(org.openide.util.NbBundle.getMessage(DesktopExeCustomizerPanel.class, "DesktopExeCustomizerPanel.jCheckBox1.text")); // NOI18N

        jCheckBox2.setText(org.openide.util.NbBundle.getMessage(DesktopExeCustomizerPanel.class, "DesktopExeCustomizerPanel.jCheckBox2.text")); // NOI18N

        jCheckBox3.setText(org.openide.util.NbBundle.getMessage(DesktopExeCustomizerPanel.class, "DesktopExeCustomizerPanel.jCheckBox3.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(DesktopExeCustomizerPanel.class, "DesktopExeCustomizerPanel.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                    .addComponent(jCheckBox2, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                    .addComponent(jCheckBox3, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox3)
                .addContainerGap(191, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}