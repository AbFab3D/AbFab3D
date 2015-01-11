#pragma OPENCL EXTENSION cl_khr_fp64: enable

#define maxSteps 2048
#define tstep (2.0 / maxSteps)

// intersect ray with a box
// http://www.siggraph.org/education/materials/HyperGraph/raytrace/rtinter3.htm

int intersectBox(float4 r_o, float4 r_d, float4 boxmin, float4 boxmax, float *tnear, float *tfar)
{
    // compute intersection of ray with all six bbox planes
    float4 invR = (float4)(1.0f,1.0f,1.0f,1.0f) / r_d;
    float4 tbot = invR * (boxmin - r_o);
    float4 ttop = invR * (boxmax - r_o);

    // re-order intersections to find smallest and largest on each axis
    float4 tmin = min(ttop, tbot);
    float4 tmax = max(ttop, tbot);

    // find the largest tmin and the smallest tmax
    float largest_tmin = max(max(tmin.x, tmin.y), max(tmin.x, tmin.z));
    float smallest_tmax = min(min(tmax.x, tmax.y), min(tmax.x, tmax.z));

	*tnear = largest_tmin;
	*tfar = smallest_tmax;

	return smallest_tmax > largest_tmin;
}

uint rgbaFloatToInt(float3 rgb)
{
    rgb.x = clamp(rgb.x,0.0f,1.0f);  
    rgb.y = clamp(rgb.y,0.0f,1.0f);  
    rgb.z = clamp(rgb.z,0.0f,1.0f);  
    return ((uint)(1)<<24) | ((uint)(rgb.z*255.0f)<<16) | ((uint)(rgb.y*255.0f)<<8) | (uint)(rgb.x*255.0f);
}

float step01(float x, float x0, float vs){
/*
    if(x <= x0 - vs)
        return 0.;

    if(x >= x0 + vs)
        return 1.;
*/
    return (x-(x0-vs))/(2*vs);
}

float step10(float x, float x0, float vs) {
/*
    if(x <= x0 - vs)
        return 1.0;

    if(x >= x0 + vs)
        return 0.0;
*/
    return ((x0+vs)-x)/(2*vs);
}

