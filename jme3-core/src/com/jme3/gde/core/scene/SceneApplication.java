/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.core.scene;

import com.jme3.app.Application;
import com.jme3.app.StatsView;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.gde.core.scene.nodes.JmeSpatial;
import com.jme3.gde.core.scene.processors.WireProcessor;
import com.jme3.gde.core.sceneviewer.SceneViewerTopComponent;
import com.jme3.input.FlyByCamera;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.system.AppSettings;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.util.BufferUtils;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.spi.project.LookupProvider;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * TODO:
 * - unbloat this file by outsourcing stuff to other classes
 * @author normenhansen
 */
public class SceneApplication extends Application implements LookupProvider, LookupListener, SceneProcessor {

    private PointLight camLight;
    private static SceneApplication application;

    public static SceneApplication getApplication() {
        if (application == null) {
            application = new SceneApplication();
        }
        return application;
    }
    protected Node rootNode = new Node("Root Node");
    protected Node guiNode = new Node("Gui Node");
    private SceneCameraController camController;
    //preview variables
    private static final int width = 120, height = 120;
    private final ByteBuffer cpuBuf = BufferUtils.createByteBuffer(width * height * 4);
    private final byte[] cpuArray = new byte[width * height * 4];
    protected Node previewNode = new Node("Preview Node");
    protected JmeSpatial previewSpat = null;
    protected float secondCounter = 0.0f;
    protected BitmapText fpsText;
    protected StatsView statsView;
    protected FlyByCamera flyCam;
    protected boolean showSettings = true;
    private Lookup.Result nodeSelectionResult;
    private ApplicationLogHandler logHandler = new ApplicationLogHandler();
    private WireProcessor wireProcessor;
    private FrameBuffer offBuffer;
    private ViewPort offView;
    private ConcurrentLinkedQueue<PreviewRequest> previewQueue = new ConcurrentLinkedQueue<PreviewRequest>();
    private SceneRequest currentSceneRequest;
    private PreviewRequest currentPreviewRequest;
    private ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Opening SceneViewer..");

    public SceneApplication() {
        progressHandle.start(7);
        AppSettings newSetting = new AppSettings(true);
        newSetting.setFrameRate(30);
//        settings.setVSync(true);
//        settings.setRenderer("JOGL");
        setSettings(newSetting);

        Logger.getLogger("com.jme3").addHandler(logHandler);

        setPauseOnLostFocus(false);

        //add listener for project selection
        nodeSelectionResult = Utilities.actionsGlobalContext().lookupResult(JmeSpatial.class);
        nodeSelectionResult.addLookupListener(this);

        createCanvas();
        getContext().setAutoFlushFrames(true);
        getContext().setSystemListener(this);
        progressHandle.progress("initialize Base Application", 1);
    }

    private void loadFPSText() {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        fpsText = new BitmapText(font, false);
        fpsText.setSize(font.getCharSet().getRenderedSize());
        fpsText.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        fpsText.setText("Frames per second");
        guiNode.attachChild(fpsText);
    }

    public void loadStatsView() {
        statsView = new StatsView("Statistics View", assetManager, renderer.getStatistics());
        // move it up so it appears above fps text
        statsView.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        guiNode.attachChild(statsView);
    }

