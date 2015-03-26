// blending function 
float blendQuadric(float x){
	return (1.f-x)*(1.f - x)*0.25f;
}

float blendMin(float a, float b, float w){

    float dd = min(a,b);
    float d = fabs(a-b);
    if( d < w) return dd - w*blendQuadric(d/w);	
    else return dd;
}

float blendMax(float a, float b, float w){

    float dd = max(a,b);
    float d = fabs(a-b);
    if( d < w) return dd + w*blendQuadric(d/w);
    else return dd;
}

// copy from global to local using individual workers 
void copyToLocalMT(global const int *pgBufFrom, local int *plBufTo,int count){
    
    // fancy way of making copy. 
    int sx =  get_local_size(0);
    int sy =  get_local_size(1);
    int ix =  get_local_id(0);
    int iy =  get_local_id(1);
    int wi = ix + sx * iy; // worker index 
    int wg = sx*sy;        // workers count 
    int ws = count/wg;   // jobs per worker 
    int remainder = count % wg; // remainder to copy for 
    int roffset = ws*wg; // offset to remainder 
    int wo = wi*ws; // worker offset 
    for(int i = 0; i < ws; i++){
        plBufTo[wo+i] = pgBufFrom[wo+i];
    }
    // do end of buffer 
    if(wi < remainder) plBufTo[roffset+wi] = pgBufFrom[roffset+wi];
    barrier(CLK_LOCAL_MEM_FENCE);    
}
 

// copy from global to local ST 
void copyToLocal(global const int *pgBufFrom, local int *plBufTo,int count){

    for(int i = 0; i < count; i++){
        plBufTo[i] = pgBufFrom[i];
    }
        
}



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

float4 mulMatVec4(global const float*mat, float4 vec){
	float f0 = dot(vec, ((float4)(mat[0],mat[1],mat[2],mat[3])));
	float f1 = dot(vec, ((float4)(mat[4],mat[5],mat[6],mat[7])));
	float f2 = dot(vec, ((float4)(mat[8],mat[9],mat[10],mat[11])));
	float f3 = 0;
	return (float4)(f0, f1, f2, f3);
}

// Utility functions
float step01(float x, float x0, float vs){  // this is used for density
    if(x <= -vs)
        return 0;

    if(x >=  vs)
        return 1;

    return (x-(x0-vs))/(2*vs);
}


float step10(float x, float x0, float vs) { // this is used for density
   if(x <= x0 - vs)
       return 1;

   if(x >= x0 + vs)
       return 0;

    return ((x0+vs)-x)/(2*vs);
}

