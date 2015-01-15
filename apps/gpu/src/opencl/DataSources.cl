float step10(float x, float x0, float vs) {
    if(x <= x0 - vs)
        return 1.0;

    if(x >= x0 + vs)
        return 0.0;

    return ((x0+vs)-x)/(2*vs);
}

float step01(float x, float x0, float vs){

    if(x <= x0 - vs)
        return 0.;

    if(x >= x0 + vs)
        return 1.;

    return (x-(x0-vs))/(2*vs);
}

float gyroid(float vs, float level, float factor, float thickness, float3 offset, float3 pnt) {
    pnt = pnt - offset;

    pnt = pnt * factor;

    float d = fabs((sin(pnt.x) * cos(pnt.y) + sin(pnt.y) * cos(pnt.z) + sin(pnt.z) * cos(pnt.x) - level) / factor) - (thickness + vs);

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

float subtraction(float a, float b) {
    if (a < 0) {
        return 0;
    }

    if (b > 1) {
        return 0;
    }

    return (a * (1.0 - b));
}

float unionop(float a, float b) {
    if (a >= 1) {
        return 1;
    }
    
    return max(a,b); 
}