float gyroid(float vs, float voxelScale, float level, float factor, float thickness, float3 offset, float3 pnt) {
    pnt = pnt - offset;

    pnt = pnt * factor;

    float d = fabs((sin(pnt.x) * cos(pnt.y) + sin(pnt.y) * cos(pnt.z) + sin(pnt.z) * cos(pnt.x) - level) / factor) - (thickness + voxelScale * vs);

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

float readShapeJS(float3 pos) {
    // gyroid params
    float factor = 2 * 3.14159265 / 0.2;
//    float vs = 0.0001;
    float vs = tstep;
//    float thickness = 0.004;
    float thickness = 0.04;
    float level = 0;
    float voxelScale = 1;
    float radius = 1;

    float data1 = clamp(gyroid(vs,voxelScale,level,factor,thickness, (float3)(0,0,0),pos),0.0f,1.0f);
    float data2 = clamp(sphere(vs, radius, 0, 0, 0, true, pos),0.0f,1.0f);

    // Intersection op
    float data3 = subtraction(data2,data1);

    // TODO: Not certain why this is necessary
    data3 = clamp(data3,0.0f,1.0f);

    return data3;
}

kernel void render(global uint *d_output, uint imageW, uint imageH, global const float* invViewMatrix) {
    uint x = get_global_id(0);
    uint y = get_global_id(1);

    float u = (x / (float) imageW)*2.0f-1.0f;
    float v = (y / (float) imageH)*2.0f-1.0f;

    float4 boxMin = (float4)(-1.0f, -1.0f, -1.0f,1.0f);
    float4 boxMax = (float4)(1.0f, 1.0f, 1.0f,1.0f);

    // calculate eye ray in world space
    float4 eyeRay_o;    // eye origin
    float4 eyeRay_d;    // eye direction

    eyeRay_o = (float4)(invViewMatrix[3], invViewMatrix[7], invViewMatrix[11], 1.0f);   

    float4 temp = normalize(((float4)(u, v, -2.0f,0.0f)));
    eyeRay_d.x = dot(temp, ((float4)(invViewMatrix[0],invViewMatrix[1],invViewMatrix[2],invViewMatrix[3])));
    eyeRay_d.y = dot(temp, ((float4)(invViewMatrix[4],invViewMatrix[5],invViewMatrix[6],invViewMatrix[7])));
    eyeRay_d.z = dot(temp, ((float4)(invViewMatrix[8],invViewMatrix[9],invViewMatrix[10],invViewMatrix[11])));
    eyeRay_d.w = 0.0f;

    // find intersection with box
	float tnear, tfar;
	int hit = intersectBox(eyeRay_o, eyeRay_d, boxMin, boxMax, &tnear, &tfar);

    // TODO: Always clear, optimize this
    if ((x < imageW) && (y < imageH)) {
        uint idx =(y * imageW) + x;
        d_output[idx] = 0;
    }
    if (!hit) {
        if ((x < imageW) && (y < imageH)) {
            // write output color
            uint i =(y * imageW) + x;
            d_output[i] = 0;
        }
#ifdef DEBUG2
if (y==979) {
printf("x: %4d y: %4d eye o: %5.2v4f d: %5.2v4f   hit: %d\n",x,y,eyeRay_o,eyeRay_d,hit);
}
#endif
        return;
    }
	if (tnear < 0.0f) tnear = 0.0f;     // clamp to near plane

    hit = -1;
    // march along ray from tnear till we hit something
    float t = tnear;

    float4 tpos;
    float3 pos;
    for(uint i=0; i<maxSteps; i++) {
        tpos = eyeRay_o + eyeRay_d*t;
        pos.x = tpos.x;
        pos.y = tpos.y;
        pos.z = tpos.z;
        //pos = pos*0.5f+0.5f;    // map position to [0, 1] coordinates

        // read from grid

        float density = readShapeJS(pos);
#ifdef DEBUG
if (density > 1 && x==171 && y==160) {
printf("high density: x: %4d y: %4d dens: %7.4f\n",x,y,density);
}
#endif
        if (density > 0.5 && density <= 1) {
           hit = i;

           // adjust hit based on density to reduce aliasing
#ifdef DEBUG2
if (y==979) {
printf("density: %d  pos: %7.4v4f\n",density,pos);
}
#endif
           //pos = eyeRay_o + eyeRay_d*(t - ((1.0 - density) * tstep));   // no longer necessary?
#ifdef DEBUG2
if (y==979) {
printf("          new pos: %7.4v4f\n",pos);
}
#endif
           break;
        }

        t += tstep;
        if (t > tfar) break;
    }

#ifdef DEBUG2
if (y==979 && hit == -1) {
printf("x: %4d y: %4d eye o: %5.2v4f d: %5.2v4f   hit: %3d   tnear: %4.1f tfar: %4.1f\n",x,y,eyeRay_o,eyeRay_d,hit,tnear,tfar);
}
#endif
    if ((hit != -1) && (x < imageW) && (y < imageH)) {
    //printf("hit: x: %d y: %d\n",x,y);
        // write output color
        uint i =(y * imageW) + x;

/*
        // fake shading shows steps distance
        float color = ((float) (maxSteps - hit))/maxSteps;
        float4 shading = (float4)(color,color,color,0.25f);
*/

        // use exact answer for a sphere
        //float3 grad = normalize((float3)(pos.x,pos.y,pos.z));

        // Gradient Calc - http://stackoverflow.com/questions/21272817/compute-gradient-for-voxel-data-efficiently
        float3 grad;
        //float dist = tstep*1000; // TODO: make one voxel size?
        double dist = tstep; // TODO: make one voxel size?

        // second order precision formula for gradient
        // x
        float xd0 = readShapeJS((float3) (pos.x + dist, pos.y, pos.z));
        float xd1 = readShapeJS((float3) (pos.x, pos.y, pos.z));
        float xd2 = readShapeJS((float3) (pos.x - dist, pos.y, pos.z));
        grad.x = ((double)xd2 - xd0)/((double)2*dist);
        //grad.x = (xd1 - xd0) * (1.0f - dist) + (xd2 - xd1) * dist; // lerp
        // y
        float yd0 = readShapeJS((float3) (pos.x,pos.y + dist, pos.z));
        float yd1 = readShapeJS((float3) (pos.x, pos.y, pos.z));
        float yd2 = readShapeJS((float3) (pos.x, pos.y - dist, pos.z));
        grad.y = ((double)yd2 - yd0)/((double)2*dist);
        //grad.y = (yd1 - yd0) * (1.0f - dist) + (yd2 - yd1) * dist; // lerp
        // z
        float zd0 = readShapeJS((float3) (pos.x,pos.y, pos.z + dist));
        float zd1 = readShapeJS((float3) (pos.x, pos.y, pos.z));
        float zd2 = readShapeJS((float3) (pos.x, pos.y, pos.z - dist));
        grad.z = ((double)zd2 - zd0)/((double)2*dist);
        //grad.z = (zd1 - zd0) * (1.0f - dist) + (zd2 - zd1) * dist; // lerp

#ifdef DEBUG
if (x==171 && y==160) {
printf("x: %4d y: %4d xd0: %7.4f xd1: %7.4f xd2: %7.5f grad: %7.4f\n",x,y,xd0,xd1,xd2,grad.x);
printf("   pos: %7.4v3f dist: %7.4f xd2: %7.4v3f xd0: %7.5v3f\n",pos,dist,(float3)(pos.x - dist, pos.y, pos.z),(float3) (pos.x + dist, pos.y, pos.z));
}
#endif

        // TODO: hardcode headlight from eye direction
        // from this equation: http://en.wikipedia.org/wiki/Phong_reflection_model
        float ambient = 0.1;

        float3 lm = (float3) (eyeRay_o.x - pos.x,eyeRay_o.y - pos.y, eyeRay_o.z - pos.z);
        float3 n = normalize(grad);  //  use gradient for normal at the surface
        float3 shading = dot(lm,n) + ambient;
        //float3 shading = normalize(grad);

        d_output[i] = rgbaFloatToInt(shading);

#ifdef DEBUG2
if (y==979) {
printf("x: %4d y: %4d eye o: %5.2v4f d: %5.2v4f   hit: %3d   tnear: %4.1f tfar: %4.1f color: %4.3f\n",x,y,eyeRay_o,eyeRay_d,hit,tnear,tfar,shading);
}
#endif

    }
}

