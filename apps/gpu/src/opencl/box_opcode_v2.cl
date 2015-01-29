float box(float vs, float3 minv, float3 maxv, float3 pnt) {
/*
    if (pnt.x <= minv.x - vs || pnt.x >= maxv.x + vs ||
        pnt.y <= minv.y - vs || pnt.y >= maxv.y + vs ||
        pnt.z <= minv.z - vs || pnt.z >= maxv.z + vs) {

        return 0;
    }
    float finalValue=1;

    finalValue = min(finalValue, intervalCap(pnt.x, minv.x, maxv.x,vs));
    finalValue = min(finalValue, intervalCap(pnt.y, minv.y, maxv.y,vs));
    finalValue = min(finalValue, intervalCap(pnt.z, minv.z, maxv.z,vs));
    return finalValue;
*/
    return intervalCap(pnt.x, minv.x, maxv.x,vs) * intervalCap(pnt.y, minv.y, maxv.y,vs) * intervalCap(pnt.z, minv.z, maxv.z,vs);
}