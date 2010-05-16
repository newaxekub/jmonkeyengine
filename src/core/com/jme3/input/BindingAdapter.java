package com.jme3.input;

import com.jme3.input.binding.BindingListener;

public abstract class BindingAdapter implements BindingListener {

    public void onBinding(String binding, float value) {
    }

    public void onPreUpdate(float tpf) {
    }

    public void onPostUpdate(float tpf) {
    }

}
