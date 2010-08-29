package com.jme3.video;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VQueue extends ArrayBlockingQueue<VFrame> {

//    private final ArrayList<VFrame> returnedFrames;
    private final ArrayBlockingQueue<VFrame> returnedFrames;
    
    public VQueue(int bufferedFrames){
        super(bufferedFrames);
//        returnedFrames = new ArrayList<VFrame>(remainingCapacity());
        returnedFrames = new ArrayBlockingQueue<VFrame>(bufferedFrames * 3);
    }

    public VFrame nextReturnedFrame(boolean waitForIt){
        //        synchronized (returnedFrames){
        //            while (returnedFrames.size() == 0){
        //                if (!waitForIt)
        //                    return null;
        //
        //                try {
        //                    returnedFrames.wait();
        //                } catch (InterruptedException ex) {
        //                }
        //            }
        //        }
        //        }

        try {
            return returnedFrames.take();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void returnFrame(VFrame frame){
        returnedFrames.add(frame);

//        synchronized (returnedFrames){
//            returnedFrames.add(frame);
//            returnedFrames.notifyAll();
//        }
    }
}
