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
package com.jme3.cinematic;

import com.jme3.animation.LoopMode;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import java.io.IOException;

/**
 *
 * @author Nehon
 */
public abstract class AbstractCinematicEvent implements CinematicEvent, Savable {

    protected PlayState playState = PlayState.Stopped;
    protected float speed = 1;
    protected float initialDuration = -1;
    protected float duration = initialDuration / speed;
    protected LoopMode loopMode = LoopMode.DontLoop;
    protected float time = 0;

    public void play() {
        playState = PlayState.Playing;
        playEvent();
    }

    public abstract void playEvent();

    public void update(float tpf) {
        if (playState == PlayState.Playing) {
            time += tpf * speed;
            updateEvent(tpf);
            if (time >= duration && duration!=-1) {
                stop();
            }
        }

    }

    public abstract void updateEvent(float tpf);

    /**
     * stops the animation, next time play() is called the animation will start from the begining.
     */
    public void stop() {
        playState = PlayState.Stopped;
        time = 0;
        stopEvent();
    }

    public abstract void stopEvent();

    public void pause() {
        playState = PlayState.Paused;
        pauseEvent();
    }

    public abstract void pauseEvent();

    /**
     * returns the actual duration of the animtion (initialDuration/speed)
     * @return
     */
    public float getDuration() {
        return duration;
    }

    /**
     * Sets the speed of the animation.
     * At speed = 1, the animation will last initialDuration seconds,
     * At speed = 2 the animation will last initialDuraiton/2...
     * @param speed
     */
    public void setSpeed(float speed) {
        this.speed = speed;
        duration = initialDuration / speed;
    }

    /**
     * returns the speed of the animation.
     * @return
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Returns the current playstate of the animation
     * @return
     */
    public PlayState getPlayState() {
        return playState;
    }

    /**
     * returns the initial duration of the animation at speed = 1 in seconds.
     * @return
     */
    public float getInitialDuration() {
        return initialDuration;
    }

    /**
     * Sets the duration of the antionamtion at speed = 1 in seconds
     * @param initialDuration
     */
    public void setInitialDuration(float initialDuration) {
        this.initialDuration = initialDuration;
        duration = initialDuration / speed;
    }

    /**
     * retursthe loopMode of the animation
     * @see LoopMode
     * @return
     */
    public LoopMode getLoopMode() {
        return loopMode;
    }

    /**
     * Sets the loopMode of the animation
     * @see LoopMode
     * @param loopMode
     */
    public void setLoopMode(LoopMode loopMode) {
        this.loopMode = loopMode;
    }

    public void setInitalDuration(float initalDuration) {
        this.initialDuration = initalDuration;
        duration = initalDuration / speed;
    }

    public float getInitalDuration() {
        return initialDuration;
    }

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(playState, "playState", PlayState.Stopped);
        oc.write(speed, "speed", 1);
        oc.write(initialDuration, "initalDuration", 20);
        oc.write(loopMode, "loopMode", LoopMode.DontLoop);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        playState = ic.readEnum("playState", PlayState.class, PlayState.Stopped);
        speed = ic.readFloat("speed", 1);
        initialDuration = ic.readFloat("initalDuration", 20);
        duration = initialDuration / speed;
        loopMode = ic.readEnum("loopMode", LoopMode.class, LoopMode.DontLoop);
    }
}
