package com.jme3.scene.plugins.ogre;

import com.jme3.animation.BoneAnimation;
import com.jme3.animation.Skeleton;
import java.util.ArrayList;

public class AnimData {

    public final Skeleton skeleton;
    public final ArrayList<BoneAnimation> anims;

    public AnimData(Skeleton skeleton, ArrayList<BoneAnimation> anims) {
        this.skeleton = skeleton;
        this.anims = anims;
    }
}
