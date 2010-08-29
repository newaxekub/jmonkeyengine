/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jme3test.awt;

import com.jme3.app.Application;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.system.JmeSystem;
import java.applet.Applet;
import java.awt.Canvas;
import java.awt.Graphics;
import javax.swing.SwingUtilities;

/**
 *
 * @author Kirill
 */
public class AppHarness extends Applet {

    private JmeCanvasContext context;
    private Canvas canvas;
    private Application app;
    private String appClass;

    private void createCanvas(){
        AppSettings settings = new AppSettings(true);
        settings.setWidth(getWidth());
        settings.setHeight(getHeight());
        settings.setAudioRenderer(null);

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
        canvas.setSize(getWidth(), getHeight());
        
        add(canvas);
        app.startCanvas();
    }

    public final void update(Graphics g) {
        canvas.setSize(getWidth(), getHeight());
    }

    public void init(){
        appClass = getParameter("AppClass");
        if (appClass == null)
            throw new RuntimeException("The required parameter AppClass isnt specified!");
        
        createCanvas();
        System.out.println("applet:init");
    }

    public void start(){
        context.setAutoFlushFrames(true);
        System.out.println("applet:start");
    }

    public void stop(){
        context.setAutoFlushFrames(false);
        System.out.println("applet:stop");
    }

    public void destroy(){
        System.out.println("applet:destroyStart");
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                removeAll();
                System.out.println("applet:destroyRemoved");
            }
        });
        app.stop(true);
        System.out.println("applet:destroyDone");
    }

}
