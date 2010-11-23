varying float u_clipDist;
void main(void)
{
  // Reject fragments behind the clip plane
  if(u_clipDist < 0.0)
  discard;
}
