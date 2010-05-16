package jme3test.model.anim;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 *
 * @author lex
 */
public class TestOgreAnim extends SimpleApplication implements AnimEventListener {

    public static void main(String[] args) {
        TestOgreAnim app = new TestOgreAnim();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(10f);
        cam.setLocation(new Vector3f(6.4013605f, 7.488437f, 12.843031f));
        cam.setRotation(new Quaternion(-0.060740203f, 0.93925786f, -0.2398315f, -0.2378785f));

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f, -0.7f, -1).normalizeLocal());
        dl.setColor(new ColorRGBA(1f, 1f, 1f, 1.0f));
        rootNode.addLight(dl);

        Spatial model = (Spatial) assetManager.loadModel("Models/Oto/Oto.meshxml");

        model.center();

        AnimControl control = model.getControl(AnimControl.class);
        control.addListener(this);
        AnimChannel channel = control.createChannel();

        channel.setAnim("Walk");

//        float scale = 1.00f;
        rootNode.attachChild(model);

//        model.setAnimation("Walk");
    }

    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        System.out.println("AnimDone: "+animName);
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
    }

}
