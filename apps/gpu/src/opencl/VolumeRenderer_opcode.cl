#define tstep (2.0 / maxSteps)
#define sstep (2.0 / 0)
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

/*
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

        float density = readShapeJS(op,len,fparams,iparams,fvparams,bparams,mparams,pos);

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
		   //float gp = (readShapeJS(op,len,fparams,iparams,fvparams,bparams,mparams,pa)-density)/dt;
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
*/

float4 mulMatVec4(global const float*mat, float4 vec){
	float f0 = dot(vec, ((float4)(mat[0],mat[1],mat[2],mat[3])));
	float f1 = dot(vec, ((float4)(mat[4],mat[5],mat[6],mat[7])));
	float f2 = dot(vec, ((float4)(mat[8],mat[9],mat[10],mat[11])));
	float f3 = 0;
	return (float4)(f0, f1, f2, f3);
}

float readShapeJS(const global int * op, int len, const global float * fparams, const global int * iparams, const global float3 * fvparams, const global char * bparams, const global float16 * mparams, float3 pos0) {
   int f_idx = 0;
   int i_idx = 0;
   int fv_idx = 0;
   int b_idx = 0;
   int m_idx = 0;

   float results[10];

   float fparam1;
   float fparam2;
   float fparam3;
   float3 fvparam1;
   float3 fvparam2;
   float16 mparam1;
   int iparam1;
   int iparam2;
   char bparam1;

   float vs = voxelSize;
   float3 pos = pos0;

   int ridx = 0;
   for(int i=0; i < len; i++) {
      switch(op[i]) {
	    case 0:
	        fvparam1 = fvparams[fv_idx++];
	        fparam1 = fparams[f_idx++];
	        bparam1 = bparams[b_idx++];
	        results[ridx++] = sphere(voxelSize,fvparam1,fparam1,bparam1,pos);
	        break;
	    case 1:
	        fvparam1 = fvparams[fv_idx++];
	        fvparam2 = fvparams[fv_idx++];
	        results[ridx++] = box(voxelSize,fvparam1,fvparam2,pos);
	        break;
	    case 2:
	        fparam1 = fparams[f_idx++]; // level
	        fparam2 = fparams[f_idx++]; // thickness
	        fvparam1 = fvparams[fv_idx++]; // offset
	        fparam3 = fparams[f_idx++];    // factor

	        results[ridx++] = gyroid(voxelSize,fparam1,fparam2,fvparam1,fparam3,pos);
	        break;
	    case 3:
	        iparam1 = iparams[i_idx++];
	        iparam2 = iparams[i_idx++];
	        results[ridx++] = intersectionOp(results[iparam1],results[iparam2]);    // TODO: I suspect this indirect indexing is bad for performance
	        break;
	    case 4:
	        iparam1 = iparams[i_idx++];
	        iparam2 = iparams[i_idx++];

	        results[ridx++] = unionOp(results[iparam1],results[iparam2]);
	        break;
	    case 5:
	        iparam1 = iparams[i_idx++];
	        iparam2 = iparams[i_idx++];

	        results[ridx++] = subtraction(results[iparam1],results[iparam2]);
	        break;
	    case 6:   // intersection arr
	        iparam1 = iparams[i_idx++];   // count

            results[ridx] = results[iparams[i_idx++]];
	        for(int i=1; i < iparam1; i++) {
	            results[ridx] = min(results[ridx],results[iparams[i_idx++]]);
            }
            ridx++;
	        break;
	    case 7:   // union arr
	        iparam1 = iparams[i_idx++];   // count

            results[ridx] = results[iparams[i_idx++]];
	        for(int i=1; i < iparam1; i++) {
	            results[ridx] = max(results[ridx],results[iparams[i_idx++]]);
            }
            ridx++;
	        break;
	    case 8:
            fvparam1 = fvparams[fv_idx++];
            fparam1 = fparams[f_idx++];
            fparam2 = fparams[f_idx++];
            results[ridx++] = torus(voxelSize,fvparam1,fparam1,fparam2,pos);
            break;
	    case 1000: // reset
	        pos = pos0;
	        fvparam1 = fvparams[fv_idx++];
	        pos /= fvparam1;
	        break;
	    case 1001:  // scale
	        fvparam1 = fvparams[fv_idx++];
	        pos = scale(fvparam1,pos);

	        // TODO: should this change vs
	        break;
        case 1002: // translation
	        fvparam1 = fvparams[fv_idx++];
	        pos = translation(fvparam1,pos);
	        break;
	    case 1003: // rotation
	        fvparam1 = fvparams[fv_idx++];
	        mparam1 = mparams[m_idx++];
	        pos = rotation(fvparam1,mparam1,pos);
	        break;

      }
   }

   return results[ridx-1];
}

