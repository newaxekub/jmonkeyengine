varying vec3 normal;

void main(void)
{
 gl_FragColor = vec4(normal.xy* 0.5 + 0.5,-normal.z* 0.5 + 0.5, 1.0);

}

