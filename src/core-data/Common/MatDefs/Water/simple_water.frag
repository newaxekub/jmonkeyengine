/*
GLSL conversion of Michael Horsch water demo
http://www.bonzaisoftware.com/wfs.html
Converted by Mars_999
8/20/2005
*/

uniform sampler2D m_water_normalmap;
uniform sampler2D m_water_reflection;
uniform sampler2D m_water_refraction;
uniform sampler2D m_water_dudvmap;
uniform sampler2D m_water_depthmap;
uniform vec4 m_waterColor;
uniform float m_waterDepth;
uniform vec4 m_distortionScale;
uniform vec4 m_distortionMix;
uniform vec4 m_texScale;
uniform vec3 m_camDir;
uniform vec3 m_lightDir;

varying vec4 waterTex0; //lightpos
varying vec4 waterTex1; //moving texcoords
varying vec4 waterTex2; //moving texcoords
varying vec4 waterTex3; //for projection
varying vec4 waterTex4; //viewts
varying vec3 H;

//unit 0 = m_water_reflection
//unit 1 = m_water_refraction
//unit 2 = m_water_normalmap
//unit 3 = m_water_dudvmap
//unit 4 = m_water_depthmap

 const vec4 two = vec4(2.0, 2.0, 2.0, 1.0);
 const vec4 mone = vec4(-1.0, -1.0, -1.0, 1.0);

 const vec4 ofive = vec4(0.5,0.5,0.5,1.0);

 const float exponent = 64.0;

float tangDot(in vec3 v1, in vec3 v2){
    float d = dot(v1,v2);
    #ifdef V_TANGENT
        d = 1.0 - d*d;
        return step(0.0, d) * sqrt(d);
    #else
        return d;
    #endif
}

void main(void)
{


     vec4 lightTS = normalize(waterTex0);
     vec4 viewt = normalize(waterTex4);
     vec4 disdis = texture2D(m_water_dudvmap, vec2(waterTex2 * m_texScale));
     vec4 dist = texture2D(m_water_dudvmap, vec2(waterTex1 + disdis*m_distortionMix));
     vec4 fdist = dist;
     fdist = fdist * two + mone;
     fdist = normalize(fdist);
     fdist *= m_distortionScale;

     //load normalmap
     vec4 nmap = texture2D(m_water_normalmap, vec2(waterTex1 + disdis*m_distortionMix));
     nmap = (nmap-ofive) * two;
     vec4 vNorm = normalize(nmap);

     //get projective texcoords
     vec4 tmp = vec4(1.0 / waterTex3.w);
     vec4 temp = tmp;

     vec4 projCoord = waterTex3 * tmp;
     projCoord += vec4(1.0);
     projCoord *= vec4(0.5);
     tmp = projCoord + fdist;
     tmp = clamp(tmp, 0.001, 0.999);

     //load reflection,refraction and depth texture
     vec4 refTex = texture2D(m_water_reflection, vec2(tmp.x,1.0-tmp.y));
     vec4 refl = refTex;
     vec4 refr = texture2D(m_water_refraction, vec2(tmp));
     vec4 wdepth = texture2D(m_water_depthmap, vec2(tmp));

     wdepth = vec4(pow(wdepth.x, m_waterDepth));
     vec4 invdepth = 1.0 - wdepth;


 // Standard Phong

  //   vec3 specular =vec3(pow(max(tangDot(H, vNorm), 0.0), 25.0));//25.0 is shininess parameter, it should be a uniform


   //  vec3 R = reflect(-m_lightDir, vNorm);
//     float specular = pow(max(tangDot(R, m_camDir), 0.0), 25.0);

 
     //calculate specular highlight
     vec4 vRef = normalize(reflect(-lightTS, vNorm));
     float stemp =max(0.0, dot(viewt, vRef) );
     stemp = pow(stemp, exponent);
     vec4 specular = vec4(stemp);

     //calculate fresnel and inverted fresnel
     vec4 invfres = vec4( dot(vNorm, viewt) );
     vec4 fres = vec4(1.0) -invfres ;

     //calculate reflection and refraction
     refr *= invfres;
     refr *= invdepth;
     temp = m_waterColor * wdepth * invfres;
     refr += temp;
     refl *= fres;

     //add reflection and refraction
     tmp = refr + refl;

     gl_FragColor =tmp+specular;//tmp ;//+vec4(1.0,1.0,1.0,1.0);//*specular;
}
