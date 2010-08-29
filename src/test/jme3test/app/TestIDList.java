package jme3test.app;

import com.jme3.renderer.IDList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class TestIDList {

    static class StateCol {

        static Random rand = new Random();

        Map<Integer, Object> objs = new HashMap<Integer, Object>();

        public StateCol(){
            // populate with free ids
            List<Integer> freeIds = new ArrayList();
            for (int i = 0; i < 16; i++){
                freeIds.add(i);
            }

            // create random
            int numStates = rand.nextInt(6) + 1;
            for (int i = 0; i < numStates; i++){
                // remove a random id from free id list
                int idx = rand.nextInt(freeIds.size());
                int id = freeIds.remove(idx);

                objs.put(id, new Object());
            }
        }

        public void print(){
            System.out.println("-----------------");

            Set<Integer> keys = objs.keySet();
            Integer[] keysArr = keys.toArray(new Integer[0]);
            Arrays.sort(keysArr);
            for (int i = 0; i < keysArr.length; i++){
                System.out.println(keysArr[i]+" => "+objs.get(keysArr[i]).hashCode());
            }
        }

    }

    static IDList list = new IDList();
    static int boundSlot = 0;
    
    static Object[] slots = new Object[16];
    static boolean[] enabledSlots = new boolean[16];

    static void enable(int slot){
        System.out.println("Enabled SLOT["+slot+"]");
        if (enabledSlots[slot] == true){
            System.err.println("FAIL! Extra state change");
        }
        enabledSlots[slot] = true;
    }

    static void disable(int slot){
        System.out.println("Disabled SLOT["+slot+"]");
        if (enabledSlots[slot] == false){
            System.err.println("FAIL! Extra state change");
        }
        enabledSlots[slot] = false;
    }

    static void setSlot(int slot, Object val){
        if (!list.moveToNew(slot)){
            enable(slot);
        }
        if (slots[slot] != val){
            System.out.println("SLOT["+slot+"] = "+val.hashCode());
            slots[slot] = val;
        }
    }

    static void checkSlots(StateCol state){
        for (int i = 0; i < 16; i++){
            if (slots[i] != null && enabledSlots[i] == false){
                System.err.println("FAIL! SLOT["+i+"] assigned, but disabled");
            }
            if (slots[i] == null && enabledSlots[i] == true){
                System.err.println("FAIL! SLOT["+i+"] enabled, but not assigned");
            }

            Object val = state.objs.get(i);
            if (val != null){
                if (slots[i] != val)
                    System.err.println("FAIL! SLOT["+i+"] does not contain correct value");
                if (!enabledSlots[i])
                    System.err.println("FAIL! SLOT["+i+"] is not enabled");
            }else{
                if (slots[i] != null)
                    System.err.println("FAIL! SLOT["+i+"] is not set");
                if (enabledSlots[i])
                    System.err.println("FAIL! SLOT["+i+"] is enabled");
            }
        }
    }

    static void clearSlots(){
        for (int i = 0; i < list.oldLen; i++){
            int slot = list.oldList[i];
            disable(slot);
            slots[slot] = null;
        }
        list.copyNewToOld();
//        context.attribIndexList.print();
    }
    
    static void setState(StateCol state){
        state.print();
        for (Map.Entry<Integer, Object> entry : state.objs.entrySet()){
            setSlot(entry.getKey(), entry.getValue());
        }
        clearSlots();
        checkSlots(state);
    }

    public static void main(String[] args){
        StateCol[] states = new StateCol[20];
        for (int i = 0; i < states.length; i++)
            states[i] = new StateCol();

        // shuffle would be useful here..

        for (int i = 0; i < states.length; i++){
            setState(states[i]);
        }
    }

}
