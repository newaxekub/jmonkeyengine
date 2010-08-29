package com.jme3.input.lwjgl;

import com.jme3.input.JoyInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.system.lwjgl.LwjglTimer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

public class LwjglJoyInput implements JoyInput {

    private static final Logger logger = Logger.getLogger(LwjglKeyInput.class.getName());

    private RawInputListener listener;
    private boolean enabled = false;

    public void initialize() {
        try {
            Controllers.create();
            if (Controllers.getControllerCount() == 0 || !Controllers.isCreated()){
                logger.warning("Joysticks disabled.");
                return;
            }
            logger.info("Joysticks created.");
            enabled = true;
        } catch (LWJGLException ex) {
            logger.log(Level.SEVERE, "Failed to create joysticks", ex);
        }
    }

    public int getJoyCount() {
        return Controllers.getControllerCount();
    }

    public String getJoyName(int joyIndex) {
        return Controllers.getController(joyIndex).getName();
    }

    public int getAxesCount(int joyIndex) {
        return Controllers.getController(joyIndex).getAxisCount();
    }

    public int getButtonCount(int joyIndex) {
        return Controllers.getController(joyIndex).getButtonCount();
    }

    private void printController(Controller c){
        System.out.println("Name: "+c.getName());
        System.out.println("Index: "+c.getIndex());
        System.out.println("Button Count: "+c.getButtonCount());
        System.out.println("Axis Count: "+c.getAxisCount());

        int buttons = c.getButtonCount();
        for (int b = 0; b < buttons; b++) {
            System.out.println("Button " + b + " = " + c.getButtonName(b));
        }

        int axis = c.getAxisCount();
        for (int b = 0; b < axis; b++) {
            System.out.println("Axis " + b + " = " + c.getAxisName(b));
        }
    }

    public void update() {
        if (!enabled)
            return;

        Controllers.poll();
        while (Controllers.next()){
            Controller c = Controllers.getEventSource();
            if (Controllers.isEventXAxis()){
                JoyAxisEvent evt = new JoyAxisEvent(c.getIndex(),
                                                    JoyInput.AXIS_X,
                                                    Controllers.getEventControlIndex(),
                                                    c.getXAxisValue());
                listener.onJoyAxisEvent(evt);
            }else if (Controllers.isEventYAxis()){
                JoyAxisEvent evt = new JoyAxisEvent(c.getIndex(),
                                                    JoyInput.AXIS_Y,
                                                    Controllers.getEventControlIndex(),
                                                    c.getYAxisValue());
                listener.onJoyAxisEvent(evt);
            }else if (Controllers.isEventAxis()){
                int realAxis = Controllers.getEventControlIndex();
                String axisName = c.getAxisName(realAxis);
                int axisId = -1;
                if (axisName.equals("Z Axis")){
                    axisId = JoyInput.AXIS_Z;
                }else if (axisName.equals("Z Rotation")){
                    axisId = JoyInput.AXIS_Z_ROT;
                }
                JoyAxisEvent evt = new JoyAxisEvent(c.getIndex(),
                                                    axisId,
                                                    realAxis,
                                                    c.getAxisValue(realAxis));
                listener.onJoyAxisEvent(evt);
            }else if (Controllers.isEventPovX()){
                JoyAxisEvent evt = new JoyAxisEvent(c.getIndex(),
                                                    JoyInput.POV_X,
                                                    -1,
                                                    c.getPovX());
                listener.onJoyAxisEvent(evt);
            }else if (Controllers.isEventPovY()){
                JoyAxisEvent evt = new JoyAxisEvent(c.getIndex(),
                                                    JoyInput.POV_Y,
                                                    -1,
                                                    c.getPovY());
                listener.onJoyAxisEvent(evt);
            }else if (Controllers.isEventButton()){
                int btn = Controllers.getEventControlIndex();
                JoyButtonEvent evt = new JoyButtonEvent(c.getIndex(),
                                                        btn,
                                                        c.isButtonPressed(btn));
                listener.onJoyButtonEvent(evt);
            }
        }
        Controllers.clearEvents();
    }

    public void destroy() {
        if (!enabled)
            return;

        Controllers.destroy();
        logger.info("Joysticks destroyed.");
    }

    public boolean isInitialized() {
        if (!enabled)
            return false;
        
        return Controllers.isCreated();
    }

    public void setInputListener(RawInputListener listener) {
        this.listener = listener;
    }

    public long getInputTimeNanos() {
        return Sys.getTime() * LwjglTimer.LWJGL_TIME_TO_NANOS;
    }

}
