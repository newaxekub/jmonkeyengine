// ********************
// * Defines	      *
// ********************
//#define VERTEX_LIGHTING
//#define MATERIAL_COLORS
//#define DIFFUSEMAP
//#define NORMALMAP
//#define LOW_QUALITY
//#define SPECULARMAP
//#define PARALLAXMAP
//#define NORMALMAP_PARALLAX

varying vec2 texCoord;

varying vec4 AmbientSum;
varying vec4 DiffuseSum;
varying vec4 SpecularSum;

#ifndef VERTEX_LIGHTING
  varying vec3 vPosition;
  varying vec3 vViewDir;
  varying vec4 vLightDir;
#endif

#ifdef DIFFUSEMAP
  uniform sampler2D m_DiffuseMap;
#endif

#ifdef SPECULARMAP
  uniform sampler2D m_SpecularMap;
#endif

#ifdef PARALLAXMAP
  uniform sampler2D m_ParallaxMap;
#endif
  
#ifdef NORMALMAP
  uniform sampler2D m_NormalMap;
#else
  varying vec3 vNormal;
#endif

#ifndef VERTEX_LIGHTING
uniform float m_Shininess;

float lightComputeDiffuse(vec3 norm, vec3 lightdir){
    return max(0.0, dot(norm, lightdir));
}

float lightComputeSpecular(vec3 norm, vec3 viewdir, vec3 lightdir, float shiny){
    #ifdef LOW_QUALITY // Use Blinn-Phong instead
       // Note: preferably, H should be computed in the vertex shader
       vec3 H = (viewdir + lightdir) * vec3(0.5);
       return pow(max(dot(H, norm), 0.0), shiny);
    #else
       vec3 R = reflect(-lightdir, norm);
       return pow(max(dot(R, viewdir), 0.0), shiny);
    #endif
}

vec2 computeLighting(in vec3 wvPos, in vec3 wvNorm, in vec3 wvViewDir, in vec3 wvLightDir){
   float diffuseFactor = lightComputeDiffuse(wvNorm, wvLightDir);
   float specularFactor = 1.0;
   specularFactor = step(0.01, diffuseFactor)
                  * lightComputeSpecular(wvNorm, wvViewDir, wvLightDir, m_Shininess);
   return vec2(diffuseFactor, specularFactor);
}
#endif

void main(){
    vec2 newTexCoord;
    #if defined(PARALLAXMAP) || defined(NORMALMAP_PARALLAX)
       float h;
       #ifdef PARALLAXMAP
          h = texture2D(m_ParallaxMap, texCoord).r;
       #else
          h = texture2D(m_NormalMap, texCoord).a;
       #endif
       float heightScale = 0.05;
       float heightBias = heightScale * -0.5;
       vec3 normView = normalize(vViewDir);
       h = (h * heightScale + heightBias) * normView.z;
       newTexCoord = texCoord + (h * -normView.xy);
    #else
       newTexCoord = texCoord;
    #endif
    

    // ***********************
    // Read from textures
    // ***********************
    #if defined(NORMALMAP) && !defined(VERTEX_LIGHTING)
      vec4 normalHeight = texture2D(m_NormalMap, newTexCoord);
      vec3 normal = (normalHeight.xyz * vec3(2.0) - vec3(1.0));
      normal.y = -normal.y;
    #elif !defined(VERTEX_LIGHTING)
      vec3 normal = vNormal;
      #ifndef LOW_QUALITY
         normal = normalize(normal);
      #endif
    #endif

    #ifdef DIFFUSEMAP
      vec4 diffuseColor = texture2D(m_DiffuseMap, newTexCoord);
    #else
      vec4 diffuseColor = vec4(1.0);
    #endif

    #ifdef SPECULARMAP
      vec4 specularColor = texture2D(m_SpecularMap, newTexCoord);
    #else
      vec4 specularColor = vec4(1.0);
    #endif

    #ifdef VERTEX_LIGHTING
       gl_FragColor = (AmbientSum + DiffuseSum + SpecularSum) * diffuseColor
                     + SpecularSum * specularColor;
    #else
       vec4 lightDir = vLightDir;
       lightDir.xyz = normalize(lightDir.xyz);

       vec2 light = computeLighting(vPosition, normal, vViewDir.xyz, lightDir.xyz);
       gl_FragColor = (AmbientSum + DiffuseSum * light.x) * diffuseColor;
                   //  + SpecularSum * light.y * specularColor;
    #endif
    #ifdef USE_ALPHA
       gl_FragColor.a = diffuseColor.a;
    #endif
}
