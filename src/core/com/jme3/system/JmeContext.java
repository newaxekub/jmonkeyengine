package com.jme3.system;

import com.jme3.input.JoyInput;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.renderer.Renderer;

/**
 * Represents a rendering context within the engine.
 */
public interface JmeContext {

    /**
     * The type of context.
     */
    public enum Type {
        /**
         * A display can represent a windowed or a fullscreen-exclusive display.
         * If windowed, the graphics are rendered to a new onscreen surface
         * enclosed in a system defined by the operating system. Implementations
         * are encourged to not use AWT or Swing to create the OpenGL display
         * but rather use native operating system functions to set up a native
         * display with the windowing system.
         */
        Display,
        
        /**
         * 
         */
        Canvas,
        
        /**
         * An <code>OffscreenSurface</code> is a context that is not visible
         * by the user. The application can use the offscreen surface to do
         * General Purpose GPU computations or render a scene into a buffer
         * in order to save it as a screenshot, video or send through a network.
         */
        OffscreenSurface
    }

    /**
     * @return The type of the context.
     */
    public Type getType();
    
    /**
     * @param settings the display settings to use for the created context. If
     * the context has already been created, then <code>restart()</code> must be called
     * for the changes to be applied.
     */
    public void setSettings(AppSettings settings);

    /**
     * Sets the listener that will recieve events relating to context
     * creation, update, and destroy.
     */
    public void setSystemListener(SystemListener listener);

    /**
     * @return The current display settings. Note that they might be 
     * different from the ones set with setDisplaySettings() if the context
     * was restarted or the settings changed internally.
     */
    public AppSettings getSettings();

    /**
     * @return The renderer for this context, or null if not created yet.
     */
    public Renderer getRenderer();

    /**
     * @return Mouse input implementation. May be null if not available.
     */
    public MouseInput getMouseInput();

    /**
     * @return Keyboard input implementation. May be null if not available.
     */
    public KeyInput getKeyInput();

    /**
     * @return Joystick input implementation. May be null if not available.
     */
    public JoyInput getJoyInput();

    /**
     * @return The timer for this context, or null if not created yet.
     */
    public Timer getTimer();
    
    /**
     * Sets the title of the display (if available). This does nothing
     * for fullscreen, headless, or canvas contexts.
     * @param title The new title of the display.
     */
    public void setTitle(String title);

    /**
     * @return True if the context has been created but not yet destroyed.
     */
    public boolean isCreated();

    /**
     * @param enabled If enabled, the context will automatically flush
     * frames to the video card (swap buffers) after an update cycle.
     */
    public void setAutoFlushFrames(boolean enabled);

    /**
     * Creates the context and makes it active.
     */
    @Deprecated
    public void create();

    /**
     * Creates the context and makes it active.
     *
     * @param waitFor If true, will wait until context has initialized.
     */
    public void create(boolean waitFor);

    /**
     * Destroys and then re-creates the context. This should be called after
     * the display settings have been changed.
     */
    public void restart();

    /**
     * Destroys the context completely, making it inactive.
     */
    @Deprecated
    public void destroy();

    /**
     * Destroys the context completely, making it inactive.
     *
     * @param waitFor If true, will wait until the context is destroyed fully.
     */
    public void destroy(boolean waitFor);

}
