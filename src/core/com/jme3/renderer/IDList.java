package com.jme3.renderer;

import java.util.Arrays;

/**
 * A specialized data-structure used to optimize state changes of "slot"
 * based state. 
 */
public class IDList {

    public int[] newList = new int[16];
    public int[] oldList = new int[16];
    public int newLen = 0;
    public int oldLen = 0;

    public void reset(){
        newLen = 0;
        oldLen = 0;
        Arrays.fill(newList, 0);
        Arrays.fill(oldList, 0);
    }

    public boolean moveToNew(int idx){
        if (newLen == 0 || newList[newLen-1] != idx)
            // add item to newList first
            newList[newLen++] = idx;

        // find idx in oldList, if removed successfuly, return true.
        for (int i = 0; i < oldLen; i++){
            if (oldList[i] == idx){
                // found index in slot i
                // delete index from old list
                oldLen --;
                for (int j = i; j < oldLen; j++){
                    oldList[j] = oldList[j+1];
                }
                return true;
            }
        }
        return false;
    }

    public void copyNewToOld(){
        System.arraycopy(newList, 0, oldList, 0, newLen);
        oldLen = newLen;
        newLen = 0;
    }

    public void print(){
        if (newLen > 0){
            System.out.print("New List: ");
            for (int i = 0; i < newLen; i++){
                if (i == newLen -1)
                    System.out.println(newList[i]);
                else
                    System.out.print(newList[i]+", ");
            }
        }
        if (oldLen > 0){
            System.out.print("Old List: ");
            for (int i = 0; i < oldLen; i++){
                if (i == oldLen -1)
                    System.out.println(oldList[i]);
                else
                    System.out.print(oldList[i]+", ");
            }
        }
    }

}