float readShapeJSDebug(const global int * op, int len, const global float * fparams, const global int * iparams, const global float3 * fvparams, const global char * bparams, float3 pos0) {
   int f_idx = 0;
   int i_idx = 0;
   int fv_idx = 0;
   int b_idx = 0;

   float results[10];

   float fparam1;
   float fparam2;
   float fparam3;
   float3 fvparam1;
   float3 fvparam2;
   int iparam1;
   int iparam2;
   char bparam1;

  float vs = voxelSize;
   float3 pos = pos0;

   int ridx = 0;
   for(int i=0; i < len; i++) {
      int opCode = op[i];

      switch(opCode) {
	    case 0:
	        fvparam1 = fvparams[fv_idx++];
	        fparam1 = fparams[f_idx++];
	        bparam1 = bparams[b_idx++];
	        results[ridx++] = sphere(vs,fvparam1,fparam1,bparam1,pos);
	        break;
	    case 1:
	        fvparam1 = fvparams[fv_idx++];
	        fvparam2 = fvparams[fv_idx++];
	        results[ridx++] = box(vs,fvparam1,fvparam2,pos);
	        break;
	    case 2:
	        fparam1 = fparams[f_idx++];
	        fparam2 = fparams[f_idx++];
	        fvparam1 = fvparams[fv_idx++];
	        fparam3 = fparams[f_idx++];
#ifdef DEBUG 
		printf("Pos into gyoid: %v3f\n",pos);
#endif 
	        results[ridx++] = gyroidDebug(vs,fparam1,fparam2,fvparam1,fparam3,pos);
	        break;
	    case 3:
	        iparam1 = iparams[i_idx++];
	        iparam2 = iparams[i_idx++];
	        results[ridx++] = intersectionOp(results[iparam1],results[iparam2]);    // TODO: I suspect this indirect indexing is bad for performance
	        break;
	    case 4:
	        iparam1 = iparams[i_idx++];
	        iparam2 = iparams[i_idx++];

	        results[ridx++] = unionOp(results[iparam1],results[iparam2]);
	        break;
	    case 5:
	        iparam1 = iparams[i_idx++];
	        iparam2 = iparams[i_idx++];

	        results[ridx++] = subtraction(results[iparam1],results[iparam2]);
	        break;
	    case 1000:  // scale
	        fvparam1 = fvparams[fv_idx++];
	        pos *= fvparam1;
#ifdef DEBUG
	        printf("Scaling pos to: %v3f\n",pos);
#endif 
	        break;
      }
   }

   return results[ridx-1];
}

