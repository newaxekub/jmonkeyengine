package com.jme3.video;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.ui.Picture;
import com.jme3.video.plugins.jheora.AVThread;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class TestVideoPlayer extends SimpleApplication {

    private Picture picture;
    private AVThread decoder;
    private Thread videoThread;
    private VQueue videoQueue;

    private long lastFrameTime = 0;
    private Clock masterClock;
    private AudioNode source;

    private float waitTime = 0;
    private VFrame frameToDraw = null;

    public static void main(String[] args){
        TestVideoPlayer app = new TestVideoPlayer();
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(60);
        app.setSettings(settings);
        app.start();
    }

    private void createVideo(){
        try {
            // uncomment to play video from harddrive
//          FileInputStream  fis = new FileInputStream("E:\\bunny.ogg");
            InputStream fis = new URL("http://mirrorblender.top-ix.org/peach/bigbuckbunny_movies/big_buck_bunny_480p_stereo.ogg").openStream();

            // increasing queued frames value from 5 will make web streamed
            // playback smoother at the cost of memory.
            videoQueue = new VQueue(5);
            decoder = new AVThread(fis, videoQueue);
            videoThread = new Thread(decoder, "Jheora Video Decoder");
            videoThread.setDaemon(true);
            videoThread.start();
            masterClock = decoder.getMasterClock();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void simpleInitApp() {
        picture = new Picture("VideoPicture", true);
        picture.setPosition(0, 0);
        picture.setWidth(settings.getWidth());
        picture.setHeight(settings.getHeight());
        picture.setImage(assetManager, "Interface/Logo/Monkey.jpg", false);

        // attach geometry to orthoNode
        rootNode.attachChild(picture);

        // start video playback
        createVideo();
    }

    private void drawFrame(VFrame frame){
        Image image = frame.getImage();
        frame.setImage(image);
        picture.setTexture(assetManager, frame, false);

        // note: this forces renderer to upload frame immediately,
        // since it is going to be returned to the video queue pool
        // it could be used again.
        renderer.setTexture(0, frame);
        videoQueue.returnFrame(frame);
        lastFrameTime = frame.getTime();
    }

    private void waitNanos(long time){
        long millis = (long) (time / Clock.MILLIS_TO_NANOS);
        int nanos   = (int) (time - (millis * Clock.MILLIS_TO_NANOS));

        try {
            Thread.sleep(millis, nanos);
        }catch (InterruptedException ex){
            stop();
            return;
        }
    }

    @Override
    public void simpleUpdate(float tpf){
        if (source == null){
            if (decoder.getAudioStream() != null){
                source = new AudioNode(decoder.getAudioStream(), null);
                source.setPositional(false);
                source.setReverbEnabled(false);
                audioRenderer.playSource(source);
            }else{
                // uncomment this statement to be able to play videos
                // without audio.
                return;
            }
        }

        if (waitTime > 0){
            waitTime -= tpf;
            if (waitTime > 0)
                return;
            else{
                waitTime = 0;
                drawFrame(frameToDraw);
                frameToDraw = null;
            }
        }else{
            VFrame frame;
            try {
                frame = videoQueue.take();
            } catch (InterruptedException ex){
                stop();
                return;
            }
            if (frame.getTime() < lastFrameTime){
                videoQueue.returnFrame(frame);
                return;
            }

            if (frame.getTime() == -2){
                // end of stream
                System.out.println("End of stream");
                stop();
                return;
            }

            long AV_SYNC_THRESHOLD = 1 * Clock.MILLIS_TO_NANOS;

            long delay = frame.getTime() - lastFrameTime;
//            delay -= tpf * Clock.SECONDS_TO_NANOS;
            long diff = frame.getTime() - masterClock.getTime();
            long syncThresh = delay > AV_SYNC_THRESHOLD ? delay : AV_SYNC_THRESHOLD;

            // if difference is more than 1 second, synchronize.
            if (Math.abs(diff) < Clock.SECONDS_TO_NANOS){
                if(diff <= -syncThresh) {
                  delay = 0;
                } else if(diff >= syncThresh) {
                  delay = 2 * delay;
                }
            }
//            delay = diff;

            System.out.println("M: "+decoder.getSystemClock().getTimeSeconds()+
                               ", V: "+decoder.getVideoClock().getTimeSeconds()+
                               ", A: "+decoder.getAudioClock().getTimeSeconds());

            if (delay > 0){
                waitNanos(delay);
                drawFrame(frame);
//                waitTime = (float) ((double) delay / Clock.SECONDS_TO_NANOS);
//                frameToDraw = frame;
            }else{
                videoQueue.returnFrame(frame);
                lastFrameTime = frame.getTime();
            }
        }
    }

}
