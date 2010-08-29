package jme3test.export;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.export.binary.BinaryImporter;
import com.jme3.light.DirectionalLight;
import com.jme3.material.MaterialList;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.ogre.OgreMeshKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TestOgreConvert extends SimpleApplication {

    public static void main(String[] args){
        TestOgreConvert app = new TestOgreConvert();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Spatial ogreModel = assetManager.loadModel("Models/Oto/Oto.mesh.xml");

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(0,-1,-1).normalizeLocal());
        rootNode.addLight(dl);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BinaryExporter exp = new BinaryExporter();
            exp.save(ogreModel, baos);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            BinaryImporter imp = new BinaryImporter();
            imp.setAssetManager(assetManager);
            Node ogreModelReloaded = (Node) imp.load(bais, null, null);
            
            AnimControl control = ogreModelReloaded.getControl(AnimControl.class);
            AnimChannel chan = control.createChannel();
            chan.setAnim("Walk");
//            fis.close();

            rootNode.attachChild(ogreModelReloaded);
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