float3 renderPixel(uint x, uint y, float u, float v, float tnear, float tfar, uint imageW, uint imageH, global const float* invViewMatrix, global const int * op, int len, global const float * fparams, global const int * iparams, global const float3 * fvparams, global const char * bparams, global const float16 * mparams) {
    // calculate eye ray in world space
    float4 eyeRay_o;    // eye origin
    float4 eyeRay_d;    // eye direction
	
    eyeRay_o = (float4)(invViewMatrix[3], invViewMatrix[7], invViewMatrix[11], 1.0f);
				   
    float4 temp = normalize(((float4)(u, v, -2.f,0.0f)));
	eyeRay_d = mulMatVec4(invViewMatrix,temp);

    int hit = -1;
    // march along ray from tnear till we hit something
    float t = tnear;

    float4 tpos;
    float3 pos;
    float density;

    tpos = eyeRay_o + eyeRay_d*t;
    pos = tpos.xyz; 
    density = readShapeJS(op,len,fparams,iparams,fvparams,bparams,mparams,pos);
	if (density > 0.5){  // solid on the boundary 
		return (float3)(1.f,0,0);
	}	

    for(uint i=0; i < maxSteps; i++) {
        tpos = eyeRay_o + eyeRay_d*t;
        pos = tpos.xyz; 

        density = readShapeJS(op,len,fparams,iparams,fvparams,bparams,mparams,pos);

        if (density > 0.5){  // overshot the surface
			int backcount = 10;
			while(density > 0.5 && backcount-- > 0 ){
			   t -= 0.1*tstep;  // back off 
			   pos = (eyeRay_o + eyeRay_d*t).xyz;
			   density = readShapeJS(op,len,fparams,iparams,fvparams,bparams,mparams,pos);
			}			   
           hit = i;
		   float dt = 0.01*tstep;
		   float3 p1 = (eyeRay_o + eyeRay_d*(t+dt)).xyz;
		   float gp = (readShapeJS(op,len,fparams,iparams,fvparams,bparams,mparams,p1)-density)/dt;
		   float ddt = (0.5-density)/gp;
		   //if( true ){
		   if( true) {//ddt > -0.5*tstep && ddt < 0.5*tstep){
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
//        float dist = voxelSize / 2.0; // This is what I expect we should use or just voxelSize
        float dist = tstep*0.01;

        // second order precision formula for gradient
        // x
        float xd0 = readShapeJS(op,len,fparams,iparams,fvparams,bparams,mparams,(float3) (pos.x + dist, pos.y, pos.z));
        float xd2 = readShapeJS(op,len,fparams,iparams,fvparams,bparams,mparams,(float3) (pos.x - dist, pos.y, pos.z));
        grad.x = (xd2 - xd0)/(2*dist);
        //grad.x = (xd1 - xd0) * (1.0f - dist) + (xd2 - xd1) * dist; // lerp
        // y
        float yd0 = readShapeJS(op,len,fparams,iparams,fvparams,bparams,mparams,(float3) (pos.x,pos.y + dist, pos.z));
        float yd2 = readShapeJS(op,len,fparams,iparams,fvparams,bparams,mparams,(float3) (pos.x, pos.y - dist, pos.z));
        grad.y = (yd2 - yd0)/(2*dist);
        //grad.y = (yd1 - yd0) * (1.0f - dist) + (yd2 - yd1) * dist; // lerp
        // z
        float zd0 = readShapeJS(op,len,fparams,iparams,fvparams,bparams,mparams,(float3) (pos.x,pos.y, pos.z + dist));
        float zd2 = readShapeJS(op,len,fparams,iparams,fvparams,bparams,mparams,(float3) (pos.x, pos.y, pos.z - dist));
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

        // matlab style lighting
        float3 ambient = (float3) (0.1,0.1,0.1);
        float4 light1a =  (float4)(10.f,0, 20.f,0);//float (float3)(-10,0,20);
        float3 light1_color = (float3) (0.8f,0,0);
        float4 light2a = (float4)(10.f, 10.f, 20.f,0);// (float3)(-10,-10,20);
        float3 light2_color = (float3) (0,0.8f,0);
        float4 light3a = (float4)(0.f, 10.f, 20.f,0);//(float3)(0,-10,20);
        float3 light3_color = (float3) (0,0,0.8f);

/*
        // tony lighting
        //float3 ambient = (float3) (0.1,0.1,0.1);
//        float3 ambient = (float3) (0.1,0.1,0.1);
        float3 ambient = (float3) (0.4,0.4,0.4);
        float lscale = 0.65;
        float key = 0.8f * lscale;
        float fill = 0.25f * lscale;
        float rim = 1.0f * lscale;
        float3 light_color = (float3) (255.0/255.0 * lscale, 255/255.0 * lscale, 251.0 / 255.0 * lscale);  // high noon sun
        float4 light1a =  (float4)(6.5f,-6.5f, 10.f,0);  // key light
//        float3 light1_color = (float3) (key,key,key);
        float3 light1_color = key * light_color;
        float4 light2a = (float4)(10.f, 1.f, -10.f,0);  // fill light
        float3 light2_color = fill * light_color;
        float4 light3a = (float4)(-10.f, 9.0f, 10.f,0);  // rim light
        float3 light3_color = rim * light_color;
*/
        // WSF params
//        float3 mat_diffuse = (float3) 0.831;
        float3 mat_diffuse = (float3) 1;

		float3 light1, light2, light3;

        light1 = mulMatVec4(invViewMatrix,light1a).xyz; 
        light2 = mulMatVec4(invViewMatrix,light2a).xyz; 
        light3 = mulMatVec4(invViewMatrix,light3a).xyz; 

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
kernel void renderSuper(global uint *d_output, uint imageW, uint imageH, global const float* invViewMatrix, global const int * op, int len, global const float * fparams, global const int * iparams, global const float3 * fvparams, global const char * bparams, global const float16 * mparams) {
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
	eyeRay_d = mulMatVec4(invViewMatrix, temp);

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

    sum += renderPixel(x,y,u - subPixel,v - subPixel,tnear,tfar,imageW,imageH,invViewMatrix,op,len,fparams,iparams,fvparams,bparams,mparams);
    sum += renderPixel(x,y,u + subPixel,v - subPixel,tnear,tfar,imageW,imageH,invViewMatrix,op,len,fparams,iparams,fvparams,bparams,mparams);
    sum += renderPixel(x,y,u - subPixel,v + subPixel,tnear,tfar,imageW,imageH,invViewMatrix,op,len,fparams,iparams,fvparams,bparams,mparams);
    sum += renderPixel(x,y,u + subPixel,v + subPixel,tnear,tfar,imageW,imageH,invViewMatrix,op,len,fparams,iparams,fvparams,bparams,mparams);

    float3 shading = sum / 4;
/*
    float subPixel = (1 / (float) imageW)*2.0f / samples / 4;

    // TODO: we should change to rotated grid pattern: http://en.wikipedia.org/wiki/Supersampling
    // TODO: we can factor out the bounding box test to speed this up
    float3 sum = (float3)(0,0,0);
    float u = (x / (float) imageW)*2.0f-1.0f;
    float v = (y / (float) imageH)*2.0f-1.0f;

    sum += renderPixel(x,y,u - 2*subPixel,v - 2*subPixel,imageW,imageH,invViewMatrix,);
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
kernel void render(global uint *d_output, uint imageW, uint imageH, global const float* invViewMatrix, global const int * op, int len, global const float * fparams, global const int * iparams, global const float3 * fvparams, global const char * bparams, global const float16 * mparams) {
    uint x = get_global_id(0);
    uint y = get_global_id(1);

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

    float3 shading = renderPixel(x,y,u,v,tnear,tfar,imageW,imageH,invViewMatrix,op,len,fparams,iparams,fvparams,bparams,mparams);

    shading = clamp(shading, 0.0f, 1.0f);

    uint idx =(y * imageW) + x;
    d_output[idx] = rgbaFloatToInt(shading);
}
#endif
