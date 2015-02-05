float torus(float vs, float3 center, float rout, float rin, float3 pnt) {

    pnt -= center;
    pnt.y = length(pnt.xy) - rout;
    return length(pnt.yz) - rin;

}
