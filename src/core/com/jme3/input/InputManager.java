package com.jme3.input;

import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.binding.BindingListener;
import com.jme3.util.IntMap;
import com.jme3.util.IntMap.Entry;
import java.util.ArrayList;

/**
 * The <code>InputManager</code> is responsible for converting input events
 * recieved from the Key, Mouse and Joy Input implementations into an
 * abstract, input device independent representation that user code can use.
 *
 * By default a dispatcher is included with every Application instance for use
 * in user code to query input, unless the Application is created as headless
 * or with input explicitly disabled.
 */
public class InputManager implements RawInputListener {

    private MouseInput mouseInput;
    private KeyInput keyInput;
    private JoyInput joyInput;

    private IntMap<String> keyBindings = new IntMap<String>();
    private IntMap<String> mouseBtnBindings = new IntMap<String>();
    private IntMap<String> mouseAxisBindings = new IntMap<String>();
    private IntMap<String> joyAxisBindings = new IntMap<String>();
    private IntMap<String> joyButtonBindings = new IntMap<String>();

    private ArrayList<BindingListener> listeners = new ArrayList<BindingListener>();
    private ArrayList<RawInputListener> rawListeners = new ArrayList<RawInputListener>();

    private MouseButtonEvent lastButtonEvent = null;
    private KeyInputEvent lastKeyEvent = null;
    private float frameTPF = -1f;
    private boolean mouseVisible = true;

    private boolean[] keyboard;
    private boolean[] mouse;
    private float[][] joyAxes;
    private boolean[][] joyButtons;

    /**
     * Initializes the Dispatcher.
     *
     * @param mouseInput
     * @param keyInput
     * @param joyInput
     * @throws IllegalArgumentException If either mouseInput or keyInput are null.
     */
    public InputManager(MouseInput mouseInput, KeyInput keyInput, JoyInput joyInput){
        if (mouseInput == null || keyInput == null)
            throw new IllegalArgumentException("Mouse and key input cannot be null");

        this.keyInput = keyInput;
        keyboard = new boolean[255];
        keyInput.setInputListener(this);

        this.mouseInput = mouseInput;
        mouse = new boolean[mouseInput.getButtonCount()];
        mouseInput.setInputListener(this);
        
        if (joyInput != null){
            this.joyInput = joyInput;
            joyInput.setInputListener(this);
            joyAxes = new float[joyInput.getJoyCount()][];
            joyButtons = new boolean[joyInput.getJoyCount()][];
            for (int i = 0; i < joyAxes.length; i++){
                joyAxes[i] = new float[7]; // because using virtual axes
                joyButtons[i] = new boolean[joyInput.getButtonCount(i)];
            }
        }
    }

    private void notifyListeners(String name, float value){
        if (name == null)
            return;

        for (int i = 0; i < listeners.size(); i++){
            listeners.get(i).onBinding(name, value);
        }
    }

    /**
     * Called to reset pressed keys or buttons when focus is restored.
     */
    public void reset(){
    }

    /**
     * @param visible whether the mouse cursor should be visible or not.
     */
    public void setCursorVisible(boolean visible){
        if (mouseVisible != visible){
            mouseVisible = visible;
            mouseInput.setCursorVisible(mouseVisible);
        }
    }

    public void onMouseMotionEvent(MouseMotionEvent evt) {
        for (int i = 0; i < rawListeners.size(); i++){
            rawListeners.get(i).onMouseMotionEvent(evt);
        }

        if (evt.getDX() > 0){
            // positive X axis
            String name = mouseAxisBindings.get(1);
            float value = evt.getDX() / 1024f;
            notifyListeners(name, value);
        }else if (evt.getDX() < 0){
            String name = mouseAxisBindings.get(-1);
            float value = evt.getDX() / -1024f;
            notifyListeners(name, value);
        }
        if (evt.getDY() > 0){
            String name = mouseAxisBindings.get(2);
            float value = evt.getDY() / 1024f;
            notifyListeners(name, value);
        }else if (evt.getDY() < 0){
            String name = mouseAxisBindings.get(-2);
            float value = evt.getDY() / -1024f;
            notifyListeners(name, value);
        }
        if (evt.getDeltaWheel() > 0){
            String name = mouseAxisBindings.get(3);
            float value = evt.getDeltaWheel() / 100f;
            notifyListeners(name, value);
        }else if (evt.getDeltaWheel() < 0){
            String name = mouseAxisBindings.get(-3);
            float value = evt.getDeltaWheel() / -100f;
            notifyListeners(name, value);
        }
    }

    public void onMouseButtonEvent(MouseButtonEvent evt) {
        for (int i = 0; i < rawListeners.size(); i++){
            rawListeners.get(i).onMouseButtonEvent(evt);
        }
        mouse[evt.getButtonIndex()] = evt.isPressed();
        
        String name = mouseBtnBindings.get(evt.getButtonIndex());
        if (name == null)
            return;

        if (lastButtonEvent != null
         && lastButtonEvent.getButtonIndex() == evt.getButtonIndex()
         && lastButtonEvent.isPressed() && evt.isReleased()){
            long delta = evt.getTime() - lastButtonEvent.getTime();
            float seconds = delta / (1000000000f); // convert nanoseconds to seconds

            // for how long was the key pressed relative to the current
            // frame time? deltaSeconds / frameSeconds
            notifyListeners(name, seconds);
        }
        lastButtonEvent = evt;
    }

