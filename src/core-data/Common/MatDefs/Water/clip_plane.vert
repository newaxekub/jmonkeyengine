uniform vec4 u_clipPlane;
//uniform mat4 matViewProjection;
//uniform mat4 u_modelViewMatrix;

//attribute vec4 a_position;
attribute vec4 rm_Vertex;

varying float u_clipDist;

//vec4 vertexPositionInEye;

void main(void)
{
    // Compute the distance between the vertex and the clip plane
    u_clipDist = dot(rm_Vertex.xyz, u_clipPlane.xyz) + u_clipPlane.w;
//    vertexPositionInEye = u_modelViewMatrix * a_position;
//    u_clipDist = dot(vertexPositionInEye, u_clipPlane);
//    gl_Position = matViewProjection * rm_Vertex;
}
