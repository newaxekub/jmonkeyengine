package jme3test.audio;

import com.jme3.audio.AudioRenderer;
import com.jme3.asset.AssetManager;
import com.jme3.audio.Listener;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;

public class AudioApp {

    private static final float UPDATE_RATE = 0.01f;

    protected AssetManager manager;
    protected Listener listener;
    protected AudioRenderer ar;

    public AudioApp(){
        AppSettings settings = new AppSettings(true);
        settings.setRenderer(null); // force dummy renderer (?)
        settings.setAudioRenderer(AppSettings.LWJGL_OPENAL);
        ar = JmeSystem.newAudioRenderer(settings);
        ar.initialize();
        manager = JmeSystem.newAssetManager();

        listener = new Listener();
        ar.setListener(listener);
    }

    public void initAudioApp(){
    }

    public void updateAudioApp(float tpf){
    }

    public void start(){
        initAudioApp();

        while (true){
            updateAudioApp(UPDATE_RATE);
            ar.update(UPDATE_RATE);

            try{
                Thread.sleep((int) (UPDATE_RATE * 1000f));
            }catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
    }
}
