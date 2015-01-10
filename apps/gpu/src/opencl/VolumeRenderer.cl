#define maxSteps 100
#define tstep 0.01f

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

uint rgbaFloatToInt(float4 rgba)
{
    rgba.x = clamp(rgba.x,0.0f,1.0f);  
    rgba.y = clamp(rgba.y,0.0f,1.0f);  
    rgba.z = clamp(rgba.z,0.0f,1.0f);  
    rgba.w = clamp(rgba.w,0.0f,1.0f);  
    return ((uint)(rgba.w*255.0f)<<24) | ((uint)(rgba.z*255.0f)<<16) | ((uint)(rgba.y*255.0f)<<8) | (uint)(rgba.x*255.0f);
}

float step10(float x, float x0, float vs) {
    if(x <= x0 - vs)
        return 1.0;

    if(x >= x0 + vs)
        return 0.0;

    return ((x0+vs)-x)/(2*vs);
}

uint readDensity(float4 pos,uint debug) {
    float r = sqrt(pos.x * pos.x + pos.y * pos.y + pos.z * pos.z);
    //uint ret = step10(r,1,0.0001);
    uint ret;

    if (r < 1) ret = 1;  else ret =0;

    if (debug) printf((__constant char *)"   r: %6.4f  ret: %d\n",r,ret);

    return ret;
}

//__kernel void render(__global uint *d_output, uint imageW, uint imageH, __constant float* invViewMatrix)
__kernel void render(global uint *d_output, uint imageW, uint imageH, global const float* invViewMatrix)

{	
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
#ifdef DEBUG
if (y==79) {
printf("x: %4d y: %4d eye o: %5.2v4f d: %5.2v4f   hit: %d\n",x,y,eyeRay_o,eyeRay_d,hit);
}
#endif
        return;
    }
	if (tnear < 0.0f) tnear = 0.0f;     // clamp to near plane

    hit = -1;
    // march along ray from tnear till we hit something
    float t = tnear;

    for(uint i=0; i<maxSteps; i++) {
        float4 pos = eyeRay_o + eyeRay_d*t;
        //pos = pos*0.5f+0.5f;    // map position to [0, 1] coordinates

        // read from grid

        uint debug = 0;
        uint density = readDensity(pos,debug);

        if (density > 0) {
           hit = i;
           break;
        }

        t += tstep;
        if (t > tfar) break;
    }

#ifdef DEBUG
if (y==79 && hit == -1) {
printf("x: %4d y: %4d eye o: %5.2v4f d: %5.2v4f   hit: %3d   tnear: %4.1f tfar: %4.1f\n",x,y,eyeRay_o,eyeRay_d,hit,tnear,tfar);
}
#endif
    if ((hit != -1) && (x < imageW) && (y < imageH)) {
    //printf("hit: x: %d y: %d\n",x,y);
        // write output color
        uint i =(y * imageW) + x;
        float color = ((float) (maxSteps - hit))/maxSteps;
        float4 temp = (float4)(color,color,color,0.25f);

        d_output[i] = rgbaFloatToInt(temp);
#ifdef DEBUG
if (y==79) {
printf("x: %4d y: %4d eye o: %5.2v4f d: %5.2v4f   hit: %3d   tnear: %4.1f tfar: %4.1f color: %4.3f\n",x,y,eyeRay_o,eyeRay_d,hit,tnear,tfar,color);
}
#endif

    }
}

