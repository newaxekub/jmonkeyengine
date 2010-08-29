package com.jme3.shader;

public class ShaderUtils {

    public static String convertToGLSL130(String input, boolean isFrag){
        StringBuilder sb = new StringBuilder();
        sb.append("#version 130\n");
        if (isFrag){
            input = input.replaceAll("varying", "in");
        }else{
            input = input.replaceAll("attribute", "in");
            input = input.replaceAll("varying", "out");
        }
        sb.append(input);
        return sb.toString();
    }

}
