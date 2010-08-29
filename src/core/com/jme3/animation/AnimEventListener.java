package com.jme3.animation;

/**
 * <code>AnimEventListener</code> allows user code to recieve various
 * events regarding an AnimControl. For example, when an animation cycle is done.
 * 
 * @author Kirill Vainer
 */
public interface AnimEventListener {

    /**
     * Invoked when an animation "cycle" is done. For non-looping animations,
     * this event is invoked when the animation is finished playing. For
     * looping animations, this even is invoked each time the animation is restarted.
     *
     * @param control The control to which the listener is assigned.
     * @param channel The channel being altered
     * @param animName The new animation that is done.
     */
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName);

    /**
     * Invoked when a animation is set to play by the user on the given channel.
     *
     * @param control The control to which the listener is assigned.
     * @param channel The channel being altered
     * @param animName The new animation name set.
     */
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName);

}
