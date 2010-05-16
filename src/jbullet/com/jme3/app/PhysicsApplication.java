/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.app;

import com.jme3.bullet.PhysicsSpace;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author normenhansen
 */
@Deprecated
public class PhysicsApplication extends Application implements Runnable{
    private Thread physicsThread;
    private PhysicsSpace pSpace;

    private boolean multithreaded=false;
    private boolean running=false;

    private long lastTime=-1;

    private float accuracy=1/60f;

    public PhysicsApplication() {
        super();
    }

    public void startPhysics(){
        if(!isMultithreaded()){
            pSpace=new PhysicsSpace();
        }
        else{
            startThread();
            //TODO: crude way to check for thread
            while(pSpace==null){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PhysicsApplication.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void update() {
        super.update();
        if(!isMultithreaded()){
            physicsUpdate(timer.getTimePerFrame());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public void destroy() {
        super.destroy();
        if(physicsThread!=null){
            try {
                running = false;
                physicsThread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(PhysicsApplication.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * can be overridden by user, called from physics thread!
     */
    public void physicsUpdate(float tpf){
        if(pSpace==null) return;
        pSpace.update(tpf * speed);
    }

    private void startThread(){
        running=true;
        if(physicsThread!=null)
            return;
        physicsThread=new Thread(this);
        physicsThread.start();
    }

    public void run() {
        pSpace=new PhysicsSpace();
        while(running){
            lastTime=System.currentTimeMillis();
            physicsUpdate(accuracy);
            float wait=(lastTime+(accuracy*1000))-System.currentTimeMillis();
            if(wait<=0){
                try {
                    //                    System.out.println("sleep "+wait);
                    Thread.sleep(Math.round(accuracy * 1000));
                } catch (InterruptedException ex) {
                    Logger.getLogger(PhysicsApplication.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                try {
                    System.out.println("sleep "+wait);
                    Thread.sleep(Math.round(wait));
                } catch (InterruptedException ex) {
                    Logger.getLogger(PhysicsApplication.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public PhysicsSpace getPhysicsSpace() {
        return pSpace;
    }

    /**
     * @return the multithreaded
     */
    public boolean isMultithreaded() {
        return multithreaded;
    }

    /**
     * note that when you enable multithreading all changes to PhysicsNodes have
     * to be done from the physics thread. Also, the updatePhysics() method
     * will be called from the physics thread!
     * @param multithreaded the multithreaded to set
     */
    public void setMultithreaded(boolean multithreaded) {
        this.multithreaded = multithreaded;
    }

}
