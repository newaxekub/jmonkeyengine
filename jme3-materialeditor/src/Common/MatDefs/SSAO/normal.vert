uniform mat4 g_WorldViewProjectionMatrix;
uniform mat3 g_NormalMatrix;

attribute vec3 inPosition;
attribute vec3 inNormal;

varying vec3 normal;

void main(void)
{
   normal = normalize(g_NormalMatrix * inNormal);
   gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition,1.0);
}