    @Override
    public void initialize() {
        super.initialize();
        progressHandle.progress("Setup Camera Controller", 2);
        //create camera controler
        camController = new SceneCameraController(cam, inputManager);
        //create preview view
        progressHandle.progress("Setup Preview Scene", 3);
        setupPreviewView();

        // enable depth test and back-face culling for performance
        renderer.applyRenderState(RenderState.DEFAULT);

        progressHandle.progress("Prepare Camera", 4);
        camLight = new PointLight();
        camLight.setColor(ColorRGBA.Black);
        rootNode.addLight(camLight);

        progressHandle.progress("Prepare Stats View", 5);
        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);
        loadFPSText();
        loadStatsView();
        progressHandle.progress("Attach Scene to Viewport", 6);
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);
        cam.setLocation(new Vector3f(0, 0, 10));

        progressHandle.progress("Create", 6);
        wireProcessor = new WireProcessor(assetManager);
        progressHandle.finish();
    }

    private void doPreviews() {
        currentPreviewRequest = previewQueue.poll();
        if (currentPreviewRequest != null) {
            previewNode.detachAllChildren();
            previewNode.attachChild(currentPreviewRequest.getSpatial());
        }
    }

    @Override
    public void update() {
        if (speed == 0) {
            return;
        }

        super.update();
        float tpf = timer.getTimePerFrame();
        doPreviews();
//        Vector3f temp = camLight.getPosition();
//        temp.set(cam.getLeft()).multLocal(5.0f);
//        temp.addLocal(cam.getLocation());
        camLight.setPosition(cam.getLocation());

        secondCounter += tpf;
        int fps = (int) timer.getFrameRate();
        if (secondCounter >= 1.0f) {
            fpsText.setText("Frames per second: " + fps);
            secondCounter = 0.0f;
        }
        try {
            rootNode.updateLogicalState(tpf);
            guiNode.updateLogicalState(tpf);
            rootNode.updateGeometricState();
            guiNode.updateGeometricState();

            previewNode.updateLogicalState(tpf);
            previewNode.updateGeometricState();

            renderManager.render(tpf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO: Lookup for Application
    public Lookup createAdditionalLookup(Lookup baseContext) {
        return Lookups.fixed(getApplication());
    }

    /**
     * updates node selection
     * @param ev
     */
    public void resultChanged(LookupEvent ev) {
//        Collection collection = nodeSelectionResult.allInstances();
//        for (Iterator it = collection.iterator(); it.hasNext();) {
//            Object object = it.next();
//            if(object instanceof JmeSpatial){
//                notifySceneListeners((JmeSpatial)object);
//                return;
//            }
//        }
    }

    private void setupPreviewView() {
        Camera offCamera = new Camera(width, height);
        Geometry offBox;

        // create a pre-view. a view that is rendered before the main view
        offView = renderManager.createPreView("Offscreen View", offCamera);
        offView.setBackgroundColor(ColorRGBA.DarkGray);
        offView.addProcessor(this);

        // create offscreen framebuffer
        offBuffer = new FrameBuffer(width, height, 0);

        //setup framebuffer's cam
        offCamera.setFrustumPerspective(45f, 1f, 1f, 1000f);
        offCamera.setLocation(new Vector3f(0f, 1f, 40f));
        offCamera.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        //setup framebuffer to use texture
        offBuffer.setDepthBuffer(Format.Depth);
        offBuffer.setColorBuffer(Format.RGBA8);

        //set viewport to render to offscreen framebuffer
        offView.setOutputFrameBuffer(offBuffer);

        // setup framebuffer's scene
        PointLight light = new PointLight();
        light.setPosition(offCamera.getLocation());
        light.setColor(ColorRGBA.White);
        previewNode.addLight(light);

        // attach the scene to the viewport to be rendered
        offView.attachScene(previewNode);

    }

    private void updateImageContents() {
        cpuBuf.clear();
        renderer.readFrameBuffer(offBuffer, cpuBuf);

        // copy native memory to java memory
        cpuBuf.clear();
        cpuBuf.get(cpuArray);
        cpuBuf.clear();

        // flip the components the way AWT likes them
        for (int i = 0; i < width * height * 4; i += 4) {
            byte b = cpuArray[i + 0];
            byte g = cpuArray[i + 1];
            byte r = cpuArray[i + 2];
            byte a = cpuArray[i + 3];

            cpuArray[i + 0] = a;
            cpuArray[i + 1] = b;
            cpuArray[i + 2] = g;
            cpuArray[i + 3] = r;
        }

        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_4BYTE_ABGR);
//        synchronized (image) {
        WritableRaster wr = image.getRaster();
        DataBufferByte db = (DataBufferByte) wr.getDataBuffer();
        System.arraycopy(cpuArray, 0, db.getData(), 0, cpuArray.length);
//        }
        currentPreviewRequest.setImage(image);
        notifySceneListeners(currentPreviewRequest);
        currentPreviewRequest = null;
    }

    public void initialize(RenderManager rm, ViewPort vp) {
    }

    public void reshape(ViewPort vp, int i, int i1) {
    }

    public boolean isInitialized() {
        return true;
    }

    public void preFrame(float f) {
    }

    public void postQueue(RenderQueue rq) {
    }
    boolean mooPreview = false;

    public void postFrame(FrameBuffer fb) {
        if (currentPreviewRequest != null) {
            updateImageContents();
        }
    }

    public void cleanup() {
    }
    //TODO: replace with Lookup functionality
    private LinkedList<SceneListener> listeners = new LinkedList<SceneListener>();

    public void addSceneListener(SceneListener listener) {
        listeners.add(listener);
    }

    public void removeSceneListener(SceneListener listener) {
        listeners.remove(listener);
    }

    private void notifySceneListeners() {
        for (Iterator<SceneListener> it = listeners.iterator(); it.hasNext();) {
            SceneListener sceneViewerListener = it.next();
            sceneViewerListener.sceneRequested(currentSceneRequest);
        }
    }

    private void notifySceneListeners(PreviewRequest request) {
        for (Iterator<SceneListener> it = listeners.iterator(); it.hasNext();) {
            SceneListener sceneViewerListener = it.next();
            sceneViewerListener.previewRequested(request);
        }
    }

    public void createPreview(final PreviewRequest request) {
        previewQueue.add(request);
    }

    /**
     * method to display the node tree of a plugin (threadsafe)
     * @param tree
     */
    public void requestScene(final SceneRequest request) {
        setWindowTitle(request.getWindowTitle());
        setMimeType(request.getMimeType());
        enqueue(new Callable() {

            public Object call() throws Exception {
                rootNode.detachAllChildren();
                if (request.getManager() != null) {
                    assetManager = request.getManager().getManager();
                }
                closeCurrentScene();
                if (request.getRequester() instanceof SceneApplication) {
                    camController.enable();
                } else {
                    camController.disable();
                }
                currentSceneRequest = request;
                getCurrentSceneRequest().setDisplayed(true);
                Node model = request.getLookup().lookup(Node.class);
                if (model == null) {
                    StatusDisplayer.getDefault().setStatusText("could not load tree from request: " + getCurrentSceneRequest().getWindowTitle());
                    return null;
                }
                rootNode.attachChild(model);
                notifySceneListeners();
                return null;
            }
        });
    }

    private void closeCurrentScene() {
        if (currentSceneRequest != null) {
            currentSceneRequest.setDisplayed(false);
        }
        currentSceneRequest = null;
        resetCam();
    }

    private void resetCam() {
        cam.setLocation(new Vector3f(0, 0, 10));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

    private void setWindowTitle(final String string) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                SceneViewerTopComponent.findInstance().setDisplayName(string);
            }
        });
    }

    //TODO: mime type
    private void setMimeType(String string) {
    }

    public void enableCamLight(final boolean enabled) {
        enqueue(new Callable() {

            public Object call() throws Exception {
                //TODO: how to remove lights?? no removeLight in node?
                if (enabled) {
                    camLight.setColor(ColorRGBA.White);
                } else {
                    camLight.setColor(ColorRGBA.Black);
                }
                return null;
            }
        });
    }

    public void enableWireFrame(final boolean selected) {
        enqueue(new Callable() {

            public Object call() throws Exception {
                if (selected) {
                    viewPort.addProcessor(wireProcessor);
                } else {
                    viewPort.removeProcessor(wireProcessor);
                }
                return null;
            }
        });
    }

    /**
     * get list of materials used in this scene, those are the ones that
     * will be saved with the scene.
     * @return
     */
    private LinkedList<Material> getMaterialList(Node node, LinkedList<Material> materials) {
        List<Spatial> children = node.getChildren();
        for (Spatial spatial : children) {
            if (spatial instanceof Geometry) {
                Geometry geometry = (Geometry) spatial;
                Material material = geometry.getMaterial();
                if (!materials.contains(material)) {
                    materials.add(material);
                }
            }
            if (spatial instanceof Node) {
                getMaterialList((Node) spatial, materials);
            }
        }
        return materials;
    }

    @Deprecated
    public LinkedList<Material> getMaterialList() {
        return getMaterialList(rootNode, new LinkedList<Material>());
    }

    /**
     * @return the currentSceneRequest
     */
    public SceneRequest getCurrentSceneRequest() {
        return currentSceneRequest;
    }
}
