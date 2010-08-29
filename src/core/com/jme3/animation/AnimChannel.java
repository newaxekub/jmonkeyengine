package com.jme3.animation;

import com.jme3.math.FastMath;
import java.util.BitSet;

/**
 * <code>AnimChannel</code> provides controls, such as play, pause,
 * fast forward, etc, for an animation. The animation
 * channel may influence the entire model or specific bones of the model's
 * skeleton. A single model may have multiple animation channels influencing
 * various parts of its body. For example, a character model may have an
 * animation channel for its feet, and another for its torso, and
 * the animations for each channel are controlled independently.
 * 
 * @author Kirill Vainer
 */
public class AnimChannel {

    private AnimControl control;

//    private ArrayList<Integer> affectedBones;
    private BitSet affectedBones;

    private BoneAnimation animation;
    private BoneAnimation blendFrom;
    private float time;
    private float speed;
    private float timeBlendFrom;
    private float speedBlendFrom;

    private LoopMode loopMode, loopModeBlendFrom;
    private float defaultBlendTime = 0.15f;

    private float blendAmount = 1f;
    private float blendRate   = 0;

    private static float clampWrapTime(float t, float max, LoopMode loopMode){
        if (t < 0f){
            switch (loopMode){
                case DontLoop:
                    return 0;
                case Cycle:
                    return /*-0.001f;*/t;
                case Loop:
                    return max - t;
            }
        }else if (t > max){
            switch (loopMode){
                case DontLoop:
                    return max;
                case Cycle:
                    return /*-max;*/-(2f * max - t);
                case Loop:
                    return t - max;
            }
        }

        return t;
    }

    AnimChannel(AnimControl control){
        this.control = control;
    }

    /**
     * @return The name of the currently playing animation, or null if
     * none is assigned.
     *
     * @see AnimChannel#setAnim(java.lang.String) 
     */
    public String getAnimationName() {
        return animation != null ? animation.getName() : null;
    }

    /**
     * @return The loop mode currently set for the animation. The loop mode
     * determines what will happen to the animation once it finishes
     * playing.
     * 
     * For more information, see the LoopMode enum class.
     * @see LoopMode
     * @see AnimChannel#setLoopMode(com.jme3.animation.LoopMode)
     */
    public LoopMode getLoopMode() {
        return loopMode;
    }

    /**
     * @param loopMode Set the loop mode for the channel. The loop mode
     * determines what will happen to the animation once it finishes
     * playing.
     *
     * For more information, see the LoopMode enum class.
     * @see LoopMode
     */
    public void setLoopMode(LoopMode loopMode) {
        this.loopMode = loopMode;
    }

    /**
     * @return The speed that is assigned to the animation channel. The speed
     * is a scale value starting from 0.0, at 1.0 the animation will play
     * at its default speed.
     *
     * @see AnimChannel#setSpeed(float)
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * @param speed Set the speed of the animation channel. The speed
     * is a scale value starting from 0.0, at 1.0 the animation will play
     * at its default speed.
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * @return The time of the currently playing animation. The time
     * starts at 0 and continues on until getAnimMaxTime().
     *
     * @see AnimChannel#setTime(float)
     */
    public float getTime() {
        return time;
    }

    /**
     * @param time Set the time of the currently playing animation, the time
     * is clamped from 0 to getAnimMaxTime().
     */
    public void setTime(float time) {
        this.time = FastMath.clamp(time, 0, getAnimMaxTime());
    }

    /**
     * @return The length of the currently playing animation, or zero
     * if no animation is playing.
     *
     * @see AnimChannel#getTime()
     */
    public float getAnimMaxTime(){
        return animation != null ? animation.getLength() : 0f;
    }

