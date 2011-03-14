/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SelectionPanel.java
 *
 * Created on 14.06.2010, 16:52:22
 */
package com.jme3.gde.materials.multiview.widgets;

import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.sceneexplorer.nodes.properties.TexturePropertyEditor;
import com.jme3.gde.materials.MaterialProperty;
import java.awt.Component;

/**
 *
 * @author normenhansen
 */
public class TexturePanel extends MaterialPropertyWidget {

    private TexturePropertyEditor editor;
    private ProjectAssetManager manager;
    private boolean flip = false;
    private boolean repeat = false;
    private String textureName = null;

    /** Creates new form SelectionPanel */
    public TexturePanel(ProjectAssetManager manager) {
        this.manager = manager;
        editor = new TexturePropertyEditor(manager);
        initComponents();
    }

    private String getName(String path) {
        int idx = path.lastIndexOf("/");
        if (idx != -1 && path.length() > idx + 1) {
            path = path.substring(idx + 1, path.length());
        }
        if (path.length() > 15) {
            path = path.substring(0, 15) + "..";
        }
        return path;
    }

    private void updateFlipRepeat() {
        if (flip && repeat) {
            property.setValue("Flip Repeat " + textureName);
            jLabel2.setToolTipText("Flip Repeat " + textureName);
        } else if (flip) {
            property.setValue("Flip " + textureName);
            jLabel2.setToolTipText("Flip " + textureName);
        } else if (repeat) {
            property.setValue("Repeat " + textureName);
            jLabel2.setToolTipText("Repeat " + textureName);
        } else {
            property.setValue(textureName);
        }
    }

    private String getTextureName(String string) {
        string = string.replaceFirst("Flip ", "");
        return string.replaceFirst("Repeat ", "");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();

        setBackground(new java.awt.Color(204, 204, 204));

        jToolBar1.setBackground(new java.awt.Color(204, 204, 204));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setMinimumSize(new java.awt.Dimension(212, 32));
        jToolBar1.setPreferredSize(new java.awt.Dimension(212, 32));

        jLabel1.setText(org.openide.util.NbBundle.getMessage(TexturePanel.class, "TexturePanel.jLabel1.text")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 0));
        jToolBar1.add(jLabel1);

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setPreferredSize(new java.awt.Dimension(10, 0));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );

        jToolBar1.add(jPanel1);

        jLabel2.setFont(new java.awt.Font("Courier", 0, 13)); // NOI18N
        jLabel2.setText(org.openide.util.NbBundle.getMessage(TexturePanel.class, "TexturePanel.jLabel2.text")); // NOI18N
        jLabel2.setMaximumSize(new java.awt.Dimension(200, 30));
        jLabel2.setPreferredSize(new java.awt.Dimension(200, 14));
        jToolBar1.add(jLabel2);

        jButton1.setText(org.openide.util.NbBundle.getMessage(TexturePanel.class, "TexturePanel.jButton1.text")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        jCheckBox1.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jCheckBox1.setText(org.openide.util.NbBundle.getMessage(TexturePanel.class, "TexturePanel.jCheckBox1.text")); // NOI18N
        jCheckBox1.setFocusable(false);
        jCheckBox1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jCheckBox1);

        jCheckBox2.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jCheckBox2.setText(org.openide.util.NbBundle.getMessage(TexturePanel.class, "TexturePanel.jCheckBox2.text")); // NOI18N
        jCheckBox2.setFocusable(false);
        jCheckBox2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jCheckBox2);

        jButton2.setText(org.openide.util.NbBundle.getMessage(TexturePanel.class, "TexturePanel.jButton2.text")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Component view = editor.getCustomEditor();
        view.setVisible(true);
        if (editor.getValue() != null) {
            textureName = editor.getAsText();
            jLabel2.setText(getName(textureName));
            updateFlipRepeat();
            fireChanged();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        textureName = "";
        jLabel2.setText("");
        jLabel2.setToolTipText("");
        property.setValue("");
        fireChanged();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        if (property == null) {
            return;
        }
        flip = jCheckBox1.isSelected();
        updateFlipRepeat();
        fireChanged();
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        if (property == null) {
            return;
        }
        repeat = jCheckBox2.isSelected();
        updateFlipRepeat();
        fireChanged();
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    @Override
    protected void readProperty() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (property.getValue().startsWith("Flip Repeat ")) {
                    flip = true;
                    repeat = true;
                    textureName = property.getValue().replaceFirst("Flip Repeat ", "").trim();
                } else if (property.getValue().startsWith("Flip ")) {
                    flip = true;
                    textureName = property.getValue().replaceFirst("Flip ", "").trim();
                } else if (property.getValue().startsWith("Repeat ")) {
                    repeat = true;
                    textureName = property.getValue().replaceFirst("Repeat ", "").trim();
                } else {
                    textureName = property.getValue();
                }
                jLabel1.setText(property.getName());
                jLabel1.setToolTipText(property.getName());
                jLabel2.setText(getName(textureName));
                jLabel2.setToolTipText(property.getValue());
                MaterialProperty prop = property;
                property = null;
                jCheckBox1.setSelected(flip);
                jCheckBox2.setSelected(repeat);
                property = prop;
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables
}
