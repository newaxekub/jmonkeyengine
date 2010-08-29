package jme3test.awt;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.system.JmeSystem;
import java.applet.Applet;
import java.awt.Canvas;
import java.awt.Graphics;
import java.util.concurrent.Callable;
import javax.swing.SwingUtilities;

public class TestApplet extends Applet {

    private static JmeCanvasContext context;
    private static Application app;
    private static Canvas canvas;
    private static TestApplet applet;

    public TestApplet(){
    }

    public static void createCanvas(String appClass){
        AppSettings settings = new AppSettings(true);
        settings.setWidth(640);
        settings.setHeight(480);
//        settings.setRenderer(AppSettings.JOGL);

        JmeSystem.setLowPermissions(true);

        try{
            Class<? extends Application> clazz = (Class<? extends Application>) Class.forName(appClass);
            app = clazz.newInstance();
        }catch (ClassNotFoundException ex){
            ex.printStackTrace();
        }catch (InstantiationException ex){
            ex.printStackTrace();
        }catch (IllegalAccessException ex){
            ex.printStackTrace();
        }

        app.setSettings(settings);
        app.createCanvas();
        
        context = (JmeCanvasContext) app.getContext();
        canvas = context.getCanvas();
        canvas.setSize(settings.getWidth(), settings.getHeight());
    }

    public static void startApp(){
        applet.add(canvas);
        app.startCanvas();

        app.enqueue(new Callable<Void>(){
            public Void call(){
                if (app instanceof SimpleApplication){
                    SimpleApplication simpleApp = (SimpleApplication) app;
                    simpleApp.getFlyByCamera().setDragToRotate(true);
                    simpleApp.getInputManager().setCursorVisible(true);
                }
                return null;
            }
        });
    }

    public void freezeApp(){
        remove(canvas);
    }

    public void unfreezeApp(){
        add(canvas);
    }

    @Override
    public final void update(Graphics g) {
//        canvas.setSize(getWidth(), getHeight());
    }

    @Override
    public void init(){
        applet = this;
        createCanvas("jme3test.model.shape.TestBox");
        startApp();
        app.setPauseOnLostFocus(false);
        System.out.println("applet:init");
    }

    @Override
    public void start(){
//        context.setAutoFlushFrames(true);
        System.out.println("applet:start");
    }

    @Override
    public void stop(){
//        context.setAutoFlushFrames(false);
        System.out.println("applet:stop");
    }

    @Override
    public void destroy(){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                removeAll();
                System.out.println("applet:destroyStart");
            }
        });
        app.stop(true);
        System.out.println("applet:destroyEnd");
    }

}
