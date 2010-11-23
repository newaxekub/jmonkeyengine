package atmosphere;

import com.jme3.animation.AnimationPath;
import com.jme3.animation.AnimationPathListener;
import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioRenderer;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.welcome.WelcomeScreen;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.post.HDRRenderer;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Caps;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.util.SkyFactory;
import de.lessvoid.nifty.Nifty;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 * This is a small prototype that integrates the atmosphere scattering shaders
 * developed by Sean O'Neil in a JME3 example.
 *
 * Notes:
 *
 * The planet consists of two icospheres (the mesh has been exported from blender),
 * one for the ground and one for the outer atmosphere.
 * Each icosphere uses two materials (see in MatDefs folder). One when the
 * camera is in outer space and the other when it is inside the atmosphere.
 *
 * On the upper left corner there are a few parameters that can be changed using
 * the keys in brackets. Press shift+key to decrease a value.
 *
 * 
 * @author jiyarza
 */
public class PlanetView implements AppState {

    DirectionalLight sun;
    Planet planet;
    PlanetRenderer planetRenderer;
    final static Vector3f PLANET_POSITION = new Vector3f(0.0f, 0.0f, 0.0f);
    final static float PLANET_RADIUS = 6371f * 10f; // Km
    public final static Vector3f lightPosition = new Vector3f(0, 0f, -PLANET_RADIUS * 10f);
    final static Vector3f LIGHT_DIRECTION = PLANET_POSITION.subtract(lightPosition);
    private SkyBox sky;
    // starting angle for the sun light
    float angle = 3.14159f;
    // This object encapsulates the HUD and user input
    private HDRRenderer hdrRender;
    // Music can't hurt, usually
    private AudioNode musicNode;
    private String[] jukebox = {
        "Sounds/SpaceDrums.ogg"
    };
    private int songIndex = 0;
    private AssetManager assetManager;
    private Renderer renderer;
    private AudioRenderer audioRenderer;
    private Camera camera;
    private boolean initialized = false;
    private boolean active = false;
    private Node rootNode;
    private ViewPort viewPort;
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;
    private WelcomeScreen welcomeScreen;

    public PlanetView(Node rootNode, ViewPort viewPort, Camera cam, WelcomeScreen welcomeScreen) {
        this.rootNode = rootNode;
        this.viewPort = viewPort;
        this.camera = cam;
        this.welcomeScreen = welcomeScreen;
    }

