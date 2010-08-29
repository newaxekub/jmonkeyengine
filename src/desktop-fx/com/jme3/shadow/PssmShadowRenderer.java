package com.jme3.shadow;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.renderer.queue.GeometryList;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireFrustum;
import com.jme3.shader.VarType;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

public class PssmShadowRenderer implements SceneProcessor {

    public static final String EDGE_FILTERING_PCF = "EDGE_FILTERING_PCF";
    public static final String EDGE_FILTERING_DITHER = "EDGE_FILTERING_DITHER";
    private int nbSplits = 3;
    private float lambda = 0.65f;
    private float shadowIntensity = 0.7f;
    private float zFarOverride = 0;
    private RenderManager renderManager;
    private ViewPort viewPort;
    private FrameBuffer[] shadowFB;
    private Texture2D[] shadowMaps;
    private Texture2D dummyTex;
    private Camera shadowCam;
    private Material preshadowMat;
    private Material postshadowMat;
    private Picture[] dispPic;
    private Matrix4f[] lightViewProjectionsMatrices;
    private float[] splits;
    private boolean noOccluders = false;
    private Vector3f[] points = new Vector3f[8];
    private Vector3f direction = new Vector3f();
    private AssetManager assetManager;
    private boolean debug = false;
    private float textureSize;

    /**
     * Create a PSSM Shadow Renderer
     * More info on the technique at http://http.developer.nvidia.com/GPUGems3/gpugems3_ch10.html
     * @param manager the application asset manager
     * @param size the size of the rendered shadowmaps (512,1024,2048, etc...)
     * @param nbSplits the number of shadow maps rendered (the more shadow maps the more quality, the less fps). 
     */
    public PssmShadowRenderer(AssetManager manager, int size, int nbSplits) {
        assetManager = manager;
        nbSplits= Math.max(Math.min(nbSplits, 8), 1);
        this.nbSplits = nbSplits;
        textureSize=size;

        shadowFB = new FrameBuffer[nbSplits];
        shadowMaps = new Texture2D[nbSplits];
        dispPic = new Picture[nbSplits];
        lightViewProjectionsMatrices = new Matrix4f[nbSplits];
        splits = new float[nbSplits + 1];
        dummyTex= new Texture2D(size, size, Format.RGB8);
        for (int i = 0; i < nbSplits; i++) {

            shadowFB[i] = new FrameBuffer(size, size, 0);
            shadowMaps[i] = new Texture2D(size, size, Format.Depth);

            shadowFB[i].setDepthTexture(shadowMaps[i]);
            shadowFB[i].setColorTexture(dummyTex);
            

            //quads for debuging purpose
            dispPic[i] = new Picture("Picture" + i);
            dispPic[i].setTexture(manager, shadowMaps[i], false);
        }
        preshadowMat = new Material(manager, "Common/MatDefs/Shadow/PreShadow.j3md");
        postshadowMat = new Material(manager, "Common/MatDefs/Shadow/PostShadowPSSM.j3md");
        


        shadowCam = new Camera(size, size);
        shadowCam.setParallelProjection(true);

        for (int i = 0; i < points.length; i++) {
            points[i] = new Vector3f();
        }
    }

    /**
     * Create a PSSM Shadow Renderer
     * More info on the technique at http://http.developer.nvidia.com/GPUGems3/gpugems3_ch10.html
     * @param manager the application asset manager
     * @param size the size of the rendered shadowmaps (512,1024,2048, etc...)
     * @param nbSplits the number of shadow maps rendered (the more shadow maps the more quality, the less fps).
     * @param edgeFiltering the type of filtering for shadow edge smoothing (use PSSMShadowRenderer.EDGE_FILTERING_DITHER or PSSMShadowRenderer.EDGE_FILTERING_PCF) default is PCF
     */
    public PssmShadowRenderer(AssetManager manager, int size, int nbSplits, String edgeFiltering) {
        this(manager, size, nbSplits);
        if (edgeFiltering.equals(EDGE_FILTERING_DITHER) || edgeFiltering.equals(EDGE_FILTERING_PCF)) {
            postshadowMat.selectTechnique("Default");
            postshadowMat.getActiveTechnique().getDef().addShaderParamDefine(edgeFiltering, edgeFiltering);
            postshadowMat.setParam(edgeFiltering, VarType.Boolean, true);
        }
    }

