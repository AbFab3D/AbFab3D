#define tstep (2.0 / maxSteps)
#define sstep (2.0 / maxShadowSteps)
#define clearColor (1,1,1)
#define clearInt 33554431 // White color


////////////////////////////////////////////////////////////////////////////////////////////////////
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

int intersectBox3(float3 r_o, float3 r_d, float3 boxmin, float3 boxmax, float *tnear, float *tfar)
{
    // compute intersection of ray with all six bbox planes
    float3 invR = (float3)(1.0f,1.0f,1.0f) / r_d;
    float3 tbot = invR * (boxmin - r_o);
    float3 ttop = invR * (boxmax - r_o);

    // re-order intersections to find smallest and largest on each axis
    float3 tmin = min(ttop, tbot);
    float3 tmax = max(ttop, tbot);

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
    return (x-(x0-vs))/(2*vs);
}

float step10(float x, float x0, float vs) {
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

float intersection(float a, float b) {
    return min(a,b);
}

float readShapeJS(float3 pos) {
    // gyroid params
    float factor = 2 * 3.14159265 / 1;
//    float vs = 0.0001;
    float vs = tstep * 2;
//    float thickness = 0.004;
    float thickness = 0.04;
    float level = 0;
    float voxelScale = 1;
    float radius = 1;

    //float data1 = clamp(gyroid(vs,voxelScale,level,factor,thickness, (float3)(0,0,0),pos),0.0f,1.0f);
    //float data2 = clamp(sphere(vs, radius, 0, 0, 0, true, pos),0.0f,1.0f);

    float data1 = gyroid(vs,voxelScale,level,factor,thickness, (float3)(0,0,0),pos);
    float data2 = sphere(vs, radius, 0, 0, 0, true, pos);

    // Intersection op
    float data3 = intersection(data2,data1);

    float max_noise = 0.1;

    // Don't add noise to empty space
    if (data3 < 0.0001) {
       return data3;
    }


    float bias = 128.0f;
    float scale = 200.0f;
    float lacurnarity = 2.02f;
    float increment = 1.0f;
    float octaves = 3.3f;
//    float amplitude = 0.1f;
//    float amplitude = tstep * 50;
    float amplitude = tstep * 5;

    float3 sample = (pos + bias);
    float noise = turbulence3d((float4)(sample,1.0), scale, lacurnarity, increment, octaves) * amplitude;

    if (noise > max_noise) noise = max_noise;
    if (noise < -max_noise) noise = -max_noise;
    data3 += noise;

    // TODO: Not certain why this is necessary

    data3 = clamp(data3,0.0f,1.0f);

   return data3;
}

float readShapeJSSubtract(float3 pos) {
    // gyroid params
    float factor = 2 * 3.14159265 / 0.2;
//    float vs = 0.0001;
    float vs = tstep * 2;
//    float thickness = 0.004;
    float thickness = 0.04;
    float level = 0;
    float voxelScale = 1;
    float radius = 1;

    float data1 = clamp(gyroid(vs,voxelScale,level,factor,thickness, (float3)(0,0,0),pos),0.0f,1.0f);
    float data2 = clamp(sphere(vs, radius, 0, 0, 0, true, pos),0.0f,1.0f);

    // Intersection op
    float data3 = subtraction(data2,data1);

/*
    // Don't add noise to empty space
    if (data3 < 0.01) {
       return data3;
    }


    float bias = 128.0f;
    float scale = 100.0f;
    float lacurnarity = 2.02f;
    float increment = 1.0f;
    float octaves = 3.3f;
//    float amplitude = 0.1f;
    float amplitude = tstep * 50;

    float3 sample = (pos + bias);
    float noise = turbulence3d((float4)(sample,1.0), scale, lacurnarity, increment, octaves) * amplitude;
    data3 += noise;
*/
    // TODO: Not certain why this is necessary
    data3 = clamp(data3,0.0f,1.0f);

   return data3;
}

// Can p0 see p1, if so returns 1
uint canSee(float3 p0, float3 p1
   #ifdef DEBUG
      ,uint debug
   #endif
   ) {
    float3 dir = p1 - p0;

    float3 boxMin = (float3)(-1.0f, -1.0f, -1.0f);
    float3 boxMax = (float3)(1.0f, 1.0f, 1.0f);
    float tnear, tfar;

	int hit = intersectBox3(p0, dir, boxMin, boxMax, &tnear, &tfar);

#ifdef DEBUG
if (debug) printf("bbx hit: %d near: %7.4f far: %7.4f\n",hit,tnear,tfar);
#endif

    if (!hit) return 1;  // this should never happen?

    float3 pos;
    float t = 2 * tstep;  // force outside current hit

    hit = -1;
    for(uint i=0; i < maxShadowSteps; i++) {
        pos = p0 + dir*t;

        // read from grid

        float density = readShapeJS(pos);

#ifdef DEBUG
if (debug) printf("step: %d pos: %7.4v3f dens: %7.4f\n",i,pos,density);
#endif
        if (density > 0.2 && density <= 1.) {
#ifdef DEBUG
if (debug) printf("   hit at: %d  dens: %7.4f\n",i,density);
#endif

           hit = i;
           break;

		   //float dt = 0.001;
		   //float3 pa = (p0 + dir*(t+dt));
		   //float gp = (readShapeJS(pa)-density)/dt;
		   //float ddt = (0.5-density)/gp;
		   //if( ddt > -5.f && ddt < 5.f){
				// adjust hit based on density to reduce aliasing
			//	pos = (eyeRay_o + eyeRay_d*(t + ddt)).xyz;
		//		break;
		//	}

		}
        t += sstep;
        if (t > tfar) break; // TODO: add back bbox exit
    }

#ifdef DEBUG
if (debug) printf("final hit: %d\n",hit);
#endif

    if (hit == -1) return 1;

    return 0;
}


float3 renderPixel(uint x, uint y, float u, float v, float tnear, float tfar, uint imageW, uint imageH, global const float* invViewMatrix) {
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

	if (tnear < 0.0f) tnear = 0.0f;     // clamp to near plane

    int hit = -1;
    // march along ray from tnear till we hit something
    float t = tnear;

    float4 tpos;
    float3 pos;
    float density;
    for(uint i=0; i < maxSteps; i++) {
        tpos = eyeRay_o + eyeRay_d*t;
        pos.x = tpos.x;
        pos.y = tpos.y;
        pos.z = tpos.z;
        //pos = pos*0.5f+0.5f;    // map position to [0, 1] coordinates

        // read from grid

        density = readShapeJS(pos);

        if (density > 0.2 && density <= 1.) {
           hit = i;
		   float dt = 0.001;
		   float3 p1 = (eyeRay_o + eyeRay_d*(t+dt)).xyz;
		   float gp = (readShapeJS(p1)-density)/dt;
		   float ddt = (0.5-density)/gp;
		   if( ddt > -5.f && ddt < 5.f){
				// adjust hit based on density to reduce aliasing
				pos = (eyeRay_o + eyeRay_d*(t + ddt)).xyz;
				break;
			}
		}
        t += tstep;
        if (t > tfar) break;
    }

    if ((hit != -1) && (x < imageW) && (y < imageH)) {
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
        float dist = tstep*0.1; // works for subtract script

        // second order precision formula for gradient
        // x
        float xd0 = readShapeJS((float3) (pos.x + dist, pos.y, pos.z));
        float xd2 = readShapeJS((float3) (pos.x - dist, pos.y, pos.z));
        grad.x = (xd2 - xd0)/(2*dist);
        //grad.x = (xd1 - xd0) * (1.0f - dist) + (xd2 - xd1) * dist; // lerp
        // y
        float yd0 = readShapeJS((float3) (pos.x,pos.y + dist, pos.z));
        float yd2 = readShapeJS((float3) (pos.x, pos.y - dist, pos.z));
        grad.y = (yd2 - yd0)/(2*dist);
        //grad.y = (yd1 - yd0) * (1.0f - dist) + (yd2 - yd1) * dist; // lerp
        // z
        float zd0 = readShapeJS((float3) (pos.x,pos.y, pos.z + dist));
        float zd2 = readShapeJS((float3) (pos.x, pos.y, pos.z - dist));
        grad.z = (zd2 - zd0)/(2*dist);
        //grad.z = (zd1 - zd0) * (1.0f - dist) + (zd2 - zd1) * dist; // lerp

#ifdef DEBUG
if (x==171 && y==160) {
printf("x: %4d y: %4d dens: %7.4f xd0: %7.4f xd2: %7.5f grad: %7.4f\n",x,y,density,xd0,xd2,grad.x);
printf("   pos: %7.4v3f dist: %7.4f xd2: %7.4v3f xd0: %7.5v3f\n",pos,dist,(float3)(pos.x - dist, pos.y, pos.z),(float3) (pos.x + dist, pos.y, pos.z));
}
#endif

        // TODO: hardcode headlight from eye direction
        // from this equation: http://en.wikipedia.org/wiki/Phong_reflection_model
/*
        float ambient = 0.1;
        float3 lm = (float3) (eyeRay_o.x - pos.x,eyeRay_o.y - pos.y, eyeRay_o.z - pos.z);
        float3 n = normalize(grad);  //  use gradient for normal at the surface
        float3 shading = dot(normalize(lm),n) + ambient;
        //float3 shading = normalize(grad);
*/

        float3 n = normalize(grad);  //  use gradient for normal at the surface
/*
        // matlab style lighting
        float3 ambient = (float3) (0.1,0.1,0.1);
        float3 light1a =  (float3)(10.f,0, 20.f);//float (float3)(-10,0,20);
        float3 light1_color = (float3) (0.8f,0,0);
        float3 light2a = (float3)(10.f, 10.f, 20.f);// (float3)(-10,-10,20);
        float3 light2_color = (float3) (0,0.8f,0);
        float3 light3a = (float3)(0.f, 10.f, 20.f);//(float3)(0,-10,20);
        float3 light3_color = (float3) (0,0,0.8f);
*/

        // tony lighting
        //float3 ambient = (float3) (0.1,0.1,0.1);
//        float3 ambient = (float3) (0.1,0.1,0.1);
        float3 ambient = (float3) (0.4,0.4,0.4);
        float lscale = 0.65;
        float key = 0.8f * lscale;
        float fill = 0.25f * lscale;
        float rim = 1.0f * lscale;
        float3 light_color = (float3) (255.0/255.0 * lscale, 255/255.0 * lscale, 251.0 / 255.0 * lscale);  // high noon sun
        float3 light1a =  (float3)(6.5f,-6.5f, 10.f);  // key light
//        float3 light1_color = (float3) (key,key,key);
        float3 light1_color = key * light_color;
        float3 light2a = (float3)(10.f, 1.f, -10.f);  // fill light
        float3 light2_color = fill * light_color;
        float3 light3a = (float3)(-10.f, 9.0f, 10.f);  // rim light
        float3 light3_color = rim * light_color;

        // WSF params
//        float3 mat_diffuse = (float3) 0.831;
        float3 mat_diffuse = (float3) 1;

		float3 light1, light2, light3;

        light1.x = dot((float4)(light1a,0), ((float4)(invViewMatrix[0],invViewMatrix[1],invViewMatrix[2],invViewMatrix[3])));
        light1.y = dot((float4)(light1a,0),((float4)(invViewMatrix[4],invViewMatrix[5],invViewMatrix[6],invViewMatrix[7])));
        light1.z = dot((float4)(light1a,0), ((float4)(invViewMatrix[8],invViewMatrix[9],invViewMatrix[10],invViewMatrix[11])));
        light2.x = dot((float4)(light2a,0), ((float4)(invViewMatrix[0],invViewMatrix[1],invViewMatrix[2],invViewMatrix[3])));
        light2.y = dot((float4)(light2a,0),((float4)(invViewMatrix[4],invViewMatrix[5],invViewMatrix[6],invViewMatrix[7])));
        light2.z = dot((float4)(light2a,0), ((float4)(invViewMatrix[8],invViewMatrix[9],invViewMatrix[10],invViewMatrix[11])));
        light3.x = dot((float4)(light3a,0), ((float4)(invViewMatrix[0],invViewMatrix[1],invViewMatrix[2],invViewMatrix[3])));
        light3.y = dot((float4)(light3a,0),((float4)(invViewMatrix[4],invViewMatrix[5],invViewMatrix[6],invViewMatrix[7])));
        light3.z = dot((float4)(light3a,0), ((float4)(invViewMatrix[8],invViewMatrix[9],invViewMatrix[10],invViewMatrix[11])));

/*
        // fixed lighting
        light1 = light1a;
        light2 = light2a;
        light3 = light3a;
*/
		float3 light1_sum = 0;
		float3 light2_sum = 0;
		float3 light3_sum = 0;

        #ifdef DEBUG
        uint debug = 0;
        if (x==171 && y==160) {
           printf("Debugging pixel\n");
           debug = 1;
        }
        #endif

        #ifdef SHADOWS
        if (canSee(pos,light1
           #ifdef DEBUG
              ,debug
           #endif
        )) light1_sum = dot(normalize(light1),n) * light1_color;
        if (canSee(pos,light2
           #ifdef DEBUG
              ,debug
           #endif
        )) light2_sum = dot(normalize(light2),n) * light2_color;
        if (canSee(pos,light3
           #ifdef DEBUG
              ,debug
           #endif
        )) light3_sum = dot(normalize(light3),n) * light3_color;
        #endif

        #ifndef SHADOWS
           light1_sum = dot(normalize(light1),n) * light1_color * mat_diffuse;
           light2_sum = dot(normalize(light2),n) * light2_color * mat_diffuse;
           light3_sum = dot(normalize(light3),n) * light3_color * mat_diffuse;
        #endif

//        float3 shading = light1_sum + light2_sum + light3_sum + ambient;
        float3 shading = fabs(light1_sum) + fabs(light2_sum) + fabs(light3_sum) + ambient;

        return shading;
    }

    return (float3)clearColor;
}

#ifdef SUPERSAMPLE
kernel void renderSuper(global uint *d_output, uint imageW, uint imageH, global const float* invViewMatrix) {
    uint x = get_global_id(0);
    uint y = get_global_id(1);

    // TODO: Always clear, optimize this
    if ((x < imageW) && (y < imageH)) {
        uint idx =(y * imageW) + x;
        d_output[idx] = clearInt;
    }

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

    if (!hit) {
        if ((x < imageW) && (y < imageH)) {
            // write output color
            uint i =(y * imageW) + x;
            d_output[i] = clearInt;

            return;
        }
    }

	if (tnear < 0.0f) tnear = 0.0f;     // clamp to near plane

    float subPixel = (1 / (float) imageW)*2.0f / samples / 2;

    // TODO: we should change to rotated grid pattern: http://en.wikipedia.org/wiki/Supersampling
    // TODO: we can factor out the bounding box test to speed this up
    float3 sum = (float3)(0,0,0);

    sum += renderPixel(x,y,u - subPixel,v - subPixel,tnear,tfar,imageW,imageH,invViewMatrix);
    sum += renderPixel(x,y,u + subPixel,v - subPixel,tnear,tfar,imageW,imageH,invViewMatrix);
    sum += renderPixel(x,y,u - subPixel,v + subPixel,tnear,tfar,imageW,imageH,invViewMatrix);
    sum += renderPixel(x,y,u + subPixel,v + subPixel,tnear,tfar,imageW,imageH,invViewMatrix);

    float3 shading = sum / 4;
/*
    float subPixel = (1 / (float) imageW)*2.0f / samples / 4;

    // TODO: we should change to rotated grid pattern: http://en.wikipedia.org/wiki/Supersampling
    // TODO: we can factor out the bounding box test to speed this up
    float3 sum = (float3)(0,0,0);
    float u = (x / (float) imageW)*2.0f-1.0f;
    float v = (y / (float) imageH)*2.0f-1.0f;

    sum += renderPixel(x,y,u - 2*subPixel,v - 2*subPixel,imageW,imageH,invViewMatrix);
    sum += renderPixel(x,y,u - subPixel,v - 2*subPixel,imageW,imageH,invViewMatrix);
    sum += renderPixel(x,y,u + subPixel,v - 2*subPixel,imageW,imageH,invViewMatrix);
    sum += renderPixel(x,y,u + 2*subPixel,v - 2*subPixel,imageW,imageH,invViewMatrix);

    sum += renderPixel(x,y,u - 2*subPixel,v - subPixel,imageW,imageH,invViewMatrix);
    sum += renderPixel(x,y,u - subPixel,v - subPixel,imageW,imageH,invViewMatrix);
    sum += renderPixel(x,y,u + subPixel,v - subPixel,imageW,imageH,invViewMatrix);
    sum += renderPixel(x,y,u + 2*subPixel,v - subPixel,imageW,imageH,invViewMatrix);

    sum += renderPixel(x,y,u - 2*subPixel,v + subPixel,imageW,imageH,invViewMatrix);
    sum += renderPixel(x,y,u - subPixel,v + subPixel,imageW,imageH,invViewMatrix);
    sum += renderPixel(x,y,u + subPixel,v + subPixel,imageW,imageH,invViewMatrix);
    sum += renderPixel(x,y,u + 2*subPixel,v + subPixel,imageW,imageH,invViewMatrix);

    sum += renderPixel(x,y,u - 2*subPixel,v + 2*subPixel,imageW,imageH,invViewMatrix);
    sum += renderPixel(x,y,u - subPixel,v + 2*subPixel,imageW,imageH,invViewMatrix);
    sum += renderPixel(x,y,u + subPixel,v + 2*subPixel,imageW,imageH,invViewMatrix);
    sum += renderPixel(x,y,u + 2*subPixel,v + 2*subPixel,imageW,imageH,invViewMatrix);

    float3 shading = sum / 16;
  */
    uint idx =(y * imageW) + x;
    d_output[idx] = rgbaFloatToInt(shading);
}
#endif

#ifndef SUPERSAMPLE
kernel void render(global uint *d_output, uint imageW, uint imageH, global const float* invViewMatrix) {
    uint x = get_global_id(0);
    uint y = get_global_id(1);

    // TODO: Always clear, optimize this
    if ((x < imageW) && (y < imageH)) {
        uint idx =(y * imageW) + x;
        d_output[idx] = clearInt;
    }

    float3 sum = (float3)(0,0,0);
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

    if (!hit) {
        if ((x < imageW) && (y < imageH)) {
            // write output color
            uint i =(y * imageW) + x;
            d_output[i] = clearInt;

            return;
        }
    }
	if (tnear < 0.0f) tnear = 0.0f;     // clamp to near plane

    float3 shading = renderPixel(x,y,u,v,tnear,tfar,imageW,imageH,invViewMatrix);

    shading = clamp(shading, 0.0, 1.0);

    uint idx =(y * imageW) + x;
    d_output[idx] = rgbaFloatToInt(shading);
}
#endif
