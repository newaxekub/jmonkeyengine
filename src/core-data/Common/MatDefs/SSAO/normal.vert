uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldViewMatrix;

attribute vec3 inPosition;
attribute vec3 inNormal;

varying vec3 normal;

void main(void)
{
   normal=(g_WorldViewMatrix*vec4(inNormal,0.0)).xyz;
   gl_Position= g_WorldViewProjectionMatrix*vec4(inPosition,1.0);
}