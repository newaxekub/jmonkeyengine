package com.jme3.app;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.BindingAdapter;
import com.jme3.input.FlyByCamera;
import com.jme3.input.KeyInput;
import com.jme3.material.RenderState;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext.Type;
import com.jme3.system.JmeSystem;
import com.jme3.util.BufferUtils;

/**
 * <code>SimpleApplication</code> extends the <code>Application</code> class
 * to provide default functionality like a first-person camera,
 * and an accessible root node that is updated and rendered regularly.
 */
public abstract class SimpleApplication extends Application {

    protected Node rootNode = new Node("Root Node");
    protected Node guiNode = new Node("Gui Node");

    protected float secondCounter = 0.0f;
    protected BitmapText fpsText;
    protected BitmapFont guiFont;
    protected StatsView statsView;

    protected FlyByCamera flyCam;
    protected boolean showSettings = true;

    public SimpleApplication(){
        super();
    }

    @Override
    public void start(){
        // set some default settings in-case
        // settings dialog is not shown
        if (settings == null)
            setSettings(new AppSettings(true));

        // show settings dialog
        if (showSettings){
            if (!JmeSystem.showSettingsDialog(settings))
                return;
        }

        super.start();
    }

    public FlyByCamera getFlyByCamera() {
        return flyCam;
    }

    public Node getGuiNode() {
        return guiNode;
    }

    public Node getRootNode() {
        return rootNode;
    }

    public boolean isShowSettings() {
        return showSettings;
    }

    public void setShowSettings(boolean showSettings) {
        this.showSettings = showSettings;
    }

    public void loadFPSText(){
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        fpsText = new BitmapText(guiFont, false);
        fpsText.setSize(guiFont.getCharSet().getRenderedSize());
        fpsText.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        fpsText.setText("Frames per second");
        guiNode.attachChild(fpsText);
    }

    public void loadStatsView(){
        statsView = new StatsView("Statistics View", assetManager, renderer.getStatistics());
        // move it up so it appears above fps text
        statsView.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        guiNode.attachChild(statsView);
    }

    @Override
    public void initialize(){
        super.initialize();

        // enable depth test and back-face culling for performance
        renderer.applyRenderState(RenderState.DEFAULT);

        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);
        loadFPSText();
        loadStatsView();
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);

        if (inputManager != null){
            flyCam = new FlyByCamera(cam);
            flyCam.setMoveSpeed(1f);
            flyCam.registerWithInput(inputManager);

            if (context.getType() == Type.Display){
                inputManager.registerKeyBinding("SIMPLEAPP_Exit", KeyInput.KEY_ESCAPE);
            }

            inputManager.registerKeyBinding("SIMPLEAPP_CameraPos", KeyInput.KEY_C);
            inputManager.registerKeyBinding("SIMPLEAPP_Memory",    KeyInput.KEY_M);
            inputManager.addBindingListener(new BindingAdapter() {
                @Override
                public void onBinding(String binding, float value) {
                    if (binding.equals("SIMPLEAPP_Exit")){
                        stop();
                    }else if (binding.equals("SIMPLEAPP_CameraPos")){
                        if (cam != null){
                            Vector3f loc = cam.getLocation();
                            Quaternion rot = cam.getRotation();
                            System.out.println("Camera Position: ("+
                                    loc.x+", "+loc.y+", "+loc.z+")");
                            System.out.println("Camera Rotation: "+rot);
                            System.out.println("Camera Direction: "+cam.getDirection());
                        }
                    }else if (binding.equals("SIMPLEAPP_Memory")){
                        BufferUtils.printCurrentDirectMemory(null);
                    }
                }
            });
        }

        // call user code
        simpleInitApp();
    }

    @Override
    public void update() {
        if (speed == 0 || paused)
            return;
        
        super.update();
        float tpf = timer.getTimePerFrame() * speed;

        secondCounter += timer.getTimePerFrame();
        int fps = (int) timer.getFrameRate();
        if (secondCounter >= 1.0f){
            fpsText.setText("Frames per second: "+fps);
            secondCounter = 0.0f;
        }
        
        simpleUpdate(tpf);
        rootNode.updateLogicalState(tpf);
        guiNode.updateLogicalState(tpf);
        rootNode.updateGeometricState();
        guiNode.updateGeometricState();

        renderManager.render(tpf);
        simpleRender(renderManager);
    }

    public abstract void simpleInitApp();

    public void simpleUpdate(float tpf){
    }

    public void simpleRender(RenderManager rm){
    }

}
