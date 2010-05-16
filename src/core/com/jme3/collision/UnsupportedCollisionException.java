/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.collision;

/**
 *
 * @author Kirill
 */
public class UnsupportedCollisionException extends UnsupportedOperationException {

    public UnsupportedCollisionException(Throwable arg0) {
        super(arg0);
    }

    public UnsupportedCollisionException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public UnsupportedCollisionException(String arg0) {
        super(arg0);
    }

    public UnsupportedCollisionException() {
        super();
    }
    
}
