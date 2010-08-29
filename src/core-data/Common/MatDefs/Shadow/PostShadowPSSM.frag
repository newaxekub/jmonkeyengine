#version 120
#ifdef NO_SHADOW2DPROJ
#define SHADOWMAP sampler2D
#define SHADOWTEX texture2D
#define SHADCOORD(coord) coord.xy
#else
#define SHADOWMAP sampler2DShadow
#define SHADOWTEX shadow2D
#define SHADCOORD(coord) vec3(coord.xy,0.0)
#endif

#ifdef EDGE_FILTERING_DITHER
#define GETSHADOW Shadow_DoDither_2x2
#else
#define GETSHADOW Shadow_DoPCF_2x2
#endif


uniform SHADOWMAP m_ShadowMap0;
uniform SHADOWMAP m_ShadowMap1;
uniform SHADOWMAP m_ShadowMap2;
uniform SHADOWMAP m_ShadowMap3;
uniform SHADOWMAP m_ShadowMap4;
uniform SHADOWMAP m_ShadowMap5;
uniform SHADOWMAP m_ShadowMap6;
uniform SHADOWMAP m_ShadowMap7;

//uniform int m_NbSplits;
uniform float[] m_Splits;
uniform float m_ShadowIntensity;

varying vec4[8] projCoord;

varying float shadowPosition;

const float texSize=1024.0;
const float pixSize = 1.0 / texSize;
const vec2 pixSize2 = vec2(pixSize);

float Shadow_DoShadowCompareOffset(in SHADOWMAP tex, vec4 projCoord, vec2 offset){
     return step(projCoord.z, SHADOWTEX(tex, SHADCOORD(projCoord.xy + offset * pixSize2)).r);
}

float Shadow_DoShadowCompare(in SHADOWMAP tex, vec4 projCoord){
    return step(projCoord.z, SHADOWTEX(tex, SHADCOORD(projCoord.xy)).r);
}

float Shadow_BorderCheck(in vec2 coord){
    // Fastest, "hack" method (uses 4-5 instructions)
    vec4 t = vec4(coord.xy, 0.0, 1.0);
    t = step(t.wwxy, t.xyzz);
    return dot(t,t);
}

float Shadow_DoDither_2x2(in SHADOWMAP tex, in vec4 projCoord){
    float shadow = 0.0;
    vec2 o = mod(floor(gl_FragCoord.xy), 2.0);
    shadow += Shadow_DoShadowCompareOffset(tex,projCoord,vec2(-1.5, 1.5) + o);
    shadow += Shadow_DoShadowCompareOffset(tex,projCoord,vec2( 0.5, 1.5) + o);
    shadow += Shadow_DoShadowCompareOffset(tex,projCoord,vec2(-1.5, -0.5) + o);
    shadow += Shadow_DoShadowCompareOffset(tex,projCoord,vec2( 0.5, -0.5) + o);
    shadow *= 0.25 ;
    return shadow;
}

float Shadow_DoPCF_2x2(in SHADOWMAP tex, in vec4 projCoord){

    float shadow = 0.0;
    float x,y;
    for (y = -1.5 ; y <=1.5 ; y+=1.0)
            for (x = -1.5 ; x <=1.5 ; x+=1.0)
                    shadow += clamp(Shadow_DoShadowCompareOffset(tex,projCoord,vec2(x,y)) +
                                    Shadow_BorderCheck(projCoord.xy),
                                    0.0, 1.0);

    shadow /= 16.0 ;
    return shadow;
}


float getShadow(in SHADOWMAP tex, in vec4 pProjCoord){
    vec4 coord = pProjCoord;
    coord.xyz /= coord.w;

    return GETSHADOW(tex, coord);// GETSHADOW
}

void main() {
 
    float shad=1.0;

    // find the appropriate depth map to look up in
    // based on the depth of this fragment
    if(shadowPosition < m_Splits[1]){
       shad = getShadow(m_ShadowMap0, projCoord[0]);
    }else if( shadowPosition <  m_Splits[2]){
       shad = getShadow(m_ShadowMap1, projCoord[1]);
    }else if( shadowPosition <  m_Splits[3]){
       shad = getShadow(m_ShadowMap2, projCoord[2]);
    }else if( shadowPosition <  m_Splits[4]){
       shad = getShadow(m_ShadowMap3, projCoord[3]);
    }else if( shadowPosition <  m_Splits[5]){
       shad = getShadow(m_ShadowMap4, projCoord[4]);
    }else if( shadowPosition <  m_Splits[6]){
       shad = getShadow(m_ShadowMap5, projCoord[5]);
    }else if( shadowPosition <  m_Splits[7]){
       shad = getShadow(m_ShadowMap6, projCoord[6]);
    }else if( shadowPosition <  m_Splits[8]){
       shad = getShadow(m_ShadowMap7, projCoord[7]);
    }

    shad=shad*m_ShadowIntensity+(1.0-m_ShadowIntensity);

   gl_FragColor = vec4(shad,shad,shad,1.0);
  
}

