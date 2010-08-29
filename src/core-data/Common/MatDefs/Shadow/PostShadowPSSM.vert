#version 120

uniform mat4 m_LightViewProjectionMatrix0;
uniform mat4 m_LightViewProjectionMatrix1;
uniform mat4 m_LightViewProjectionMatrix2;
uniform mat4 m_LightViewProjectionMatrix3;
uniform mat4 m_LightViewProjectionMatrix4;
uniform mat4 m_LightViewProjectionMatrix5;
uniform mat4 m_LightViewProjectionMatrix6;
uniform mat4 m_LightViewProjectionMatrix7;

uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;

varying vec4 projCoord[8];

varying float shadowPosition;

attribute vec3 inPosition;

const mat4 biasMat = mat4(0.5, 0.0, 0.0, 0.0,
                          0.0, 0.5, 0.0, 0.0,
                          0.0, 0.0, 0.5, 0.0,
                          0.5, 0.5, 0.5, 1.0);


void main(){

    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
    shadowPosition=gl_Position.z;
    // get the vertex in world space
    vec4 worldPos = g_WorldMatrix * vec4(inPosition, 1.0);

    // populate the light view matrices array and convert vertex to light viewProj space
    vec4 coord = m_LightViewProjectionMatrix0 * worldPos;
    projCoord[0] = biasMat * coord;
    coord = m_LightViewProjectionMatrix1 * worldPos;
    projCoord[1] = biasMat * coord;
    coord = m_LightViewProjectionMatrix2 * worldPos;
    projCoord[2] = biasMat * coord;
    coord = m_LightViewProjectionMatrix3 * worldPos;
    projCoord[3] = biasMat * coord;
    coord = m_LightViewProjectionMatrix4 * worldPos;
    projCoord[4] = biasMat * coord;
    coord = m_LightViewProjectionMatrix5 * worldPos;
    projCoord[5] = biasMat * coord;
    coord = m_LightViewProjectionMatrix6 * worldPos;
    projCoord[6] = biasMat * coord;
    coord = m_LightViewProjectionMatrix7 * worldPos;
    projCoord[7] = biasMat * coord;
    
}