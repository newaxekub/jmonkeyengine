/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.animation;

import com.jme3.cinematic.KeyFrame;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author Nehon
 */
public class TimeLine extends HashMap<Integer, KeyFrame> {

    protected int keyFramesPerSeconds = 30;
    protected int lastKeyFrameIndex = 0;

    public KeyFrame getKeyFrameAtTime(float time) {
        return get(getKeyFrameIndexFromTime(time));
    }

    public KeyFrame getKeyFrameAtIndex(int keyFrameIndex) {
        return get(keyFrameIndex);
    }

    public void addKeyFrameAtTime(float time, KeyFrame keyFrame) {
        addKeyFrameAtIndex(getKeyFrameIndexFromTime(time), keyFrame);
    }

    public void addKeyFrameAtIndex(int keyFrameIndex, KeyFrame keyFrame) {
        put(keyFrameIndex, keyFrame);

        if (lastKeyFrameIndex < keyFrameIndex) {
            lastKeyFrameIndex = keyFrameIndex;
        }
    }

    public void removeKeyFrame(int keyFrameIndex) {
        remove(keyFrameIndex);
        if (lastKeyFrameIndex == keyFrameIndex) {
            KeyFrame kf = null;
            for (int i = keyFrameIndex; kf == null && i >= 0; i--) {
                kf = getKeyFrameAtIndex(i);
                lastKeyFrameIndex = i;
            }
        }
    }

    public void removeKeyFrame(float time) {
        removeKeyFrame(getKeyFrameIndexFromTime(time));
    }

    public int getKeyFrameIndexFromTime(float time) {
        return Math.round(time * keyFramesPerSeconds);
    }

    public Collection<KeyFrame> getAllKeyFrames() {
        return values();
    }

    public int getLastKeyFrameIndex() {
        return lastKeyFrameIndex;
    }
}
