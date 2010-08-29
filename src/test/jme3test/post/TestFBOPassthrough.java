package jme3test.post;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

/**
 * Demonstrates FrameBuffer usage.
 * The scene is first rendered to an FB with a texture attached,
 * the texture is then rendered onto the screen in ortho mode.
 *
 * @author Kirill
 */
public class TestFBOPassthrough extends SimpleApplication {

    private Node fbNode = new Node("Framebuffer Node");
    private FrameBuffer fb;

    public static void main(String[] args){
        TestFBOPassthrough app = new TestFBOPassthrough();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        int w = settings.getWidth();
        int h = settings.getHeight();

        //setup framebuffer
        fb = new FrameBuffer(w, h, 0);

        Texture2D fbTex = new Texture2D(w, h, Format.RGB8);
        fb.setDepthBuffer(Format.Depth);
        fb.setColorTexture(fbTex);

        // setup framebuffer's scene
        Sphere sphMesh = new Sphere(20, 20, 1);
        Material solidColor = assetManager.loadMaterial("Common/Materials/RedColor.j3m");

        Geometry sphere = new Geometry("sphere", sphMesh);
        sphere.setMaterial(solidColor);
        fbNode.attachChild(sphere);

        //setup main scene
        Picture p = new Picture("Picture");
        p.setPosition(0, 0);
        p.setWidth(w);
        p.setHeight(h);
        p.setTexture(assetManager, fbTex, false);

        rootNode.attachChild(p);
    }

    @Override
    public void simpleUpdate(float tpf){
        fbNode.updateLogicalState(tpf);
        fbNode.updateGeometricState();
    }

    @Override
    public void simpleRender(RenderManager rm){
        Renderer r = rm.getRenderer();

        //do FBO rendering
        r.setFrameBuffer(fb);

        rm.setCamera(cam, false); // FBO uses current camera
        r.clearBuffers(true, true, true);
        rm.renderScene(fbNode, viewPort);
        rm.flushQueue(viewPort);

        //go back to default rendering and let
        //SimpleApplication render the default scene
        r.setFrameBuffer(null);
    }

}
