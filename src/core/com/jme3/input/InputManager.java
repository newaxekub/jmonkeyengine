package com.jme3.input;

import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.FastMath;
import com.jme3.util.IntMap;
import com.jme3.util.IntMap.Entry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The <code>InputManager</code> is responsible for converting input events
 * received from the Key, Mouse and Joy Input implementations into an
 * abstract, input device independent representation that user code can use.
 *
 * By default a dispatcher is included with every Application instance for use
 * in user code to query input, unless the Application is created as headless
 * or with input explicitly disabled.
 */
public class InputManager implements RawInputListener {

    private static final Logger logger = Logger.getLogger(InputManager.class.getName());
    
    private final KeyInput keys;
    private final MouseInput mouse;
    private final JoyInput joystick;

    private float frameTPF;
    private long lastLastUpdateTime = 0;
    private long lastUpdateTime = 0;
    private long frameDelta = 0;
    private long firstTime = 0;
    private boolean eventsPermitted = false;
    private boolean mouseVisible = true;
    private boolean safeMode = false;

    private float axisDeadZone = 0.05f;

    private final IntMap<ArrayList<Mapping>> bindings = new IntMap<ArrayList<Mapping>>();
    private final HashMap<String, Mapping> mappings = new HashMap<String, Mapping>();
    private final IntMap<Long> pressedButtons = new IntMap<Long>();

    private ArrayList<RawInputListener> rawListeners = new ArrayList<RawInputListener>();

    private static class Mapping {

        private final String name;
        private final ArrayList<Integer> triggers = new ArrayList<Integer>();
        private final ArrayList<InputListener> listeners = new ArrayList<InputListener>();

        public Mapping(String name){
            this.name = name;
        }
    }

    /**
     * Initializes the InputManager.
     *
     * @param mouseInput
     * @param keyInput
     * @param joyInput
     * @throws IllegalArgumentException If either mouseInput or keyInput are null.
     */
    public InputManager(MouseInput mouse, KeyInput keys, JoyInput joystick){
        if (keys == null || mouse == null)
            throw new NullPointerException("Mouse or keyboard cannot be null");

        this.keys = keys;
        this.mouse = mouse;
        this.joystick = joystick;

        keys.setInputListener(this);
        mouse.setInputListener(this);
        if (joystick != null) joystick.setInputListener(this);

        firstTime = keys.getInputTimeNanos();
    }

    static final int joyButtonHash(int joyButton){
        return 1536 | (joyButton & 0xff);
    }

    static final int joyAxisHash(int joyAxis, boolean negative){
        return (negative ? 1280 : 1024) | (joyAxis & 0xff);
    }

    private void invokeActions(int hash, boolean pressed){
        ArrayList<Mapping> maps = bindings.get(hash);
        if (maps == null)
            return;

        int size = maps.size();
        for (int i = size - 1; i >= 0; i--){
            Mapping mapping = maps.get(i);
            ArrayList<InputListener> listeners = mapping.listeners;
            int listenerSize = listeners.size();
            for (int j = listenerSize - 1; j >= 0; j--){
                InputListener listener = listeners.get(j);
                if (listener instanceof ActionListener){
                    ((ActionListener)listener).onAction(mapping.name, pressed, frameTPF);
                }
            }
        }
    }

    private final float computeAnalogValue(long timeDelta){
        if (safeMode || frameDelta == 0)
            return 1f;
        else
            return FastMath.clamp((float)timeDelta / (float)frameDelta, 0, 1);
    }

    private void invokeTimedActions(int hash, long time, boolean pressed){
        if (!bindings.containsKey(hash))
            return;

        if (pressed){
            pressedButtons.put(hash, time);
        }else{
            Long pressTimeObj = pressedButtons.remove(hash);
            if (pressTimeObj == null)
                return; // under certain circumstances it can be null, ignore
                        // the event then.
            
            long pressTime   = pressTimeObj;
            long lastUpdate  = lastLastUpdateTime;
            long releaseTime = time;
            long timeDelta = releaseTime - Math.max(pressTime, lastUpdate);

            if (timeDelta > 0)
                invokeAnalogs(hash, computeAnalogValue(timeDelta), false );
        }
    }

