package com.jme3.system.jogl;

import com.jme3.system.AppSettings;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class JoglDisplay extends JoglAbstractDisplay {

    private static final Logger logger = Logger.getLogger(JoglDisplay.class.getName());

    protected AtomicBoolean windowCloseRequest = new AtomicBoolean(false);
    protected AtomicBoolean needClose = new AtomicBoolean(false);
    protected Frame frame;

    public Type getType() {
        return Type.Display;
    }

    protected DisplayMode getFullscreenDisplayMode(DisplayMode[] modes, int width, int height, int bpp, int freq){
        for (DisplayMode mode : modes){
            if (mode.getWidth() == width
             && mode.getHeight() == height
             && (mode.getBitDepth() == DisplayMode.BIT_DEPTH_MULTI || mode.getBitDepth() == bpp)
             && mode.getRefreshRate() == freq){
                return mode;
            }
        }
        return null;
    }

    protected void applySettings(AppSettings settings){
        DisplayMode displayMode;
        if (settings.getWidth() <= 0 || settings.getHeight() <= 0){
            displayMode = device.getDisplayMode();
            settings.setResolution(displayMode.getWidth(), displayMode.getHeight());
        }else if (settings.isFullscreen()){
            displayMode = getFullscreenDisplayMode(device.getDisplayModes(),
                                                   settings.getWidth(), settings.getHeight(),
                                                   settings.getBitsPerPixel(), settings.getFrequency());
            if (displayMode == null)
                throw new RuntimeException("Unable to find fullscreen display mode matching settings");
        }else{
            displayMode = new DisplayMode(settings.getWidth(), settings.getHeight(), 0, 0);
        }

        // FIXME: seems to return false even though
        // it is supported..
//        if (!device.isDisplayChangeSupported()){
//            // must use current device mode if display mode change not supported
//            displayMode = device.getDisplayMode();
//            settings.setResolution(displayMode.getWidth(), displayMode.getHeight());
//        }

        frameRate = settings.getFrameRate();
        logger.info("Selected display mode: "+displayMode.getWidth()
                                             +"x"+displayMode.getHeight()+
                                             "x"+displayMode.getBitDepth()+
                                             " @"+displayMode.getRefreshRate());
        canvas.setSize(displayMode.getWidth(), displayMode.getHeight());

        DisplayMode prevDisplayMode = device.getDisplayMode();

        if (settings.isFullscreen() && device.isFullScreenSupported()){
            frame.setUndecorated(true);

            try{
                device.setFullScreenWindow(frame);
                if (!prevDisplayMode.equals(displayMode)
                  && device.isDisplayChangeSupported()){
                    device.setDisplayMode(displayMode);
                }
            } catch (Throwable t){
                logger.log(Level.SEVERE, "Failed to enter fullscreen mode", t);
                device.setFullScreenWindow(null);
            }
        }else{
            if (!device.isFullScreenSupported()){
                logger.warning("Fullscreen not supported.");
            }

            frame.setVisible(true);
        }
    }

    private void initInEDT(){
        initGLCanvas();

        Container contentPane;
        if (useAwt){
            frame = new Frame(settings.getTitle());
            contentPane = frame;
        }else{
            frame = new JFrame(settings.getTitle());
            contentPane = ((JFrame)frame).getContentPane();
        }
        
        contentPane.setLayout(new BorderLayout());

        applySettings(settings);

        frame.setResizable(false);
        frame.setFocusable(true);

        // only add canvas after frame is visible
        contentPane.add(canvas, BorderLayout.CENTER);

        if (device.getFullScreenWindow() == null){
            // now that canvas is attached,
            // determine optimal size to contain it
            frame.setSize(contentPane.getPreferredSize());

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setLocation((screenSize.width - frame.getWidth()) / 2,
                              (screenSize.height - frame.getHeight()) / 2);
        }

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                windowCloseRequest.set(true);
            }
            @Override
            public void windowClosed(WindowEvent evt) {
            }

            @Override
            public void windowActivated(WindowEvent evt) {
                active.set(true);
            }

            @Override
            public void windowDeactivated(WindowEvent evt) {
                active.set(false);
            }
        });

        startGLCanvas();
    }

    public void init(GLAutoDrawable drawable){
        canvas.requestFocus();

        super.internalCreate();
        logger.info("Display created.");

        renderer.initialize();
        listener.initialize();
    }

    public void create(boolean waitFor){
        try {
            if (waitFor){
                try{
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            initInEDT();
                        }
                    });
                } catch (InterruptedException ex) {
                    listener.handleError("Interrupted", ex);
                }
            }else{
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        initInEDT();
                    }
                });
            }
        } catch (InvocationTargetException ex) {
            throw new AssertionError(); // can never happen
        }
    }

    public void destroy(boolean waitFor){
        needClose.set(true);
        if (waitFor){
            waitFor(false);
        }
    }

    public void restart() {
    }

    public void setTitle(String title){
        if (frame != null)
            frame.setTitle(title);
    }

    /**
     * Callback.
     */
    public void display(GLAutoDrawable drawable) {
        if (needClose.get()) {
            listener.destroy();
            animator.stop();
            if (settings.isFullscreen()) {
                device.setFullScreenWindow(null);
            }
            frame.dispose();
            logger.info("Display destroyed.");
            super.internalDestroy();
            return;
        }

        if (windowCloseRequest.get()){
            listener.requestClose(false);
            windowCloseRequest.set(false);
        }

//        boolean flush = autoFlush.get();
//        if (animator.isAnimating() != flush){
//            if (flush)
//                animator.stop();
//            else
//                animator.start();
//        }

        if (wasActive != active.get()){
            if (!wasActive){
                listener.gainFocus();
                wasActive = true;
            }else{
                listener.loseFocus();
                wasActive = false;
            }
        }

        listener.update();
        renderer.onFrame();
    }
}