    public void onKeyEvent(KeyInputEvent evt) {
        for (int i = 0; i < rawListeners.size(); i++){
            rawListeners.get(i).onKeyEvent(evt);
        }

        if (evt.isRepeating())
            return; // repeat events not used for bindings

        // update keyboard state
        keyboard[evt.getKeyCode()] = evt.isPressed();
        
        // check if binding set for key
        String name = keyBindings.get(evt.getKeyCode());
        if (name == null)
            return;

        // this part is used for really short key presses (less than
        // one frame long).
        if (lastKeyEvent != null
         && lastKeyEvent.getKeyCode() == evt.getKeyCode()
         && lastKeyEvent.isPressed() && evt.isReleased()){
            long delta = evt.getTime() - lastKeyEvent.getTime();
            float seconds = delta / (1000000000f); // convert nanoseconds to seconds

            // for how long was the key pressed relative to the current
            // frame time? deltaSeconds / frameSeconds
            notifyListeners(name, seconds);
        }
        lastKeyEvent = evt;
    }

    public void onJoyAxisEvent(JoyAxisEvent evt) {
        for (int i = 0; i < rawListeners.size(); i++){
            rawListeners.get(i).onJoyAxisEvent(evt);
        }

        joyAxes[evt.getJoyIndex()][evt.getAxisIndex()] = evt.getValue();
    }

    public void onJoyButtonEvent(JoyButtonEvent evt) {
        for (int i = 0; i < rawListeners.size(); i++){
            rawListeners.get(i).onJoyButtonEvent(evt);
        }
    }

    /**
     * Updates the Dispatcher. This will query current input devices and send
     * appropriate events to registered listeners.
     *
     * @param tpf Time per frame value.
     */
    public void update(float tpf){
        lastButtonEvent = null;
        lastKeyEvent = null;
        frameTPF = tpf;

        for (int i = 0; i < listeners.size(); i++){
            listeners.get(i).onPreUpdate(tpf);
        }

        // query current keyboard state for all bindings
        for (Entry<String> entry : keyBindings){
            if (entry.getKey() >= keyboard.length)
                continue;

            if (keyboard[entry.getKey()]){
                // key is currently down
                notifyListeners(entry.getValue(), tpf);
            }
        }

        // query current mouse button state for all bindings
        for (Entry<String> entry : mouseBtnBindings){
            if (entry.getKey() >= mouse.length)
                continue;
            
            if (mouse[entry.getKey()]){
                // mouse is currently down
                notifyListeners(entry.getValue(), tpf);
            }
        }

        if (joyInput != null){
            for (Entry<String> entry : joyAxisBindings){
                int axisId = entry.getKey();
                boolean negative = false;
                if (axisId < 0){
                    axisId = -axisId;
                    negative = true;
                }
                int joyIndex = (axisId & 0xFFFF) - 1;
                int axisIndex = ((axisId & 0xFFFF0000) >> 16) - 1;
                if (joyIndex >= joyAxes.length)
                    continue;

                float value = joyAxes[joyIndex][axisIndex];
                if (value > 0 && !negative){
                    // key is currently down
                    notifyListeners(entry.getValue(), tpf * value);
                }else if (value < 0 && negative){
                    notifyListeners(entry.getValue(), -tpf * value);
                }
            }
        }

        for (int i = 0; i < listeners.size(); i++){
            listeners.get(i).onPostUpdate(tpf);
        }
    }

    /**
     * Registers a keyboard key binding.
     * @param name
     * @param keyCode
     */
    public void registerKeyBinding(String name, int keyCode){
        keyBindings.put(keyCode, name);
    }

    /**
     * Registers a mouse button binding.
     * @param name
     * @param btnIndex
     */
    public void registerMouseButtonBinding(String name, int btnIndex){
        mouseBtnBindings.put(btnIndex, name);
    }

    /**
     * Registers a mouse axis binding
     * @param name
     * @param mouseAxis 0 is X axis, 1 is Y axis, 2 is wheel
     * @param negative If true, the event will be invoked when the axis is in
     * the negative direction, otherwise it will be invoked on positive direction.
     */
    public void registerMouseAxisBinding(String name, int mouseAxis, boolean negative){
        mouseAxis ++; // makes axis 0 (X) become 1 & axis 1 (Y) become 2
        if (negative)
            mouseAxis = -mouseAxis;

        mouseAxisBindings.put(mouseAxis, name);
    }

    public void registerJoystickAxisBinding(String name, int joyIndex, int axisIndex, boolean negative){
        if (joyInput == null)
            return;

        axisIndex ++;
        joyIndex ++;
        int axisId = joyIndex | (axisIndex << 16);
        if (negative)
            axisId = -axisId;
        
        joyAxisBindings.put(axisId, name);
    }

    /**
     * Adds a trigger listener. Listeners will be invoked when the binding
     * value is greater than zero.
     *
     * @param listener
     */
    public void addBindingListener(BindingListener listener){
        listeners.add(listener);
    }

    /**
     * Remove a previously added trigger listener.
     * @param listener
     */
    public void removeBindingListener(BindingListener listener){
        listeners.remove(listener);
    }

    /**
     * Removes all trigger listeners from the list of registered listnerers.
     */
    public void clearBindingListeners(){
        listeners.clear();
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

}
