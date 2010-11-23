
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

#ifdef PCF_4
#define KERNEL 4.0
#endif
#ifdef PCF_8
#define KERNEL 8.0
#endif
#ifdef PCF_10
#define KERNEL 10.0
#endif
#ifdef PCF_16
#define KERNEL 16.0
#endif
#ifdef PCF_20
#define KERNEL 20.0
#endif

#ifdef STEP_1
#define STEP 0.1
#endif
#ifdef STEP_2
#define STEP 0.2
#endif
#ifdef STEP_3
#define STEP 0.3
#endif
#ifdef STEP_4
#define STEP 0.4
#endif
#ifdef STEP_5
#define STEP 0.5
#endif
#ifdef STEP_6
#define STEP 0.6
#endif
#ifdef STEP_7
#define STEP 0.7
#endif
#ifdef STEP_8
#define STEP 0.8
#endif
#ifdef STEP_9
#define STEP 0.9
#endif
#ifdef STEP_10
#define STEP 1.0
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
uniform float m_Splits0;
uniform float m_Splits1;
uniform float m_Splits2;
uniform float m_Splits3;
uniform float m_Splits4;
uniform float m_Splits5;
uniform float m_Splits6;
uniform float m_Splits7;
uniform float m_ShadowIntensity;


varying vec4 projCoord0;
varying vec4 projCoord1;
varying vec4 projCoord2;
varying vec4 projCoord3;
varying vec4 projCoord4;
varying vec4 projCoord5;
varying vec4 projCoord6;
varying vec4 projCoord7;

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
    for (y = -3.5 ; y <=3.5 ; y+=1.0)
            for (x = -3.5 ; x <=3.5 ; x+=1.0)
                    shadow += clamp(Shadow_DoShadowCompareOffset(tex,projCoord,vec2(x,y)) +
                                    Shadow_BorderCheck(projCoord.xy),
                                    0.0, 1.0);

    shadow /= 64.0 ;
    return shadow;
}

float Shadow_DoPCF(in SHADOWMAP tex, in vec4 projCoord, in float kernel, in float bstep){

    float shadow = 0.0;
    float bound=kernel*0.5-0.5;
    bound*=bstep;
    float x,y;
    for (y = -bound ; y <=bound ; y+=bstep)
            for (x = -bound ; x <=bound ; x+=bstep)
                    shadow += clamp(Shadow_DoShadowCompareOffset(tex,projCoord,vec2(x,y)) +
                                    Shadow_BorderCheck(projCoord.xy),
                                    0.0, 1.0);

    shadow =shadow/(kernel*kernel) ;
    return shadow;
}


float getShadow(in SHADOWMAP tex, in vec4 pProjCoord){
    vec4 coord = pProjCoord;
    coord.xyz /= coord.w;

    return Shadow_DoPCF(tex, coord,KERNEL,STEP);// GETSHADOW
}

void main() {
 
    float shad=1.0;    

    // find the appropriate depth map to look up in
    // based on the depth of this fragment
    if(shadowPosition < m_Splits0){
       shad = getShadow(m_ShadowMap0, projCoord0);
    }else if( shadowPosition <  m_Splits1){
       shad = getShadow(m_ShadowMap1, projCoord1);
    }else if( shadowPosition <  m_Splits2){
       shad = getShadow(m_ShadowMap2, projCoord2);
    }else if( shadowPosition <  m_Splits3){
       shad = getShadow(m_ShadowMap3, projCoord3);
    }else if( shadowPosition <  m_Splits4){
       shad = getShadow(m_ShadowMap4, projCoord4);
    }else if( shadowPosition <  m_Splits5){
       shad = getShadow(m_ShadowMap5, projCoord5);
    }else if( shadowPosition <  m_Splits6){
       shad = getShadow(m_ShadowMap6, projCoord6);
    }else if( shadowPosition <  m_Splits7){
       shad = getShadow(m_ShadowMap7, projCoord7);
    }

    shad=shad*m_ShadowIntensity+(1.0-m_ShadowIntensity);

   gl_FragColor = vec4(shad,shad,shad,1.0);
  
}

