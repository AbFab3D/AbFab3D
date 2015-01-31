float sphere(float vs, float3 center,float radius, bool sign, float3 pnt) {
    float x = pnt.x - center.x;
    float y = pnt.y - center.y;
    float z = pnt.z - center.z;

    float r = sqrt(x * x + y * y + z * z);

    if (radius > 0.) {
        return r-radius;
    } else {
        return -radius-r;
    }
}
