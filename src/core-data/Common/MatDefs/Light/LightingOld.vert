#import "shaderlib/skinning.glsllib"

uniform mat4 g_WorldViewProjectionMatrix;
uniform mat3 g_NormalMatrix;
uniform mat4 g_WorldViewMatrix;

attribute vec3 inPosition;
attribute vec2 inTexCoord;
attribute vec3 inNormal;
attribute vec3 inTangent;

varying vec3 wvNormal;
varying vec3 wvTangent;
varying vec3 wvPosition;

varying vec2 texCoord;
varying vec3 viewDir;

// FIXME: tangent is not needed if NORMAL_MAP is not defined
void lightComputeTangentVS(in vec3 normal, out vec3 outNormal, out vec3 outTangent){
    outNormal = normalize(g_NormalMatrix * normal);
    outTangent = normalize(g_NormalMatrix * inTangent);
}

void main(){
   vec4 pos = vec4(inPosition, 1.0);
   vec4 norm = vec4(inNormal, 1.0);

   Skinning_Compute(pos, norm);

   gl_Position = g_WorldViewProjectionMatrix * pos;
   wvPosition = (g_WorldViewMatrix * pos).xyz;
   viewDir = normalize(-wvPosition);

   texCoord = inTexCoord;
   lightComputeTangentVS(norm, wvNormal, wvTangent);
}





















