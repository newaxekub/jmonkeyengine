package com.jme3.animation;

import java.util.BitSet;

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

    public String getAnimationName() {
        return animation != null ? animation.getName() : null;
    }

    public LoopMode getLoopMode() {
        return loopMode;
    }

    public void setLoopMode(LoopMode loopMode) {
        this.loopMode = loopMode;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

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

    public void setAnim(String name){
        setAnim(name, defaultBlendTime);
    }

    /**
     * Add all the bones to the animation channel.
     */
    public void addAllBones() {
        affectedBones = null;
    }

    /**
     * Add a single Bone to the Channel,
     * and don't have multiple instances of the same in the list.
     */
    public void addBone(String name) {
        addBone(control.getSkeleton().getBone(name));
    }

    /**
     * Add a single Bone to the Channel,
     * and don't have multiple instances of the same in the list.
     */
    public void addBone(Bone bone) {
        int boneIndex = control.getSkeleton().getBoneIndex(bone);
//        if(affectedBones == null) {
//            affectedBones = new ArrayList<Integer>();
//            affectedBones.add(boneIndex);
//        } else if(!affectedBones.contains(boneIndex)) {
//            affectedBones.add(boneIndex);
//        }
        if(affectedBones == null) {
            affectedBones = new BitSet(control.getSkeleton().getBoneCount());
        }
        affectedBones.set(boneIndex);
    }

    /**
     * Add Bones to the Channel going toward the root bone. (i.e. parents)
     */
    public void addToRootBone(String name) {
        addToRootBone(control.getSkeleton().getBone(name));
    }

    /**
     * Add Bones to the Channel going toward the root bone. (i.e. parents)
     */
    public void addToRootBone(Bone bone) {
        addBone(bone);
        while (bone.getParent() != null) {
            bone = bone.getParent();
            addBone(bone);
        }
    }

    /**
     * Add Bones to the Channel going away from the root bone. (i.e. children)
     */
    public void addFromRootBone(String name) {
        addFromRootBone(control.getSkeleton().getBone(name));
    }

    /**
     * Add Bones to the Channel going away from the root bone. (i.e. children)
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
