package jme3test.audio;

import com.jme3.audio.AudioNode;
import com.jme3.audio.Environment;
//import com.jme3.audio.PointAudioSource;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

public class TestAmbient extends AudioApp {

    private AudioNode river, nature, waves;
//    private PointAudioSource waves;
    private float time = 0;
    private float nextTime = 1;

    public static void main(String[] args){
        TestAmbient test = new TestAmbient();
        test.start();
    }

    @Override
    public void initAudioApp(){
        waves  = new AudioNode(manager, "Sound/Environment/Ocean Waves.ogg", true);

        nature = new AudioNode(manager, "Sound/Environment/Nature.ogg", true);
        nature.setPositional(true);
        
//        river  = new AudioSource(manager, "sounds/river.ogg");

//        float[] eax = new float[]
//            {15,	38.0f,	0.300f,	-1000,	-3300,	0,		1.49f,	0.54f,	1.00f,  -2560,	0.162f, 0.00f,0.00f,0.00f,	-229,	0.088f,		0.00f,0.00f,0.00f,	0.125f, 1.000f, 0.250f, 0.000f, -5.0f,  5000.0f,	250.0f, 0.00f,	0x3f }
//            ;
//
//        Environment env = new Environment(eax);
//        ar.setEnvironment(env);

        waves.setLocalTranslation(new Vector3f(4, -1, 30));
//        waves.setReverbEnabled(true);
        waves.setMaxDistance(10);
        waves.setRefDistance(5);
        nature.setVolume(2);
        ar.playSource(waves);
        ar.playSource(nature);
    }

    @Override
    public void updateAudioApp(float tpf){
        time += tpf;

        if (time > nextTime){
            
            time = 0;
            nextTime = FastMath.nextRandomFloat() * 2 + 0.5f;
        }
    }

}
