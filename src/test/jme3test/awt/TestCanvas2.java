package jme3test.awt;

import com.jme3.app.Application;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.system.JmeSystem;
import java.awt.Canvas;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class TestCanvas2 {

    private static JmeCanvasContext context;
    private static Canvas canvas;
    private static Application app;
    private static JFrame frame;

    private static void createFrame(){
        frame = new JFrame("Test");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public static void createCanvas(String appClass){
        AppSettings settings = new AppSettings(true);
        settings.setWidth(640);
        settings.setHeight(480);

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
        app.startCanvas();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void freezeApp(){
        frame.getContentPane().remove(canvas);
    }

    public void unfreezeApp(){
        frame.getContentPane().add(canvas);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                String appClass = "jme3test.model.shape.TestBox";
                createCanvas(appClass);
                createFrame();
                frame.getContentPane().add(canvas);
                frame.pack();
                startApp();
            }
        });
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
        }
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                frame.dispose();
            }
        });
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestCanvas2.class.getName()).log(Level.SEVERE, null, ex);
        }
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                createFrame();
                frame.getContentPane().add(canvas);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.requestFocus();
            }
        });
    }

}
