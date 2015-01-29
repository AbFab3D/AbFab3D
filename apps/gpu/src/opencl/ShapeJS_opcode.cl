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
float3 translation(float3 inv_trans,float3 in) {
    return in - inv_trans;
}

float3 rotation(float3 center, float16 inv_mat,float3 in) {
    float3 pos = in - center;  // TODO: benchmark compare for no center calc

    return (float3)(
       dot(inv_mat.s048, pos),
       dot(inv_mat.s159, pos),
       dot(inv_mat.s26A, pos)) + center;

}

float3 scale(float3 scale,float3 in) {
    return in / scale;
}

// Datasources
float gyroid(float vs, float level, float thickness, float3 offset, float factor, float3 pnt) {
    pnt = pnt - offset;
    pnt = pnt * factor;

    float d = fabs((sin(pnt.x) * cos(pnt.y) + sin(pnt.y) * cos(pnt.z) + sin(pnt.z) * cos(pnt.x) - level) / factor) - (thickness);

    float ret = step10(d,0,vs);
/*
#ifdef DEBUG
//if (((fabs(pnt.x - 245.68)) < 1.40) && ((fabs(pnt.y - 101.89)) < 1.43)  && ((fabs(pnt.z - 243)) < 10.0)) {
if (ret > 0.0f && ret < 1.0f) {
        printf("pos: %v3f level: %f thick: %f  factor: %f d: %f ret: %f\n",pnt,level,thickness,factor,d,ret);
}
//}
#endif
*/
    return ret;
}

float gyroidDebug(float vs, float level, float thickness, float3 offset, float factor, float3 pnt) {
#ifdef DEBUG
        printf("gyroidDebug pos: %v3f\n",pnt);
#endif

    pnt = pnt - offset;
    pnt = pnt * factor;

    float d = fabs((sin(pnt.x) * cos(pnt.y) + sin(pnt.y) * cos(pnt.z) + sin(pnt.z) * cos(pnt.x) - level) / factor) - (thickness);

    float ret = step10(d,0,vs);
#ifdef DEBUG
printf("pos: %v3f level: %f thick: %f  factor: %f d: %f ret: %f\n",pnt,level,thickness,factor,d,ret);
#endif

    return ret;
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

float box(float vs, float3 minv, float3 maxv, float3 pnt) {
    if (pnt.x <= minv.x - vs || pnt.x >= maxv.x + vs ||
        pnt.y <= minv.y - vs || pnt.y >= maxv.y + vs ||
        pnt.z <= minv.z - vs || pnt.z >= maxv.z + vs) {

        return 0;
    }
    float finalValue = 1;

    finalValue = min(finalValue, intervalCap(pnt.x, minv.x, maxv.x,vs));
    finalValue = min(finalValue, intervalCap(pnt.y, minv.y, maxv.y,vs));
    finalValue = min(finalValue, intervalCap(pnt.z, minv.z, maxv.z,vs));

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

// TODO: benchmark if this is worth having, in theory loops are expensive
float unionOp(float a, float b) {
    return max(a,b);
}
