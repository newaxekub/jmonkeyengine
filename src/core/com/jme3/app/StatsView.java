package com.jme3.app;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Statistics;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

public class StatsView extends Node implements Control {

    private BitmapText[] labels;
    private Statistics statistics;

    private String[] statLabels;
    private int[] statData;

    public StatsView(String name, AssetManager manager, Statistics stats){
        super(name);
        statistics = stats;

        statLabels = statistics.getLabels();
        statData = new int[statLabels.length];
        labels = new BitmapText[statLabels.length];

        BitmapFont font = manager.loadFont("Interface/Fonts/Console.fnt");
        for (int i = 0; i < labels.length; i++){
            labels[i] = new BitmapText(font);
            labels[i].setLocalTranslation(0, labels[i].getLineHeight() * (i+1), 0);
            attachChild(labels[i]);
        }

        addControl(this);
    }

    public void update(float tpf) {
        statistics.getData(statData);
        for (int i = 0; i < labels.length; i++) {
            labels[i].setText(statLabels[i] + " = " + statData[i]);
        }
        statistics.clearFrame();
    }

    public Control cloneForSpatial(Spatial spatial) {
        return (Control) spatial;
    }

    public void setSpatial(Spatial spatial) {
    }

    public void setEnabled(boolean enabled) {
    }

    public boolean isEnabled() {
        return true;
    }

    public void render(RenderManager rm, ViewPort vp) {
    }

}
