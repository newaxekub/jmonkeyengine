package com.jme3.audio;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class QueuedAudioRenderer implements AudioRenderer, Runnable {

    private static final float UPDATE_RATE = 0.01f;

    private AudioRenderer wrapped;
    private final Thread thread = new Thread(this, "jME3 Audio Thread");
    private final Queue<Command> commandQueue = new LinkedList<Command>();

    private enum CmdType {
        Cleanup,
        SetListenerParams,
        SetEnvParams,
        PlaySourceInstance,
        PlaySource,
        PauseSource,
        StopSource,
    }

    private static class Command {
        
        private CmdType type;
        private Object[] args;

        public Command(CmdType type, Object ... args) {
            this.type = type;
            this.args = args;
        }

    }

    public QueuedAudioRenderer(AudioRenderer toWrap){
        wrapped = toWrap;
    }

    public void run(){
        wrapped.initialize();
        long updateRateNanos = (long) (UPDATE_RATE * 1000000000);
        mainloop: while (true){
            long startTime = System.nanoTime();

            // execute commands and update
            synchronized (thread){
                while (commandQueue.size() > 0){
                    Command cmd = commandQueue.remove();
                    if (cmd.type == CmdType.Cleanup)
                        break mainloop;

                    executeCommand(cmd);
                }
            }
            wrapped.update(UPDATE_RATE);

            long endTime = System.nanoTime();
            long diffTime = endTime - startTime;

            if (diffTime < updateRateNanos){
                long desiredEndTime = startTime + updateRateNanos;
                while (System.nanoTime() < desiredEndTime){
                    try{
                        Thread.sleep(1);
                    }catch (InterruptedException ex){
                    }
                }
            }
        }
        commandQueue.clear();
        wrapped.cleanup();
    }

    private void enqueueCommand(Command cmd){
        synchronized (thread){
            commandQueue.add(cmd);
        }
    }

    public void initialize(){
        if (!thread.isAlive()){
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY+1);
            thread.start();
        }else{
            throw new IllegalStateException("Initialize already called");
        }
    }

    public void cleanup(){
        if (thread.isAlive()){
            enqueueCommand(new Command(CmdType.Cleanup, (Object)null ));
        }else{
            throw new IllegalStateException("Already cleaned up or not initialized");
        }
    }

    private void executeCommand(Command cmd){
        System.out.println("-> " + cmd.type + Arrays.toString(cmd.args));
        switch (cmd.type){
            case SetListenerParams:
                wrapped.setListener( (Listener) cmd.args[0]);
                break;
            case PlaySource:
                wrapped.playSource( (AudioNode) cmd.args[0] );
                break;
            case PauseSource:
                wrapped.pauseSource( (AudioNode) cmd.args[0] );
                break;
            case StopSource:
                wrapped.stopSource( (AudioNode) cmd.args[0] );
                break;
        }
    }

    public void setListener(Listener listener){
        enqueueCommand(new Command(CmdType.SetListenerParams, new Listener(listener)));
    }

    public void setEnvironment(Environment env){
        enqueueCommand(new Command(CmdType.SetEnvParams, new Environment(env)));
    }

    public void playSourceInstance(AudioNode src){
        enqueueCommand(new Command(CmdType.PlaySourceInstance, src));
    }

    public void playSource(AudioNode src){
        enqueueCommand(new Command(CmdType.PlaySource, src));
    }
    
    public void pauseSource(AudioNode src){
        enqueueCommand(new Command(CmdType.PauseSource, src));
    }

    public void stopSource(AudioNode src){
        enqueueCommand(new Command(CmdType.StopSource, src));
    }

    public void deleteAudioData(AudioData ad){
    }

    public void update(float tpf){
        // do nothing. updated in thread.
    }

}
