/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.welcome;

import atmosphere.PlanetView;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.scene.PreviewRequest;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.scene.SceneListener;
import com.jme3.gde.core.scene.SceneRequest;
import com.jme3.gde.core.sceneexplorer.nodes.NodeUtility;
import com.jme3.scene.Node;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.checkbox.CheckboxControl;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.concurrent.Callable;
import org.netbeans.api.javahelp.Help;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 *
 * @author normenhansen
 */
public class WelcomeScreen implements ScreenController {

    PlanetView planetView;
    SceneRequest request;
    Nifty nifty;
    Screen screen;

    public void startScreen() {
        final Node rootNode = new Node("Welcome Screen");
        request = new SceneRequest(this, NodeUtility.createNode(rootNode), new ProjectAssetManager(null));
        request.setHelpCtx(new HelpCtx("com.jme3.gde.core.about"));
        request.setWindowTitle("Welcome to jMonkeyPlatform");
        SceneApplication.getApplication().addSceneListener(new SceneListener() {

            @Override
            public void sceneRequested(SceneRequest request) {
                if (request.getRequester() == WelcomeScreen.this) {
                    SceneApplication.getApplication().getStateManager().attach(planetView);
                }
            }

            @Override
            public boolean sceneClose(SceneRequest request) {
                SceneApplication.getApplication().getStateManager().detach(planetView);
                SceneApplication.getApplication().removeSceneListener(this);
                return true;
            }

            @Override
            public void previewRequested(PreviewRequest request) {
            }
        });
        SceneApplication.getApplication().enqueue(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                planetView = new PlanetView(rootNode,
                        SceneApplication.getApplication().getViewPort(),
                        SceneApplication.getApplication().getCamera(),
                        WelcomeScreen.this);
                SceneApplication.getApplication().requestScene(request);
                return null;
            }
        });
    }

    public void setNoStartup(){
        NbPreferences.forModule(Installer.class).put("NO_WELCOME_SCREEN", "true");
    }

    public void startHelp() {
        nifty.gotoScreen("help");
    }

    public void startIntro() {
        nifty.gotoScreen("intro");
    }

    public void startPlanet() {
        nifty.gotoScreen("planet");
    }

    public void creatingProjects() {
        Lookup.getDefault().lookup(Help.class).showHelp(new HelpCtx("jme3.jmonkeyplatform.project_creation"));
    }

    public void importingModels() {
        Lookup.getDefault().lookup(Help.class).showHelp(new HelpCtx("jme3.jmonkeyplatform.model_loader_and_viewer"));
    }

    public void editingScenes() {
        Lookup.getDefault().lookup(Help.class).showHelp(new HelpCtx("jme3.jmonkeyplatform.scene_composer"));
    }

    public void editingCode() {
        Lookup.getDefault().lookup(Help.class).showHelp(new HelpCtx("jme3.jmonkeyplatform.code_editor"));
    }

    public void updatingJmp() {
        Lookup.getDefault().lookup(Help.class).showHelp(new HelpCtx("com.jme3.gde.core.updating"));
    }

    public void tutorials() {
        Lookup.getDefault().lookup(Help.class).showHelp(new HelpCtx("jme3.beginner.hello_simpleapplication"));
    }

    public void quit() {
        if(screen.findElementByName("mainLayer").findElementByName("mainPanel").findElementByName("buttonBar").findElementByName("checkboxPanel").findControl("checkbox", CheckboxControl.class).isChecked()){
            setNoStartup();
        }
        SceneApplication.getApplication().closeScene(request);
    }

    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }
}
