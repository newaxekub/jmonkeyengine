#import "Common/ShaderLib/Shadow.glsllib"
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

uniform int m_NbSplits;
uniform float[] m_Splits;
uniform float shadowIntensity;

varying vec4[8] projCoord;

varying float shadowPosition;



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

    shad=shad*shadowIntensity+(1-shadowIntensity);

   gl_FragColor = vec4(shad,shad,shad,1.0);
  
}