    /**
     * Set the current animation that is played by this AnimChannel.
     * This resets the time to zero, and optionally blends the animation
     * over <code>blendTime</code> seconds with the currently playing animation.
     *
     * @param name The name of the animation to play
     * @param blendTime The blend time over which to blend the new animation
     * with the old one. If zero, then no blending will occur and the new
     * animation will be applied instantly.
     */
    public void setAnim(String name, float blendTime){
        if (name == null)
            throw new NullPointerException();

        if (blendTime < 0f)
            throw new IllegalArgumentException("blendTime cannot be less than zero");

        BoneAnimation anim = control.animationMap.get(name);
        if (anim == null)
            throw new IllegalArgumentException("Cannot find animation named: '"+name+"'");

        control.notifyAnimChange(this, name);

        if (animation != null && blendTime > 0f){
            // activate blending
            blendFrom = animation;
            timeBlendFrom = time;
            speedBlendFrom = speed;
            loopModeBlendFrom = loopMode;
            blendAmount = 0f;
            blendRate   = 1f / blendTime;
        }

        animation = anim;
        time = 0;
        speed = 1f;
        loopMode = LoopMode.Loop;
    }

    /**
     *
     * @param name
     */
    public void setAnim(String name){
        setAnim(name, defaultBlendTime);
    }

    /**
     * Add all the bones of the model's skeleton to be
     * influenced by this animation channel.
     */
    public void addAllBones() {
        affectedBones = null;
    }

    /**
     * Add a single bone to be influenced by this animation channel.
     */
    public void addBone(String name) {
        addBone(control.getSkeleton().getBone(name));
    }

    /**
     * Add a single bone to be influenced by this animation channel.
     */
    public void addBone(Bone bone) {
        int boneIndex = control.getSkeleton().getBoneIndex(bone);
        if(affectedBones == null) {
            affectedBones = new BitSet(control.getSkeleton().getBoneCount());
        }
        affectedBones.set(boneIndex);
    }

    /**
     * Add bones to be influenced by this animation channel starting from the
     * given bone name and going toward the root bone.
     */
    public void addToRootBone(String name) {
        addToRootBone(control.getSkeleton().getBone(name));
    }

    /**
     * Add bones to be influenced by this animation channel starting from the
     * given bone and going toward the root bone.
     */
    public void addToRootBone(Bone bone) {
        addBone(bone);
        while (bone.getParent() != null) {
            bone = bone.getParent();
            addBone(bone);
        }
    }

    /**
     * Add bones to be influenced by this animation channel, starting
     * from the given named bone and going toward its children.
     */
    public void addFromRootBone(String name) {
        addFromRootBone(control.getSkeleton().getBone(name));
    }

    /**
     * Add bones to be influenced by this animation channel, starting
     * from the given bone and going toward its children.
     */
    public void addFromRootBone(Bone bone) {
        addBone(bone);
        if (bone.getChildren() == null)
            return;
        for (Bone childBone : bone.getChildren()) {
            addBone(childBone);
            addFromRootBone(childBone);
        }
    }


    void reset(){
        animation = null;
        blendFrom = null;
    }

    void update(float tpf) {
        if (animation == null)
            return;

        if (blendFrom != null){
            blendFrom.setTime(timeBlendFrom, control.skeleton, 1f - blendAmount, affectedBones);
            timeBlendFrom += tpf * speedBlendFrom;
            timeBlendFrom = clampWrapTime(timeBlendFrom,
                                          blendFrom.getLength(),
                                          loopModeBlendFrom);
            if (timeBlendFrom < 0){
                timeBlendFrom = -timeBlendFrom;
                speedBlendFrom = -speedBlendFrom;
            }

            blendAmount += tpf * blendRate;
            if (blendAmount > 1f){
                blendAmount = 1f;
                blendFrom = null;
            }
        }

        animation.setTime(time, control.skeleton, blendAmount, affectedBones);
        time += tpf * speed;

        if (animation.getLength() > 0){
            if (time >= animation.getLength())
                control.notifyAnimCycleDone(this, animation.getName());
            else if (time < 0)
                control.notifyAnimCycleDone(this, animation.getName());
        }

        time = clampWrapTime(time, animation.getLength(), loopMode);
        if (time < 0){
            time = -time;
            speed = -speed;
        }

        
    }

}
