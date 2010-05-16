// ********************
// * Defines	      *
// ********************
//#define VERTEX_LIGHTING
//#define MATERIAL_COLORS
//#define DIFFUSEMAP
//#define NORMALMAP

uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldViewMatrix;
uniform mat3 g_NormalMatrix;
uniform mat4 g_ViewMatrix;

uniform vec4 m_Ambient;
uniform vec4 m_Diffuse;
uniform vec4 m_Specular;
uniform float m_Shininess;

uniform vec4 g_LightColor;
uniform vec4 g_LightPosition;

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

#ifndef VERTEX_LIGHTING
  attribute vec3 inTangent;

  #ifndef NORMALMAP
  varying vec3 vNormal;
  #endif
  varying vec3 vPosition;
  varying vec3 vViewDir;
  varying vec4 vLightDir;
#endif

// JME3 lights in world space
void lightComputeDir(in vec3 worldPos, in vec4 color, in vec4 position, out vec4 lightDir){
    float posLight = step(0.5, color.w);
    vec3 tempVec = position.xyz * sign(posLight - 0.5) - (worldPos * posLight);
    #ifdef ATTENUATION
     float dist = length(tempVec);
     lightDir.w = clamp(1.0 - position.w * dist * posLight, 0.0, 1.0);
     lightDir.xyz = tempVec / dist;
    #else
     lightDir = vec4(normalize(tempVec), 1.0);
    #endif
}

#ifdef VERTEX_LIGHTING
  float lightComputeDiffuse(vec3 norm, vec3 lightdir){
      return max(0.0, dot(norm, lightdir));
  }

  float lightComputeSpecular(vec3 norm, vec3 viewdir, vec3 lightdir, float shiny){
      vec3 H = (viewdir + lightdir) * vec3(0.5);
      //vec3 H = normalize(viewdir + lightdir);
      return pow(max(dot(H, norm), 0.0), shiny);

      //vec3 refdir = reflect(-lightdir, norm);
      //return pow(max(dot(refdir, viewdir), 0.0), shiny);
  }

  vec2 computeLighting(in vec3 wvPos, in vec3 wvNorm, in vec3 vDir){
     vec4 lightDir;
     lightComputeDir(wvPos, g_LightColor, g_LightPosition, lightDir);
     float diffuseFactor = lightComputeDiffuse(wvNorm, lightDir.xyz);
     float specularFactor = lightComputeSpecular(wvNorm, vDir, lightDir.xyz, m_Shininess);
     //specularFactor *= step(0.01, diffuseFactor);
     return vec2(diffuseFactor, specularFactor);
  }
#endif

void main(){
   vec4 pos = vec4(inPosition, 1.0);
   gl_Position = g_WorldViewProjectionMatrix * pos;
   texCoord = inTexCoord;

   vec3 wvPosition = (g_WorldViewMatrix * pos).xyz;
   vec3 wvNormal  = normalize(g_NormalMatrix * inNormal);
   vec3 viewDir = normalize(-wvPosition);

   vec4 wvLightPos = (g_ViewMatrix * vec4(g_LightPosition.xyz, g_LightColor.w));
   wvLightPos.w = g_LightPosition.w;

   #if defined(NORMALMAP) && !defined(VERTEX_LIGHTING)
     vec3 wvTangent = normalize(g_NormalMatrix * inTangent);
     vec3 wvBinormal = cross(wvNormal, wvTangent);
     mat3 tbnMat = mat3(wvTangent, wvBinormal, wvNormal);

     vPosition = wvPosition * tbnMat;
     vViewDir  = viewDir * tbnMat;
     lightComputeDir(wvPosition, g_LightColor, wvLightPos, vLightDir);
     vLightDir.xyz = (vLightDir.xyz * tbnMat).xyz;
   #elif !defined(VERTEX_LIGHTING)
     vNormal = wvNormal;

     vPosition = wvPosition;
     vViewDir = viewDir;

     lightComputeDir(wvPosition, g_LightColor, wvLightPos, vLightDir);
   #endif

   #ifdef MATERIAL_COLORS
      AmbientSum  = m_Ambient  * g_LightColor;
      DiffuseSum  = m_Diffuse  * g_LightColor;
      SpecularSum = m_Specular * g_LightColor;
    #else
      AmbientSum  = vec4(0.0); //= g_LightColor;
      DiffuseSum  = g_LightColor;
      SpecularSum = g_LightColor;
    #endif

    #ifdef VERTEX_COLOR
      DiffuseSum *= inColor;
    #endif

    #ifdef VERTEX_LIGHTING
       vec2 light = computeLighting(wvPosition, wvNormal, viewDir);
       DiffuseSum *= light.x;
       SpecularSum *= light.y;
    #endif
}