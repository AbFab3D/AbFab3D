float gyroid(float vs, float level, float thickness, float3 offset, float factor, float3 pnt) {
    pnt = pnt - offset;
    pnt = pnt * factor;

    float d = fabs((sin(pnt.x) * cos(pnt.y) + sin(pnt.y) * cos(pnt.z) + sin(pnt.z) * cos(pnt.x) - level) / factor) - (thickness);

    return step10(d,0,vs);
}
