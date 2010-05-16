package com.jme3.input;

/**
 * A specific API for interfacing with joysticks or gaming controllers.
 */
public interface JoyInput extends Input {

    public static final int AXIS_X = 0x0;
    public static final int AXIS_Y = 0x1;
    public static final int AXIS_Z = 0x2;
    public static final int AXIS_Z_ROT = 0x3;
    public static final int POV_X = 0x4;
    public static final int POV_Y = 0x5;

    /**
     * @return The number of joysticks connected to the system
     */
    public int getJoyCount();

    /**
     * @param joyIndex
     * @return The name of the joystick at the given index.
     */
    public String getJoyName(int joyIndex);

    /**
     * @param joyIndex
     * @return The number of axes that a joystick posses at the given index.
     */
    public int getAxesCount(int joyIndex);

    /**
     * @param joyIndex
     * @return The number of buttons that a joystick posses at the given index.
     */
    public int getButtonCount(int joyIndex);
}
