package com.jme3.animation;

/**
 * <code>AnimEventListener</code> allows user code to recieve various
 * events regarding an AnimControl. For example, when an animation cycle is done.
 * @author Kirusha
 */
public interface AnimEventListener {

    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName);
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName);

}