    //debug function that create a displayable frustrum
    private Geometry createFrustum(Vector3f[] pts, int i) {
        WireFrustum frustum = new WireFrustum(pts);
        Geometry frustumMdl = new Geometry("f", frustum);
        frustumMdl.setCullHint(Spatial.CullHint.Never);
        frustumMdl.setShadowMode(ShadowMode.Off);
        frustumMdl.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md"));
        switch (i) {
            case 0:
                frustumMdl.getMaterial().setColor("m_Color", ColorRGBA.Pink);
                break;
            case 1:
                frustumMdl.getMaterial().setColor("m_Color", ColorRGBA.Red);
                break;
            case 2:
                frustumMdl.getMaterial().setColor("m_Color", ColorRGBA.Green);
                break;
            case 3:
                frustumMdl.getMaterial().setColor("m_Color", ColorRGBA.Blue);
                break;
            default:
                frustumMdl.getMaterial().setColor("m_Color", ColorRGBA.White);
                break;
        }

        return frustumMdl;
    }

    public void initialize(RenderManager rm, ViewPort vp) {
        renderManager = rm;
        viewPort = vp;

    }

    public boolean isInitialized() {
        return viewPort != null;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction.set(direction).normalizeLocal();
    }

    public void postQueue(RenderQueue rq) {
        GeometryList occluders = rq.getShadowQueueContent(ShadowMode.Cast);
        noOccluders = occluders.size() == 0;
        if (noOccluders) {
            return;
        }

        GeometryList recievers = rq.getShadowQueueContent(ShadowMode.Recieve);
        Camera viewCam = viewPort.getCamera();

        float zFar = zFarOverride;
        if (zFar == 0) {
            zFar = PssmShadowUtil.computeZFar(occluders, recievers, viewCam);
        }
        //  System.out.println("Zfar : "+zFar);
        ShadowUtil.updateFrustumPoints(viewCam, viewCam.getFrustumNear(), zFar, 1.0f, points);

        Vector3f frustaCenter = new Vector3f();
        for (Vector3f point : points) {
            frustaCenter.addLocal(point);
        }
        frustaCenter.multLocal(1f / 8f);

        shadowCam.setDirection(direction);
        shadowCam.update();
        shadowCam.setLocation(frustaCenter);
        shadowCam.update();
        shadowCam.updateViewProjection();

        PssmShadowUtil.updateFrustumSplits(splits, viewCam.getFrustumNear(), zFar, lambda);

        Renderer r = renderManager.getRenderer();

        for (int i = 0; i < nbSplits; i++) {
            // update frustum points based on current camera and split
            ShadowUtil.updateFrustumPoints(viewCam, splits[i], splits[i + 1], 1.0f, points);

            //Updating shadow cam with curent slip frustra
//           System.out.println("split "+i);
            ShadowUtil.updateShadowCamera(occluders, recievers, shadowCam, points);

            //displaying the current splitted frustrum and the associated croped light frustrums in wireframe.
            //only for debuging purpose
            if (debug) {
                viewPort.attachScene(createFrustum(points, i));
                Vector3f[] pts = new Vector3f[8];
                for (int j = 0; j < pts.length; j++) {
                    pts[j] = new Vector3f();
                }
                ShadowUtil.updateFrustumPoints2(shadowCam, pts);
                viewPort.attachScene(createFrustum(pts, i));
                if (i == 3) {
                    debug = false;
                }
            }

            //saving light view projection matrix for this split
            lightViewProjectionsMatrices[i] = shadowCam.getViewProjectionMatrix().clone();

            renderManager.setCamera(shadowCam, false);
            renderManager.setForcedMaterial(preshadowMat);
            r.setFrameBuffer(shadowFB[i]);
            r.clearBuffers(false, true, false);

            // render shadow casters to shadow map
            viewPort.getQueue().renderShadowQueue(ShadowMode.Cast, renderManager, shadowCam, i == nbSplits - 1);

        }

        //restore setting for future rendering
        r.setFrameBuffer(viewPort.getOutputFrameBuffer());
        renderManager.setForcedMaterial(null);
        renderManager.setCamera(viewCam, false);


    }

