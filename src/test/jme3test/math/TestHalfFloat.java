package jme3test.math;

import com.jme3.math.FastMath;
import java.util.Scanner;

public class TestHalfFloat {
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        while (true){
            System.out.println("Enter float to convert or 'x' to exit: ");
            String s = scan.nextLine();
            if (s.equals("x"))
                break;

            float flt = Float.valueOf(s);
            short half = FastMath.convertFloatToHalf(flt);
            float flt2 = FastMath.convertHalfToFloat(half);

            System.out.println("Input float: "+flt);
            System.out.println("Result float: "+flt2);
        }
    }
}
