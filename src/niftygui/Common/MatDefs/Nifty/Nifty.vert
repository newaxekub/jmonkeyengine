uniform mat4 g_WorldViewProjectionMatrix;
uniform vec4 m_Color;
uniform int m_Mode;

attribute vec4 inPosition;
attribute vec4 inColor;
attribute vec2 inTexCoord;

varying vec2 texCoord;
varying vec4 color;
varying float texFactor;

void main() {
    vec2 pos = (g_WorldViewProjectionMatrix * inPosition).xy;
    gl_Position = vec4(pos, 0.0, 1.0);

    texCoord = inTexCoord;
    if (m_Mode == 1){
        color = m_Color;
        texFactor = 1.0;
    }else if (m_Mode == 2){
        color = inColor;
        texFactor = 1.0;
    }else if (m_Mode == 3){
        color = m_Color;
        texFactor = 0.0;
    }else if (m_Mode == 4){
        color = m_Color * inColor;
        texFactor = 0.0;
    }else{
        color = vec4(1.0);
        texFactor = 1.0;
    }
}