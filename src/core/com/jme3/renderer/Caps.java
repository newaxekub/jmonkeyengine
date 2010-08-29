package com.jme3.renderer;

import com.jme3.shader.Shader;
import com.jme3.shader.Shader.ShaderSource;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.FrameBuffer.RenderBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import java.util.Collection;

public enum Caps {

    /// Framebuffer features
    FrameBuffer,
    FrameBufferMRT,
    FrameBufferMultisample,

    /// API Version
    OpenGL20,
    OpenGL21,
    OpenGL30,
    OpenGL31,
    OpenGL32,

    /// Shader language version
    ARBprogram,
    GLSL100,
    GLSL110,
    GLSL120,
    GLSL130,
    GLSL140,
    GLSL150,

    /**
     * Supports reading from textures inside the vertex shader.
     */
    VertexTextureFetch,

    /**
     * Supports geometry shader.
     */
    GeometryShader,

    /**
     * Supports texture arrays
     */
    TextureArray,

    /**
     * Supports texture buffers
     */
    TextureBuffer,

    /**
     * Supports floating point textures (Format.RGB16F)
     */
    FloatTexture,

    /**
     * Supports floating point FBO color buffers (Format.RGB16F)
     */
    FloatColorBuffer,

    /**
     * Supports floating point depth buffer
     */
    FloatDepthBuffer,

    /**
     * Supports Format.RGB111110F for textures
     */
    PackedFloatTexture,

    /**
     * Supports Format.RGB9E5 for textures
     */
    SharedExponentTexture,

    /**
     * Supports Format.RGB111110F for FBO color buffers
     */
    PackedFloatColorBuffer,

    /**
     * Supports Format.RGB9E5 for FBO color buffers
     */
    SharedExponentColorBuffer,

    /**
     * Supports Format.LATC for textures, this includes
     * support for ATI's 3Dc texture compression.
     */
    TextureCompressionLATC, 

    /// Vertex Buffer features
    MeshInstancing,

    /**
     * Supports VAO, or vertex buffer arrays
     */
    VertexBufferArray,

    /**
     * Supports multisampling on the screen
     */
    Multisample;

    public static boolean supports(Collection<Caps> caps, Texture tex){
        if (tex.getType() == Texture.Type.TwoDimensionalArray
         && !caps.contains(Caps.TextureArray))
            return false;

        Image img = tex.getImage();
        if (img == null)
            return true;

        Format fmt = img.getFormat();
        switch (fmt){
            case Depth32F:
                return caps.contains(Caps.FloatDepthBuffer);
            case LATC:
                return caps.contains(Caps.TextureCompressionLATC);
            case RGB16F_to_RGB111110F:
            case RGB111110F:
                return caps.contains(Caps.PackedFloatTexture);
            case RGB16F_to_RGB9E5:
            case RGB9E5:
                return caps.contains(Caps.SharedExponentTexture);
            default:
                if (fmt.isFloatingPont())
                    return caps.contains(Caps.FloatTexture);
                        
                return true;
        }
    }

    public static boolean supports(Collection<Caps> caps, FrameBuffer fb){
        if (!caps.contains(Caps.FrameBuffer))
            return false;

        if (fb.getSamples() > 1
         && !caps.contains(Caps.FrameBufferMultisample))
            return false;

        RenderBuffer colorBuf = fb.getColorBuffer();
        RenderBuffer depthBuf = fb.getDepthBuffer();

        if (depthBuf != null){
            Format depthFmt = depthBuf.getFormat();
            if (!depthFmt.isDepthFormat()){
                return false;
            }else{
                if (depthFmt == Format.Depth32F
                 && !caps.contains(Caps.FloatDepthBuffer))
                    return false;
            }
        }
        if (colorBuf != null){
            Format colorFmt = colorBuf.getFormat();
            if (colorFmt.isDepthFormat())
                return false;

            if (colorFmt.isCompressed())
                return false;

            switch (colorFmt){
                case RGB111110F:
                    return caps.contains(Caps.PackedFloatColorBuffer);
                case RGB16F_to_RGB111110F:
                case RGB16F_to_RGB9E5:
                case RGB9E5:
                    return false;
                default:
                    if (colorFmt.isFloatingPont())
                        return caps.contains(Caps.FloatColorBuffer);

                    return true;
            }
        }
        return true;
    }

    public static boolean supports(Collection<Caps> caps, Shader shader){
        String lang = shader.getLanguage();
        if (lang.startsWith("GLSL")){
            int ver = Integer.parseInt(lang.substring(4));
            switch (ver){
                case 100:
                    return caps.contains(Caps.GLSL100);
                case 110:
                    return caps.contains(Caps.GLSL110);
                case 120:
                    return caps.contains(Caps.GLSL120);
                case 130:
                    return caps.contains(Caps.GLSL130);
                case 140:
                    return caps.contains(Caps.GLSL140);
                case 150:
                    return caps.contains(Caps.GLSL150);
                default:
                    return false;
            }
        }
        return false;
    }

}
