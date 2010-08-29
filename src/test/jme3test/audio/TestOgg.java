package jme3test.audio;

import com.jme3.asset.plugins.UrlLocator;
import com.jme3.audio.AudioNode;
import com.jme3.audio.LowPassFilter;

public class TestOgg extends AudioApp {

    private AudioNode src;

    public static void main(String[] args){
        TestOgg test = new TestOgg();
        test.start();
    }

    @Override
    public void initAudioApp(){
        System.out.println("Playing without filter");
        src = new AudioNode(manager, "Sound/Effects/Foot steps.ogg", true);
        ar.playSource(src);
    }

    @Override
    public void updateAudioApp(float tpf){
        if (src.getStatus() != AudioNode.Status.Playing){
            ar.deleteAudioData(src.getAudioData());

            System.out.println("Playing with low pass filter");
            src = new AudioNode(manager, "Sound/Effects/Foot steps.ogg", true);
            src.setDryFilter(new LowPassFilter(1f, .1f));
            src.setVolume(3);
            ar.playSource(src);
        }
    }

}