    private void invokeUpdateActions(){
        for (Entry<Long> pressedButton : pressedButtons){
            int hash = pressedButton.getKey();

            long pressTime   = pressedButton.getValue();
            long timeDelta = lastUpdateTime - Math.max(lastLastUpdateTime, pressTime);

            if (timeDelta > 0)
                invokeAnalogs(hash, computeAnalogValue(timeDelta), false );
        }
    }

    private void invokeAnalogs(int hash, float value, boolean isAxis){
        ArrayList<Mapping> maps = bindings.get(hash);
        if (maps == null)
            return;

        if (!isAxis)
            value *= frameTPF;

        int size = maps.size();
        for (int i = size - 1; i >= 0; i--){
            Mapping mapping = maps.get(i);
            ArrayList<InputListener> listeners = mapping.listeners;
            int listenerSize = listeners.size();
            for (int j = listenerSize - 1; j >= 0; j--){
                InputListener listener = listeners.get(j);
                if (listener instanceof AnalogListener){
                    // NOTE: multiply by TPF for any button bindings
                    ((AnalogListener)listener).onAnalog(mapping.name, value, frameTPF);
                }
            }
        }
    }

    private void invokeAnalogsAndActions(int hash, float value){
        if (value < axisDeadZone){
            invokeAnalogs(hash, value, true);
            return;
        }

        ArrayList<Mapping> maps = bindings.get(hash);
        if (maps == null)
            return;

        int size = maps.size();
        for (int i = size - 1; i >= 0; i--){
            Mapping mapping = maps.get(i);
            ArrayList<InputListener> listeners = mapping.listeners;
            int listenerSize = listeners.size();
            for (int j = listenerSize - 1; j >= 0; j--){
                InputListener listener = listeners.get(j);

                if (listener instanceof ActionListener)
                    ((ActionListener)listener).onAction(mapping.name, true, frameTPF);

                if (listener instanceof AnalogListener)
                    ((AnalogListener)listener).onAnalog(mapping.name, value, frameTPF);

            }
        }
    }

    public void onJoyAxisEvent(JoyAxisEvent evt) {
        if (!eventsPermitted)
            throw new UnsupportedOperationException("JoyInput has raised an event at an illegal time.");

        for (int i = 0; i < rawListeners.size(); i++){
            rawListeners.get(i).onJoyAxisEvent(evt);
        }

        int axis    = evt.getAxisIndex();
        float value = evt.getValue();
        if (value < 0){
            invokeAnalogsAndActions(joyAxisHash(axis, true), -value);
        }else{
            invokeAnalogsAndActions(joyAxisHash(axis, false), value);
        }
    }

    public void onJoyButtonEvent(JoyButtonEvent evt) {
        if (!eventsPermitted)
            throw new UnsupportedOperationException("JoyInput has raised an event at an illegal time.");

        for (int i = 0; i < rawListeners.size(); i++){
            rawListeners.get(i).onJoyButtonEvent(evt);
        }

        int hash = joyButtonHash(evt.getButtonIndex());
        invokeActions(hash, evt.isPressed());
        invokeTimedActions(hash, evt.getTime(), evt.isPressed());
    }

    public void onMouseMotionEvent(MouseMotionEvent evt) {
        if (!eventsPermitted)
            throw new UnsupportedOperationException("MouseInput has raised an event at an illegal time.");

        for (int i = 0; i < rawListeners.size(); i++){
            rawListeners.get(i).onMouseMotionEvent(evt);
        }

        if (evt.getDX() != 0){
            float val = Math.abs(evt.getDX()) / 1024f;
            invokeAnalogsAndActions(MouseAxisTrigger.mouseAxisHash(MouseInput.AXIS_X, evt.getDX() < 0), val);
        }
        if (evt.getDY() != 0){
            float val = Math.abs(evt.getDY()) / 1024f;
            invokeAnalogsAndActions(MouseAxisTrigger.mouseAxisHash(MouseInput.AXIS_Y, evt.getDY() < 0), val);
        }
        if (evt.getDeltaWheel() != 0){
            float val = Math.abs(evt.getDeltaWheel()) / 100f;
            invokeAnalogsAndActions(MouseAxisTrigger.mouseAxisHash(MouseInput.AXIS_WHEEL, evt.getDeltaWheel() < 0), val);
        }
    }

