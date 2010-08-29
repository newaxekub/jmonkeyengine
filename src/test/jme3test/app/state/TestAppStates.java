package jme3test.app.state;

import com.jme3.app.Application;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;

public class TestAppStates extends Application {

    public static void main(String[] args){
        TestAppStates app = new TestAppStates();
        app.start();
    }

    @Override
    public void start(JmeContext.Type contextType){
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024, 768);
        setSettings(settings);
        
        super.start(contextType);
    }

    @Override
    public void initialize(){
        super.initialize();

        System.out.println("Initialize");

        RootNodeState state = new RootNodeState();
        viewPort.attachScene(state.getRootNode());
        stateManager.attach(state);

        Spatial model = assetManager.loadModel("Models/Teapot/Teapot.obj");
        model.scale(3);
        model.setMaterial(assetManager.loadMaterial("Interface/Logo/Logo.j3m"));
        state.getRootNode().attachChild(model);

        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager,
                                                           inputManager,
                                                           audioRenderer,
                                                           guiViewPort);
        niftyDisplay.getNifty().fromXml("jme3test/niftygui/hellojme.xml", "start");
        guiViewPort.addProcessor(niftyDisplay);
    }

    @Override
    public void update(){
        super.update();

        // do some animation
        float tpf = timer.getTimePerFrame();

        stateManager.update(tpf);
        stateManager.render(renderManager);

        // render the viewports
        renderManager.render(tpf);
    }

    @Override
    public void destroy(){
        super.destroy();

        System.out.println("Destroy");
    }
}