    public void simpleInitApp() {
        rootNode.setCullHint(CullHint.Never);

        setupSkyBox();
        // Uncomment to try HDR (better planet illumination, when you find the right angle))
        //HDRInit();

        setupCamera();
        setupLights();
        Collection<Caps> caps = renderer.getCaps();
//        Logger.getLogger(PlanetView.class.getName()).log(Level.INFO, "Caps: {0}" + caps.toString());
        for (Iterator<Caps> it = caps.iterator(); it.hasNext();) {
            Caps caps1 = it.next();
            if (caps1.equals(Caps.OpenGL21)) {
                setupPlanet();
                active = true;
            }
        }
//        setupPlanet();
//        setupGUI();
        // This plays a small piece of music I did sometime
        // ago. If you don't like it, you can either comment this line or
        // consider using your own sound track ;-)
        setupMusic();
        niftyDisplay = new NiftyJmeDisplay(assetManager,
                SceneApplication.getApplication().getInputManager(),
                audioRenderer,
                SceneApplication.getApplication().getGuiViewPort());
        nifty = niftyDisplay.getNifty();
        try {
            nifty.fromXml("Interface/WelcomeScreen.xml", new URL("nbres:/Interface/WelcomeScreen.xml").openStream(), "start", welcomeScreen);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        // attach the nifty display to the gui view port as a processor
        SceneApplication.getApplication().getGuiViewPort().addProcessor(niftyDisplay);

        // disable the fly cam
//        flyCam.setEnabled(false);
    }

    private void HDRInit() {
        // remove skybox
        if (sky != null) {
            sky.detach(rootNode);
        }

        hdrRender = new HDRRenderer(assetManager, renderer);
        hdrRender.setSamples(0);
        hdrRender.setMaxIterations(0);
        hdrRender.setExposure(0.0005f);
        hdrRender.setThrottle(0.5f);

        viewPort.addProcessor(hdrRender);
    }

    public void simpleUpdate(float tpf) {
//        rootNode.getParent().updateLogicalState(tpf);
//        rootNode.getParent().updateGeometricState();
        // Rotate the sun light
        angle += tpf * 0.01;
        sun.setDirection(new Vector3f(-FastMath.sin(angle) * PLANET_RADIUS, FastMath.HALF_PI,
                FastMath.cos(angle) * PLANET_RADIUS));
        planetRenderer.setLightPosition(sun.getDirection());

        // Update UI, planet/player and audio
//        gui.update(tpf);
        planetRenderer.update(tpf);
//        camConNode.lookAt(PLANET_POSITION, camera.getUp());

        // fade in, being lazy here
        if (musicNode != null) {
            float v = musicNode.getVolume();
            if (v < 0.6f) {
                musicNode.setVolume(v + 0.001f);
            }

            if (musicNode.getStatus() == AudioNode.Status.Stopped) {
                musicNode.setVolume(0.0f);
                musicNode = new AudioNode(assetManager, jukebox[songIndex++ % jukebox.length], true);
                audioRenderer.playSource(musicNode);
            }
        }
    }

    /**
     * Create scene lights
     */
    public void setupLights() {
        sun = new DirectionalLight();
        sun.setDirection(LIGHT_DIRECTION);
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
    }

    /**
     * Create sky box
     */
    public void setupSkyBox() {
        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/blue-glow-1024.dds", true));
//        sky = new SkyBox(assetManager, "blue-glow-1024");
//        sky.attach(rootNode);
    }
    /**
     * Configure the camera relative to planet position and size
     */
    CameraNode camNode;

    public void setupCamera() {
        camera.setFrustumFar(PLANET_RADIUS * 1000f);

        farView();

        camNode = new CameraNode(camera);
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(camera.getLocation());
        camNode.setLocalRotation(camera.getRotation());

        final AnimationPath path = new AnimationPath(camNode);
        path.setDirectionType(AnimationPath.Direction.None);

        path.addWayPoint(new Vector3f(PLANET_POSITION.x + PLANET_RADIUS * 1.6f,
                PLANET_POSITION.y, PLANET_POSITION.z + PLANET_RADIUS * 0.5f));

        path.addWayPoint(new Vector3f(PLANET_POSITION.x + PLANET_RADIUS * 2.0f,
                PLANET_POSITION.y, PLANET_POSITION.z + PLANET_RADIUS * 2.0f));

        path.addWayPoint(new Vector3f(PLANET_POSITION.x + PLANET_RADIUS * 0.5f,
                PLANET_POSITION.y, PLANET_POSITION.z + PLANET_RADIUS * 1.8f));

        path.addWayPoint(new Vector3f(PLANET_POSITION.x + PLANET_RADIUS * 2.0f,
                PLANET_POSITION.y, PLANET_POSITION.z + PLANET_RADIUS * 2.0f));

        path.addWayPoint(new Vector3f(PLANET_POSITION.x + PLANET_RADIUS * 1.6f,
                PLANET_POSITION.y, PLANET_POSITION.z + PLANET_RADIUS * 0.5f));
        path.setDuration(300f);
        path.setPathInterpolation(AnimationPath.PathInterpolation.CatmullRom);
//        path.enableDebugShape(assetManager, rootNode);
        path.play();
        path.addListener(new AnimationPathListener() {

            public void onWayPointReach(AnimationPath ap, int i) {
                if (ap.getNbWayPoints() == i + 1) {
                    ap.stop();
                    ap.play();
                }
            }
        });

        rootNode.attachChild(camNode);
    }

    public void closeView() {
        camera.setLocation(new Vector3f(PLANET_POSITION.x + PLANET_RADIUS,
                PLANET_POSITION.y, PLANET_POSITION.z + PLANET_RADIUS));
        camera.lookAt(lightPosition, camera.getUp());
    }

    public void farView() {
        camera.setLocation(new Vector3f(PLANET_POSITION.x + PLANET_RADIUS * 2f,
                PLANET_POSITION.y, PLANET_POSITION.z + PLANET_RADIUS * 2f));
        camera.lookAt(PLANET_POSITION, camera.getUp());
    }

//    public void atmosphericSpeed() {
//        flyCam.setMoveSpeed(PLANET_RADIUS * 0.01f);
//    }
//
//    public void outerSpaceSpeed() {
//        flyCam.setMoveSpeed(PLANET_RADIUS * 0.05f);
//    }
    /**
     * Create a planet
     */
    public void setupPlanet() {
        planet = new Planet(PLANET_RADIUS, PLANET_POSITION);
        planetRenderer = new PlanetRenderer(this, planet, lightPosition);
        planetRenderer.init();
    }

    /**
     * Play something
     */
    private void setupMusic() {
        musicNode = new AudioNode(assetManager, jukebox[songIndex++], true);
        musicNode.setDirectional(false);
        musicNode.setLooping(true);
        musicNode.setVolume(0.0f);
        audioRenderer.playSource(musicNode);
    }

    @Override
    public void initialize(AppStateManager asm, Application aplctn) {

        assetManager = aplctn.getAssetManager();
        renderer = aplctn.getRenderer();
        audioRenderer = aplctn.getAudioRenderer();
        simpleInitApp();

        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void setActive(boolean bln) {
        this.active = bln;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void stateAttached(AppStateManager asm) {
    }

    @Override
    public void stateDetached(AppStateManager asm) {
        SceneApplication.getApplication().getGuiViewPort().removeProcessor(niftyDisplay);
        if (musicNode != null) {
            audioRenderer.stopSource(musicNode);
        }
        nifty.exit();
    }

    @Override
    public void update(float f) {
        if (active) {
            simpleUpdate(f);
        }
    }

    @Override
    public void render(RenderManager rm) {
    }

    @Override
    public void postRender() {
    }

    @Override
    public void cleanup() {
    }

    /**
     * @return the assetManager
     */
    public AssetManager getAssetManager() {
        return assetManager;
    }

    /**
     * @return the rootNode
     */
    public Node getRootNode() {
        return rootNode;
    }

    /**
     * @return the camera
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * @return the nifty
     */
    public Nifty getNifty() {
        return nifty;
    }
}