    //debug only : displays depth shadow maps
    public void displayShadowMap(Renderer r) {
        Camera cam = viewPort.getCamera();
        renderManager.setCamera(cam, true);
        int h = cam.getHeight();
        for (int i = 0; i < dispPic.length; i++) {

            dispPic[i].setPosition(64 * (i + 1) + 128 * i, h / 20f);

            dispPic[i].setWidth(128);
            dispPic[i].setHeight(128);
            dispPic[i].updateGeometricState();
            renderManager.renderGeometry(dispPic[i]);
        }

        renderManager.setCamera(cam, false);

    }

    /**For dubuging purpose
     * Allow to "snapshot" the current frustrum to the scene
     */
    public void displayDebug() {
        debug = true;
    }

    public void postFrame(FrameBuffer out) {
        Camera cam = viewPort.getCamera();
        if (!noOccluders) {

            for (int i = 0; i < nbSplits; i++) {
                postshadowMat.setMatrix4("m_LightViewProjectionMatrix" + i, lightViewProjectionsMatrices[i]);
                postshadowMat.setTexture("m_ShadowMap" + i, shadowMaps[i]);
            }

            postshadowMat.setParam("m_Splits", VarType.FloatArray, splits);
          //  postshadowMat.setInt("m_NbSplits", nbSplits);
         //   postshadowMat.setFloat("m_TexSize", textureSize);
            postshadowMat.setFloat("m_ShadowIntensity", shadowIntensity);


            renderManager.setForcedMaterial(postshadowMat);


            viewPort.getQueue().renderShadowQueue(ShadowMode.Recieve, renderManager, cam, true);
            renderManager.setForcedMaterial(null);
            renderManager.setCamera(cam, false);

        }
        //    displayShadowMap(renderManager.getRenderer());
    }

    public void preFrame(float tpf) {
    }

    public void cleanup() {
    }

    public void reshape(ViewPort vp, int w, int h) {
    }

    public float getLambda() {
        return lambda;
    }

    /*
     * Adjust the repartition of the different shadow maps in the shadow extend
     * usualy goes from 0.0 to 1.0
     * a low value give a more linear repartition resulting in a constant quality in the shadow over the extends, but near shadows could look very jagged
     * a high value give a more logarithmic repartition resulting in a high quality for near shadows, but the quality quickly decrease over the extend.
     * the default value is set to 0.65f (theoric optimal value).
     * @param lambda the lambda value.
     */
    public void setLambda(float lambda) {
        this.lambda = lambda;
    }

    public float getShadowZExtend() {
        return zFarOverride;
    }

    /**
     * Set the distance from the eye where the shadows will be rendered
     * default value is dynamicaly computed to the shadow casters/recievers union bound zFar, capped to view frustum far value.
     * @param zFar the zFar values that override the computed one
     */
    public void setShadowZextend(float zFar) {
        this.zFarOverride = zFar;
    }

    public float getShadowIntensity() {
        return shadowIntensity;
    }

    /**
     * Set the shadowIntensity, the value should be between 0 and 1,
     * a 0 value gives a bright and invisilble shadow,
     * a 1 value gives a pitch black shadow,
     * default is 0.7
     * @param shadowIntensity the darkness of the shadow
     */
    public void setShadowIntensity(float shadowIntensity) {
        this.shadowIntensity = shadowIntensity;
    }


}

