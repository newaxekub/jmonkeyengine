//============================================
// DEFINES

// if m_DiffuseMap contains valid diffuse/color map
//#define HAS_DIFFUSEMAP

// if m_NormalMap contains valid normal map
// if not defined, uses vertex normal instead
//#define HAS_NORMALMAP

// if m_SpecularMap contains valid specular map
//#define HAS_SPECULARMAP

// if defined, alpha channel of diffuse map is used for blending
//#define USE_ALPHA

// number of lights sent in g_Light params
//#define NUM_LIGHTS 4

//============================================
// BEGIN PHONG LIGHTING SHADER
//============================================

// PER-MATERIAL PARAMS
// used for specular calculation
// see phong lighting model for more info
uniform float m_Shininess;

// color of diffuse map is multiplied by diffuse from lighting
uniform sampler2D m_DiffuseMap;

// normal from m_NormalMap used for shading,
// should be in tangent space. blue channel is UP
uniform sampler2D m_NormalMap;

// color of specular map is multiplied by specular from lighting
uniform sampler2D m_SpecularMap;

// PER-MESH PARAMS
// see Renderer.updateLightListUniforms() for more info about these params
uniform vec4 g_LightColor[4];
uniform vec4 g_LightPosition[4];

// world -> eye space matrix
uniform mat4 g_ViewMatrix;

// input from vertex shader
varying vec3 wvNormal;
varying vec3 wvTangent;

// WorldView position
varying vec3 wvPosition;

#if defined HAS_DIFFUSEMAP || defined HAS_NORMALMAP || defined HAS_SPECULARMAP
varying vec2 texCoord;
#endif


varying vec3 viewDir;
//=====


//=====
// BEGIN COMMON ROUTINES
float lightComputeDiffuse(vec3 norm, vec3 lightdir){
    return max(0.0, dot(norm, lightdir));
}

float lightComputeSpecular(vec3 norm, vec3 viewdir, vec3 lightdir, float shiny){
    vec3 refdir = reflect(-lightdir, norm);
    return pow(max(dot(refdir, viewdir), 0.0), shiny);
}

mat3 lightComputeTangentBasis(){
    vec3 wvBinormal = cross(wvNormal, wvTangent);
    return mat3(wvTangent, wvBinormal, wvNormal);
}

void lightComputeDir(vec3 worldPos, vec4 color, vec4 position, out vec4 lightDir){
    float posLight = step(0.5, color.w);
    vec3 tempVec = position.xyz * sign(posLight - 0.5) - (worldPos * posLight);
    float dist = length(tempVec);

    lightDir.w = clamp(1.0 - position.w * dist * posLight, 0.0, 1.0);
    lightDir.xyz = tempVec / dist;
}

void lightComputeDir2(vec3 worldPos, vec4 color, vec4 position, out vec4 lightDir){
    lightDir.xyz = normalize(position.xyz - worldPos);
}

void lightComputeAll(mat3 tbnMat,
                     int lightCount,
                     out vec3 outDiffuse,
                     out vec3 outSpecular){
        #ifdef HAS_NORMALMAP
            vec3 normal = (texture2D(m_NormalMap, texCoord).xyz * vec3(2.0) - vec3(1.0));
            // find tangent view dir & vert pos
            vec3 localViewDir = viewDir * tbnMat;
        #else
            vec3 normal = wvNormal;
            vec3 localViewDir = viewDir;
        #endif

        for (int i = 0; i < lightCount; i++){
           // find light dir in tangent space, works for point & directional lights
           vec4 wvLightPos = (g_ViewMatrix * vec4(g_LightPosition[i].xyz, g_LightColor[i].w));
           wvLightPos.w = g_LightPosition[i].w;

           vec4 lightDir;
           lightComputeDir(wvPosition, g_LightColor[i], wvLightPos, lightDir);

           #ifdef HAS_NORMALMAP
            lightDir.xyz = lightDir.xyz * tbnMat;
           #endif

           vec3 lightScale = g_LightColor[i].rgb * lightDir.w;
           float specular = lightComputeSpecular(normal, localViewDir, lightDir.xyz, m_Shininess);
           float diffuse = lightComputeDiffuse(normal, lightDir.xyz);
           outSpecular += specular * lightScale * step(0.01, diffuse) * g_LightColor[i].rgb;
           outDiffuse += diffuse * lightScale * g_LightColor[i].rgb;
        }
}

//=====
// BEGIN MAIN ROUTINE
void main(){

   #ifdef HAS_DIFFUSEMAP
    vec4 diffuseColor = texture2D(m_DiffuseMap, texCoord);
   #else
    vec4 diffuseColor = vec4(0.8, 0.8, 0.8, 1.0);
   #endif

   #ifdef HAS_SPECULARMAP
    vec3 specularColor = texture2D(m_SpecularMap, texCoord).xyz;
   #else
    vec3 specularColor = diffuseColor.rgb * diffuseColor.rgb * vec3(2.0);
   #endif

   mat3 tbnMat = lightComputeTangentBasis();

   vec3 totalSpecular = vec3(0.0);
   vec3 totalDiffuse = vec3(0.0);

   lightComputeAll(tbnMat, 4, totalDiffuse, totalSpecular);

   gl_FragColor = vec4(specularColor * totalSpecular
                     + diffuseColor.rgb * totalDiffuse, diffuseColor.a);

   //vec3 R = -reflect(viewDir, normal);
   //float sum_spec = max(0.0, pow(dot(R, lightDir), g_Shininess));
   //float intensity = 1.0/pi + 1.0 * (g_Shininess + 2.0) * sum_spec/(2.0*pi);
   //gl_FragColor = vec4(intensity * NdotL);
}