package jme3test.audio;

import com.jme3.audio.AudioNode;

public class TestWav extends AudioApp {
    
    public static void main(String[] args){
        TestWav test = new TestWav();
        test.start();
    }

    public static final void sleep(float time){
        try{
            Thread.sleep((long) (time * 1000));
        }catch (InterruptedException ex){
        }
    }

    @Override
    public void initAudioApp(){
        AudioNode src = new AudioNode(manager, "Sound/Effects/Gun.wav", false);
        src.setLooping(true);

        ar.playSource(src);
        sleep(2);
        ar.stopSource(src);
        sleep(1);
        ar.playSource(src);
        sleep(1);
        ar.pauseSource(src);
        sleep(2);
        ar.playSource(src);
    }

}
