uniform sampler2D m_Texture;
varying vec2 texCoord;
varying vec4 color;
varying float texFactor;

void main() {
    vec4 texVal = texture2D(m_Texture, texCoord);
    texVal += vec4(texFactor);
    texVal = min(texVal, vec4(1.0));
    gl_FragColor = texVal * color;
}

