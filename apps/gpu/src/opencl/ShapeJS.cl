float step01(float x, float x0, float vs){
    return (x-(x0-vs))/(2*vs);
}

float step10(float x, float x0, float vs) {
    return ((x0+vs)-x)/(2*vs);
}

float gyroid(float vs, float level, float factor, float thickness, float3 offset, float3 pnt) {
    pnt = pnt - offset;
    pnt = pnt * factor;

    float d = fabs((sin(pnt.x) * cos(pnt.y) + sin(pnt.y) * cos(pnt.z) + sin(pnt.z) * cos(pnt.x) - level) / factor) - (thickness);

    return step10(d,0,vs);
}

float sphere(float vs, float radius, float cx, float cy, float cz, bool sign, float3 pnt) {
    float x = pnt.x - cx;
    float y = pnt.y - cy;
    float z = pnt.z - cz;

    float r = sqrt(x * x + y * y + z * z);

    if (sign) {
        return step10(r,radius,vs);
    } else {
        return step01(r,radius,vs);
    }
}

float torus(float vs, float rout, float rin, float cx, float cy, float cz, float3 pnt) {
    float x = pnt.x - cx;
    float y = pnt.y - cy;
    float z = pnt.z - cz;

    float rxy = sqrt(x*x + y*y) - rout;

    return step10(((rxy*rxy + z*z) - rin*rin)/(2*rin), 0, vs);
}

float subtraction(float a, float b) {
    if (a < 0) {
        return 0;
    }

    if (b > 1) {
        return 0;
    }

    return (a * (1.0 - b));
}

float intersection(float a, float b) {
    return min(a,b);
}

