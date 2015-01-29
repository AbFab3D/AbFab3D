float sphere(float vs, float3 center,float radius, bool sign, float3 pnt) {
    float x = pnt.x - center.x;
    float y = pnt.y - center.y;
    float z = pnt.z - center.z;

    float r = sqrt(x * x + y * y + z * z);

    if (sign) {
        return step10(r,radius,vs);
    } else {
        return step01(r,radius,vs);
    }
}
