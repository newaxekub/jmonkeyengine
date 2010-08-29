package com.jme3.app;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Statistics;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

/**
 * The <code>StatsView</code> provides a heads-up display (HUD) of various
 * statistics of rendering. The data is retrieved every frame from a
 * {@link com.jme3.renderer.Statistics} and then displayed on screen.<br/>
 * <br/>
 * Usage:<br/>
 * To use the stats view, you need to retrieve the
 * {@link com.jme3.renderer.Statistics} from the
 * {@link com.jme3.renderer.Renderer} used by the application. Then, attach
 * the <code>StatsView</code> to the scene graph.<br/>
 * <code><br/>
 * Statistics stats = renderer.getStatistics();<br/>
 * StatsView statsView = new StatsView("MyStats", assetManager, stats);<br/>
 * rootNode.attachChild(statsView);<br/>
 * </code>
 */
public class StatsView extends Node implements Control {

    private BitmapText[] labels;
    private Statistics statistics;

    private String[] statLabels;
    private int[] statData;

    private final StringBuilder stringBuilder = new StringBuilder();

    public StatsView(String name, AssetManager manager, Statistics stats){
        super(name);

        setQueueBucket(Bucket.Gui);
        setCullHint(CullHint.Never);

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
            stringBuilder.setLength(0);
            stringBuilder.append(statLabels[i]).append(" = ").append(statData[i]);
            labels[i].setText(stringBuilder);
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
