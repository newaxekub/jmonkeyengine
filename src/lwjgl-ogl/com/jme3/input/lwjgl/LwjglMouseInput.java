package com.jme3.input.lwjgl;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.MouseInput;
import com.jme3.input.RawInputListener;
import com.jme3.system.lwjgl.LwjglTimer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;

public class LwjglMouseInput implements MouseInput {

    private static final Logger logger = Logger.getLogger(LwjglMouseInput.class.getName());

    private RawInputListener listener;

    private boolean supportHardwareCursor = false;
    private boolean cursorVisible = true;

    private int curX, curY, curWheel;

    public void initialize() {
        try {
            Mouse.create();
            logger.info("Mouse created.");
            supportHardwareCursor = (Cursor.getCapabilities() & Cursor.CURSOR_ONE_BIT_TRANSPARENCY) != 0;
        } catch (LWJGLException ex) {
            logger.log(Level.SEVERE, "Error while creating mouse", ex);
        }
    }

    public boolean isInitialized(){
        return Mouse.isCreated();
    }

    public int getButtonCount(){
        return Mouse.getButtonCount();
    }

    public void update() {
        while (Mouse.next()){
            int btn = Mouse.getEventButton();

            int wheelDelta = Mouse.getEventDWheel();
            int xDelta = Mouse.getEventDX();
            int yDelta = Mouse.getEventDY();
            int x = Mouse.getX();
            int y = Mouse.getY();

            curWheel += wheelDelta;
            if (cursorVisible){
                xDelta = x - curX;
                yDelta = y - curY;
                curX = x;
                curY = y;
            }else{
                x = curX + xDelta;
                y = curY + yDelta;
                curX = x;
                curY = y;
            }

            if (xDelta != 0 || yDelta != 0 || wheelDelta != 0){
                MouseMotionEvent evt = new MouseMotionEvent(x, y, xDelta, yDelta, curWheel, wheelDelta);
                listener.onMouseMotionEvent(evt);
            }
            if (btn != -1){
                MouseButtonEvent evt = new MouseButtonEvent(btn,
                                                            Mouse.getEventButtonState());
                listener.onMouseButtonEvent(evt);
            }
        }
    }

    public void destroy() {
        Mouse.destroy();
        logger.info("Mouse destroyed.");
    }

    public void setCursorVisible(boolean visible){
        Mouse.setGrabbed(!visible);
        cursorVisible = visible;
    }

    public void setInputListener(RawInputListener listener) {
        this.listener = listener;
    }

    public long getInputTimeNanos() {
        return Sys.getTime() * LwjglTimer.LWJGL_TIME_TO_NANOS;
    }

}
