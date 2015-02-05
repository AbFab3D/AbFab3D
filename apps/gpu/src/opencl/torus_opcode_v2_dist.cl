float torus(float vs, float3 center, float rout, float rin, float3 pnt) {
    float x = pnt.x - center.x;
    float y = pnt.y - center.y;
    float z = pnt.z - center.z;

    float rxy = sqrt(x*x + y*y) - rout;

    return ((rxy*rxy + z*z) - rin*rin)/(2*rin);
}