/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jme3test.post;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.post.ssao.SSAOConfig;

/**
 *
 * @author nehon
 */
public class SSAOUI {
    private SSAOConfig ssaoConfig;
    public SSAOUI(InputManager inputManager, SSAOConfig config) {
        this.ssaoConfig=config;
        
//    protected float sampleRadius = 5.1f;
//    protected float intensity = 1.5f;
//    protected float scale = 0.2f;
//    protected float bias = 0.1f;
//    protected boolean useOnlyAo = false;
//    protected boolean useAo = true;

        System.out.println("----------------- SSAO UI Debugger --------------------");
        System.out.println("-- Sample Radius : press Y to increase, H to decrease");
        System.out.println("-- AO Intensity : press U to increase, J to decrease");
        System.out.println("-- AO scale : press I to increase, K to decrease");
        System.out.println("-- AO bias : press O to increase, P to decrease");
        System.out.println("-- Toggle AO on/off : press space bar");
        System.out.println("-- Use only AO : press Num pad 0");
        System.out.println("-- Output config declaration : press P");
        System.out.println("-------------------------------------------------------");
    
        inputManager.addMapping("sampleRadiusUp", new KeyTrigger(KeyInput.KEY_Y));
        inputManager.addMapping("sampleRadiusDown", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("intensityUp", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("intensityDown", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("scaleUp", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping("scaleDown", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("biasUp", new KeyTrigger(KeyInput.KEY_O));
        inputManager.addMapping("biasDown", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping("outputConfig", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("toggleUseAO", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("toggleUseOnlyAo", new KeyTrigger(KeyInput.KEY_NUMPAD0));
        
        ActionListener acl = new ActionListener() {

            public void onAction(String name, boolean keyPressed, float tpf) {
               

               
                
                if (name.equals("toggleUseAO") && keyPressed) {
                    ssaoConfig.setUseAo(!ssaoConfig.isUseAo());
                    System.out.println("use AO : "+ssaoConfig.isUseAo());
                }
                if (name.equals("toggleUseOnlyAo") && keyPressed) {
                    ssaoConfig.setUseOnlyAo(!ssaoConfig.isUseOnlyAo());
                    System.out.println("use Only AO : "+ssaoConfig.isUseOnlyAo());

                }
                if (name.equals("outputConfig") && keyPressed) {
                    System.out.println("new SSAOConfig("+ssaoConfig.getSampleRadius()+"f,"+ssaoConfig.getIntensity()+"f,"+ssaoConfig.getScale()+"f,"+ssaoConfig.getBias()+"f,"+ssaoConfig.isUseOnlyAo()+","+ssaoConfig.isUseAo()+");");
                }
               
            }
        };

         AnalogListener anl = new AnalogListener() {

            public void onAnalog(String name, float value, float tpf) {
                if (name.equals("sampleRadiusUp")) {
                    ssaoConfig.setSampleRadius(ssaoConfig.getSampleRadius()+0.01f);
                    System.out.println("Sample Radius : "+ssaoConfig.getSampleRadius());
                }
                if (name.equals("sampleRadiusDown")) {
                    ssaoConfig.setSampleRadius(ssaoConfig.getSampleRadius()-0.01f);
                    System.out.println("Sample Radius : "+ssaoConfig.getSampleRadius());
                }
                if (name.equals("intensityUp")) {
                    ssaoConfig.setIntensity(ssaoConfig.getIntensity()+0.01f);
                    System.out.println("Intensity : "+ssaoConfig.getIntensity());
                }
                if (name.equals("intensityDown")) {
                    ssaoConfig.setIntensity(ssaoConfig.getIntensity()-0.01f);
                    System.out.println("Intensity : "+ssaoConfig.getIntensity());
                }
                if (name.equals("scaleUp")) {
                    ssaoConfig.setScale(ssaoConfig.getScale()+0.01f);
                    System.out.println("scale : "+ssaoConfig.getScale());
                }
                if (name.equals("scaleDown")) {
                    ssaoConfig.setScale(ssaoConfig.getScale()-0.01f);
                    System.out.println("scale : "+ssaoConfig.getScale());
                }
                if (name.equals("biasUp")) {
                    ssaoConfig.setBias(ssaoConfig.getBias()+0.001f);
                    System.out.println("bias : "+ssaoConfig.getBias());
                }
                if (name.equals("biasDown")) {
                    ssaoConfig.setBias(ssaoConfig.getBias()-0.001f);
                    System.out.println("bias : "+ssaoConfig.getBias());
                }


            }
        };
        inputManager.addListener(acl,"toggleUseAO","toggleUseOnlyAo","outputConfig");
        inputManager.addListener(anl, "sampleRadiusUp","sampleRadiusDown","intensityUp","intensityDown", "scaleUp","scaleDown",
                                "biasUp","biasDown");
     
    }
    
    

}
