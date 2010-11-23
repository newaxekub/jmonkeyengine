uniform sampler2D m_Alpha;
uniform sampler2D m_Tex1;
uniform sampler2D m_Tex2;
uniform sampler2D m_Tex3;
uniform float m_Tex1Scale;
uniform float m_Tex2Scale;
uniform float m_Tex3Scale;

varying vec2 texCoord;

void main(void)
{
	vec4 alpha   = texture2D( m_Alpha, texCoord.xy );
	vec4 tex0    = texture2D( m_Tex1, texCoord.xy * m_Tex1Scale ); // Tile
	vec4 tex1    = texture2D( m_Tex2, texCoord.xy * m_Tex2Scale ); // Tile
	vec4 tex2    = texture2D( m_Tex3, texCoord.xy * m_Tex3Scale ); // Tile

	tex0 *= alpha.r; // Red channel
	tex1 = mix( tex0, tex1, alpha.g ); // Green channel
	vec4 outColor = mix( tex1, tex2, alpha.b ); // Blue channel

	gl_FragColor = outColor;
}

