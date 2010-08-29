package jme3test.model.anim;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class TestOgreAnim extends SimpleApplication 
        implements AnimEventListener, ActionListener {

    private AnimChannel channel;
    private AnimControl control;

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

        Spatial model = (Spatial) assetManager.loadModel("Models/Oto/Oto.mesh.xml");

        model.center();

        control = model.getControl(AnimControl.class);
        control.addListener(this);
        channel = control.createChannel();

        for (String anim : control.getAnimationNames())
            System.out.println(anim);

        channel.setAnim("stand");

        rootNode.attachChild(model);

        inputManager.addListener(this, "Attack");
        inputManager.addMapping("Attack", new KeyTrigger(KeyInput.KEY_SPACE));
    }

    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        if (animName.equals("Dodge")){
            channel.setAnim("stand", 0.50f);
            channel.setLoopMode(LoopMode.DontLoop);
            channel.setSpeed(1f);
        }
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Attack") && value){
            if (!channel.getAnimationName().equals("Dodge")){
                channel.setAnim("Dodge", 0.50f);
                channel.setLoopMode(LoopMode.Cycle);
                channel.setSpeed(0.10f);
            }
        }
    }

}
