/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.cinematic;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nehon
 */
public class KeyFrame {
    List<CinematicEvent> cinematicEvents=new ArrayList<CinematicEvent>();

    public List<CinematicEvent> getCinematicEvents() {
        return cinematicEvents;
    }

    public void setCinematicEvents(List<CinematicEvent> cinematicEvents) {
        this.cinematicEvents = cinematicEvents;
    }

    public List<CinematicEvent> trigger(){
        for(CinematicEvent event : cinematicEvents){
            event.play();
        }
        return cinematicEvents;
    }

}
