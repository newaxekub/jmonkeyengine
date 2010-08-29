package com.jme3.system;

import com.jme3.input.JoyInput;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.dummy.DummyKeyInput;
import com.jme3.input.dummy.DummyMouseInput;
import com.jme3.renderer.Renderer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NullContext implements JmeContext, Runnable {

    protected static final Logger logger = Logger.getLogger(NullContext.class.getName());

    protected AtomicBoolean created = new AtomicBoolean(false);
    protected AtomicBoolean needClose = new AtomicBoolean(false);
    protected final Object createdLock = new Object();

    protected int frameRate;
    protected AppSettings settings = new AppSettings(true);
    protected Timer timer;
    protected SystemListener listener;
    protected NullRenderer renderer;

    public Type getType() {
        return Type.Headless;
    }

    public void setSystemListener(SystemListener listener){
        this.listener = listener;
    }

    protected void initInThread(){
        logger.info("NullContext created.");
        logger.log(Level.FINE, "Running on thread: {0}", Thread.currentThread().getName());

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable thrown) {
                listener.handleError("Uncaught exception thrown in "+thread.toString(), thrown);
            }
        });

        timer = new NanoTimer();
        renderer = new NullRenderer();
        synchronized (createdLock){
            created.set(true);
            createdLock.notifyAll();
        }

        listener.initialize();
    }

    protected void deinitInThread(){
        listener.destroy();
        timer = null;
        synchronized (createdLock){
            created.set(false);
            createdLock.notifyAll();
        }
    }

    private static long timeThen;
    private static long timeLate;

    public void sync(int fps) {
        long timeNow;
        long gapTo;
        long savedTimeLate;

        gapTo = timer.getResolution() / fps + timeThen;
        timeNow = timer.getTime();
        savedTimeLate = timeLate;

        try {
            while (gapTo > timeNow + savedTimeLate) {
                Thread.sleep(1);
                timeNow = timer.getTime();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (gapTo < timeNow) {
            timeLate = timeNow - gapTo;
        } else {
            timeLate = 0;
        }

        timeThen = timeNow;
    }

    public void run(){
        initInThread();

        while (!needClose.get()){
            listener.update();

            if (frameRate > 0)
                sync(frameRate);
        }

        deinitInThread();
        
        logger.info("NullContext destroyed.");
    }

    public void destroy(boolean waitFor){
        needClose.set(true);
        if (waitFor)
            waitFor(false);
    }

    public void create(boolean waitFor){
        if (created.get()){
            logger.warning("create() called when NullContext is already created!");
            return;
        }

        new Thread(this, "Headless Application Thread").start();
        if (waitFor)
            waitFor(true);
    }

    public void restart() {
    }

    public void setAutoFlushFrames(boolean enabled){
    }

    public MouseInput getMouseInput() {
        return new DummyMouseInput();
    }

    public KeyInput getKeyInput() {
        return new DummyKeyInput();
    }

    public JoyInput getJoyInput() {
        return null;
    }

    public void setTitle(String title) {
    }

    public void create(){
        create(false);
    }

    public void destroy(){
        destroy(false);
    }

    protected void waitFor(boolean createdVal){
        synchronized (createdLock){
            while (created.get() != createdVal){
                try {
                    createdLock.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    public boolean isCreated(){
        return created.get();
    }

    public void setSettings(AppSettings settings) {
        this.settings.copyFrom(settings);
        frameRate = settings.getFrameRate();
        if (frameRate <= 0)
            frameRate = 60; // use default update rate.
    }

    public AppSettings getSettings(){
        return settings;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public Timer getTimer() {
        return timer;
    }

}
