/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.gde.gui.multiview;

import com.jme3.gde.gui.NiftyGuiDataObject;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.java2d.input.InputSystemAwtImpl;
import de.lessvoid.nifty.java2d.renderer.RenderDeviceJava2dImpl;
import de.lessvoid.nifty.nulldevice.NullSoundDevice;
import de.lessvoid.nifty.tools.TimeProvider;
import java.awt.Canvas;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.ui.PanelView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author normenhansen
 */
public class NiftyPreviewPanel extends PanelView{
    private Nifty nifty;
    private RenderDeviceJava2dImpl dev;
    private NiftyGuiDataObject niftyObject;
    private FileObject file=null;
    private Thread thread;
    private String fileName;

    public NiftyPreviewPanel(NiftyGuiDataObject niftyObject) {
        super();
        this.niftyObject=niftyObject;
        setRoot(Node.EMPTY);
    }

    public void initComponents() {
        super.initComponents();
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));
        Canvas can=new Canvas();
        dev=new RenderDeviceJava2dImpl(can);
//        dev.
        add(can);
//        add(dev.);
        fileName="assets/gui/StartGui.xml";
        if(niftyObject!=null){
            Set<FileObject> files = niftyObject.files();
            for (Iterator<FileObject> it = files.iterator(); it.hasNext();) {
                FileObject fileObject = it.next();
                file=fileObject;//getNameExt();
            }
            fileName=file.getPath();
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Could not find niftyObject");
        }
        InputSystemAwtImpl awtInput=new InputSystemAwtImpl();
        can.addMouseListener(awtInput);
        can.addMouseMotionListener(awtInput);
        nifty = new Nifty(dev,
                    new NullSoundDevice(),
                    awtInput,
                    new TimeProvider());
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "nify started");
    }

    public void start(){
        if(running) stop();
//        thread=new Thread(run);
//        thread.start();
    }

    public void stop(){
        running=false;
//        try {
//            thread.join();
//        } catch (InterruptedException ex) {
//            Exceptions.printStackTrace(ex);
//        }
    }

    private boolean running=false;
    private Runnable run=new Runnable() {
        public void run() {
            running=true;
            nifty.fromXml(fileName,"start");//
            while(running){
                if(nifty.render(true)){
                    running=false;
                }
            }
        }
    };

    @Override
    protected Error validateView() {
        return null;
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void showSelection(Node[] nodes) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

}
