uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldViewMatrix;
uniform mat3 g_NormalMatrix;
uniform vec2 g_FrustumNearFar;

uniform vec4 m_Ambient;
uniform vec4 m_Diffuse;
uniform vec4 m_Specular;
uniform float m_Shininess;

varying vec2 texCoord;

varying vec4 AmbientSum;
varying vec4 DiffuseSum;
varying vec4 SpecularSum;

attribute vec3 inPosition;
attribute vec2 inTexCoord;
attribute vec3 inNormal;

#ifdef VERTEX_COLOR
  attribute vec4 inColor;
#endif

varying vec3 vNormal;
varying float vDepth;

void main(){
   vec4 pos = vec4(inPosition, 1.0);
   vec3 posView = (g_WorldViewMatrix * pos).xyz;
   vDepth = (posView.z - g_FrustumNearFar.x) / (g_FrustumNearFar.y - g_FrustumNearFar.x);

   gl_Position = g_WorldViewProjectionMatrix * pos;
   texCoord = inTexCoord;

   #if defined(NORMALMAP)
     vec3 wvNormal   = normalize(g_NormalMatrix * inNormal);
     vec3 wvTangent  = normalize(g_NormalMatrix * inTangent);
     vec3 wvBinormal = cross(wvNormal, wvTangent);
     mat3 tbnMat = mat3(wvTangent, wvBinormal, wvNormal);
   #else
     #ifdef V_TANGENT
        vNormal = normalize(g_NormalMatrix * inTangent);
     #else
        vNormal = normalize(g_NormalMatrix * inNormal);;
     #endif
   #endif

   #ifdef MATERIAL_COLORS
      AmbientSum  = m_Ambient;
      DiffuseSum  = m_Diffuse;
      SpecularSum = m_Specular;
    #else
      AmbientSum  = vec4(0.0);
      DiffuseSum  = vec4(1.0);
      SpecularSum = vec4(1.0);
    #endif

    #ifdef VERTEX_COLOR
      DiffuseSum *= inColor;
    #endif
}