    public void onMouseButtonEvent(MouseButtonEvent evt) {
        if (!eventsPermitted)
            throw new UnsupportedOperationException("MouseInput has raised an event at an illegal time.");

        for (int i = 0; i < rawListeners.size(); i++){
            rawListeners.get(i).onMouseButtonEvent(evt);
        }

        int hash = MouseButtonTrigger.mouseButtonHash(evt.getButtonIndex());
        invokeActions(hash, evt.isPressed());
        invokeTimedActions(hash, evt.getTime(), evt.isPressed());
    }

    public void onKeyEvent(KeyInputEvent evt){
        if (!eventsPermitted)
            throw new UnsupportedOperationException("KeyInput has raised an event at an illegal time.");

        for (int i = 0; i < rawListeners.size(); i++){
            rawListeners.get(i).onKeyEvent(evt);
        }

        if (evt.isRepeating())
            return; // repeat events not used for bindings

        int hash = KeyTrigger.keyHash(evt.getKeyCode());
        invokeActions(hash, evt.isPressed());
        invokeTimedActions(hash, evt.getTime(), evt.isPressed());
    }

    public void setAxisDeadZone(float deadZone){
        this.axisDeadZone = deadZone;
    }

    public void addListener(InputListener listener, String ... mappingNames){
        for (String mappingName : mappingNames){
            Mapping mapping = mappings.get(mappingName);
            if (mapping == null){
                mapping = new Mapping(mappingName);
                mappings.put(mappingName, mapping);
            }
            if (!mapping.listeners.contains(listener)){
                mapping.listeners.add(listener);
            }
        }
    }

    public void removeListener(InputListener listener){
        for (Mapping mapping : mappings.values()){
            mapping.listeners.remove(listener);
        }
    }

    public void addMapping(String mappingName, Trigger ... triggers){
        Mapping mapping = mappings.get(mappingName);
        if (mapping == null){
            mapping = new Mapping(mappingName);
            mappings.put(mappingName, mapping);
        }

        for (Trigger trigger : triggers){
            int hash = trigger.hashCode();
            ArrayList<Mapping> names = bindings.get(hash);
            if (names == null){
                names = new ArrayList<Mapping>();
                bindings.put(hash, names);
            }
            if (!names.contains(mapping)){
                names.add(mapping);
                mapping.triggers.add(hash);
            }else{
                logger.log(Level.WARNING, "Attempted to add mapping '" + mappingName + "' twice to trigger.");
            }
        }
    }

    public void deleteMapping(String mappingName){
        Mapping mapping = mappings.remove(mappingName);
        if (mapping == null)
            throw new IllegalArgumentException("Cannot find mapping: "+mappingName);

        ArrayList<Integer> triggers = mapping.triggers;
        for (int i = triggers.size() - 1; i >= 0; i--){
            int hash = triggers.get(i);
            ArrayList<Mapping> maps = bindings.get(hash);
            maps.remove(mapping);
        }
    }

    /**
     * Called to reset pressed keys or buttons when focus is restored.
     */
    public void reset(){
        pressedButtons.clear();
    }

    /**
     * @param visible whether the mouse cursor should be visible or not.
     */
    public void setCursorVisible(boolean visible){
        if (mouseVisible != visible){
            mouseVisible = visible;
            mouse.setCursorVisible(mouseVisible);
        }
    }

    public void addRawInputListener(RawInputListener listener){
        rawListeners.add(listener);
    }

    public void removeRawInputListener(RawInputListener listener){
        rawListeners.remove(listener);
    }

    public void clearRawInputListeners(){
        rawListeners.clear();
    }

    /**
     * Updates the Dispatcher. This will query current input devices and send
     * appropriate events to registered listeners.
     *
     * @param tpf Time per frame value.
     */
    public void update(float tpf){
        frameTPF = tpf;
        safeMode = tpf < 0.015f;
        long currentTime = keys.getInputTimeNanos();
        frameDelta = currentTime - lastUpdateTime;

        eventsPermitted = true;

        keys.update();
        mouse.update();
        if (joystick != null) joystick.update();

        eventsPermitted = false;

        invokeUpdateActions();

        lastLastUpdateTime = lastUpdateTime;
        lastUpdateTime = currentTime;
    }

}
