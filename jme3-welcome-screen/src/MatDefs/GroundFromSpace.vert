#version 110

uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;
uniform mat4 g_NormalMatrix;

uniform vec3 v3LightPos;            // world space (or direction)
uniform vec3 v3CameraPos;           // The camera's current position (world space)
uniform vec3 v3InvWavelength;       // 1 / pow(wavelength, 4) for the red, green, and blue channels
uniform float fCameraHeight2;       // fCameraHeight^2
uniform float fOuterRadius;         // The outer (atmosphere) radius
uniform float fOuterRadius2;        // fOuterRadius^2
uniform float fInnerRadius;         // The inner (planetary) radius
uniform float fKr4PI;               // Kr * 4 * PI
uniform float fKm4PI;               // Km * 4 * PI
uniform float fKrESun;              // Kr * ESun
uniform float fKmESun;              // Km * ESun
uniform float fScale;               // 1 / (fOuterRadius - fInnerRadius)
uniform float fScaleDepth;          // The scale depth (i.e. the altitude at which the atmosphere's average density is found)
uniform float fScaleOverScaleDepth; // fScale / fScaleDepth
uniform int nSamples;
uniform float fSamples;
uniform float m_Time;
uniform float m_Speed;

attribute vec4 inPosition;          // vertex position in Model Coordinates
attribute vec2 inTexCoord; 
attribute vec4 inNormal;            // vertex normal in Model Coordinates

varying vec2 texCoord;
varying vec2 texCoord2;
varying vec4 v4RayleighColor;
varying vec4 v4MieColor;
varying float fLightIntensity;

float fInvScaleDepth = (1.0 / fScaleDepth);

vec3 v3ELightPos = vec3(g_WorldMatrix * vec4(v3LightPos, 1.0));
vec3 v3ECameraPos= vec3(g_WorldMatrix * vec4(v3CameraPos, 1.0));


// Offset the x texture coordinate delta units
vec2 rotate(vec2 coord, float delta)
{
	coord.x -= delta;

	if(coord.x < 0.0)
		coord.x = coord.x + 1.0;
        if(coord.x >= 1.0)
                coord.x = coord.x - 1.0;

	return coord;
}

// Returns the near intersection point of a line and a sphere
float getNearIntersection(vec3 v3Pos, vec3 v3Ray, float fDistance2, float fRadius2)
{
   float B = 2.0 * dot(v3Pos, v3Ray);
   float C = fDistance2 - fRadius2;
   float fDet = max(0.0, B*B - 4.0 * C);
   return 0.5 * (-B - sqrt(fDet));
}

float scale(float fCos)
{
   float x = 1.0 - fCos;
   return fScaleDepth * exp(-0.00287 + x*(0.459 + x*(3.83 + x*(-6.80 + x*5.25))));
}

void main(void)
{
   gl_Position = g_WorldViewProjectionMatrix * inPosition;

   // Get the ray from the camera to the vertex and its length (which is the far point of the ray passing through the atmosphere)
   vec3 v3Pos = vec3(g_WorldMatrix * inPosition);
   vec3 v3Ray = v3Pos - v3ECameraPos;
   float fFar = length(v3Ray);
   v3Ray /= fFar;

   // Calculate the closest intersection of the ray with the outer atmosphere (which is the near point of the ray passing through the atmosphere)
   float fNear = getNearIntersection(v3CameraPos, v3Ray, fCameraHeight2, fOuterRadius2);

   // Calculate the ray's starting position, then calculate its scattering offset
   vec3 v3Start = v3CameraPos + v3Ray * fNear;
   fFar -= fNear;

   // Calculate the ray's start and end positions in the atmosphere, then calculate its scattering offset
   float fStartAngle = dot(v3Ray, v3Start) / fOuterRadius;
   float fStartDepth = exp(-fInvScaleDepth);
   float fStartOffset = fStartDepth*scale(fStartAngle);
   float fDepth = exp((fInnerRadius - fOuterRadius) * fInvScaleDepth);

   float fCameraAngle = dot(-v3Ray, v3Pos) / length(v3Pos); // fFar
   // float fCameraAngle = dot(-v3Ray, v3Pos) / fFar;
   // float fLightAngle = dot(v3ELightPos, v3Pos) / length(v3Pos); // fFar
   float fLightAngle = dot(v3ELightPos, v3Pos) / fFar;

   float fCameraScale = scale(fCameraAngle);
   float fLightScale = scale(fLightAngle);
   float fCameraOffset = fDepth * fCameraScale;
   float fTemp = (fLightScale * fCameraScale);

   // Initialize the scattering loop variables
   //float fSampleLength = fFar / fSamples;
   float fSampleLength = length(v3Pos) / fSamples;
   float fScaledLength = fSampleLength * fScale;
   vec3 v3SampleRay = v3Ray * fSampleLength;
   vec3 v3SamplePoint = v3Start + v3SampleRay * 0.5;

   // Now loop through the sample rays
    vec3 v3FrontColor = vec3(0.0, 0.0, 0.0);
    vec3 v3Attenuate;
    for(int i=0; i<nSamples; i++)
    {
            float fHeight = max(fInnerRadius, length(v3SamplePoint));
            float fSampleDepth = exp(fScaleOverScaleDepth * (fInnerRadius - fHeight));
            float fScatter = fSampleDepth * fTemp - fCameraOffset;
            v3Attenuate = exp(-fScatter * (v3InvWavelength * fKr4PI + fKm4PI));
            v3FrontColor += v3Attenuate * (fSampleDepth * fScaledLength);
            v3SamplePoint += v3SampleRay;
    }

    // Some day - night shadow "spherifies" the planet
    fLightIntensity = (dot(v3LightPos, vec3(inNormal)) + 1.0) * 0.5;


    // scattering colors
    v4RayleighColor = vec4(clamp(v3FrontColor, 0.0, 3.0) * (v3InvWavelength * fKrESun + fKmESun), 0.0);
    v4MieColor = vec4(clamp(v3Attenuate, 0.0, 3.0), 0.0);

    // Rotate surface textures in opposite directions
    texCoord  = rotate(inTexCoord, m_Time * m_Speed);
    texCoord2 = rotate(inTexCoord, m_Time * -m_Speed);
}


