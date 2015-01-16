// Utility functions
float step01(float x, float x0, float vs){
    if(x <= -vs)
        return 0.0;

    if(x >=  vs)
        return 1.0;

    return (x-(x0-vs))/(2*vs);
}

float step10(float x, float x0, float vs) {
   if(x <= x0 - vs)
       return 1.0;

   if(x >= x0 + vs)
       return 0.0;

    return ((x0+vs)-x)/(2*vs);
}

float intervalCap(float x, float xmin, float xmax, float vs){

    if(xmin >= xmax-vs)
        return 0;

    float vs2 = vs*2;
    float vxi = clamp((x-(xmin-vs))/vs2,0.0f,1.0f);
    float vxa = clamp((((xmax+vs)-x))/vs2,0.0f,1.0f);

    return vxi*vxa;
}


// Transform functions
float3 translation(float3 in, float3 inv_trans) {
    return in - inv_trans;
}


float3 rotation(float3 in, float3 center, float16 inv_mat) {
    float3 pos = in - center;  // TODO: benchmark compare for no center calc

    return (float3)(
       dot(inv_mat.s048, pos),
       dot(inv_mat.s159, pos),
       dot(inv_mat.s26A, pos)) + center;

}

// Datasources
float gyroid(float vs, float level, float thickness, float3 offset, float factor, float3 pnt) {
    pnt = pnt - offset;
    pnt = pnt * factor;

    float d = fabs((sin(pnt.x) * cos(pnt.y) + sin(pnt.y) * cos(pnt.z) + sin(pnt.z) * cos(pnt.x) - level) / factor) - (thickness);

    return step10(d,0,vs);
}

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

float box(float vs, float xmin, float xmax, float ymin, float ymax, float zmin, float zmax, float3 pnt) {
    if (pnt.x <= xmin - vs || pnt.x >= xmax + vs ||
        pnt.y <= ymin - vs || pnt.y >= ymax + vs ||
        pnt.z <= zmin - vs || pnt.z >= zmax + vs) {

        return 0;
    }
    float finalValue = 1;

    finalValue = min(finalValue, intervalCap(pnt.x, xmin, xmax, vs));
    finalValue = min(finalValue, intervalCap(pnt.y, ymin, ymax, vs));
    finalValue = min(finalValue, intervalCap(pnt.z, zmin, zmax, vs));

    return finalValue;
}

float torus(float vs, float3 center, float rout, float rin, float3 pnt) {
    float x = pnt.x - center.x;
    float y = pnt.y - center.y;
    float z = pnt.z - center.z;

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

float intersectionOp(float a, float b) {
    return min(a,b);
}

float intersectionArr(float * src, int len) {
    float ret = src[0];

#pragma unroll
    for(int i=1; i < len; i++) {
       ret = min(ret,src[i]);
    }

    return ret;
}

// TODO: benchmark if this is worth having, in theory loops are expensive
float unionOp(float a, float b) {
    return max(a,b);
}

float unionArr(float * src, int len) {
    float ret = src[0];

#pragma unroll
    for(int i=1; i < len; i++) {
       ret = max(ret,src[i]);
    }

    return ret;
}
