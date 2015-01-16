#define BlendWidth 0.0003
#define CUBIC_BLEND_VALUE 0.05  // value above 0.25 makes blending flater, below 0.25 - heigher blending 
// 0 - linear blend, 1 - quadric blend, 2 cubic blend 
#define BlendType 1

// Utility functions
float step01_(float x, float x0, float vs){  // this is used for density 
    if(x <= -vs)
        return 0.0;

    if(x >=  vs)
        return 1.0;

    return (x-(x0-vs))/(2*vs);
}

float step01(float x, float x0, float vs){ // used for distance 
    return (x0-x);
}

float step10_(float x, float x0, float vs) { // this is used for density 
   if(x <= x0 - vs)
       return 1.0;

   if(x >= x0 + vs)
       return 0.0;

    return ((x0+vs)-x)/(2*vs);
}

float step10(float x, float x0, float vs) { // used for distance 
     return (x-x0);
}


float intervalCap(float x, float xmin, float xmax, float vs){

    if(xmin >= xmax-vs)
        return 0;

    float vs2 = vs*2;
    float vxi = clamp((x-(xmin-vs))/vs2,0.0f,1.0f);
    float vxa = clamp((((xmax+vs)-x))/vs2,0.0f,1.0f);

    return vxi*vxa;
}

float blendQuadric(float x){
	return (x - 1.)*(x - 1.)*0.25;
}

float blendCubic(float x, float v){
    float d = v;
	float c = -0.5;
	float a = c + 2*d;
	float b = -0.5*(3*a + c);
	return ((a*x+b)*x + c)*x + d;
}

float blendLinear(float x){
	return (1-x)*0.5;
}

float blendFunc(float x) {
    switch(BlendType){
	  case 0:
		return blendLinear(x);
	  default:
	  case 1:
		return blendQuadric(x);
	  case 2:
		return blendCubic(x,CUBIC_BLEND_VALUE);
	}
}

float blendMin(float a, float b, float w){
	
    float dd = min(a,b);
    float d = fabs(a-b);
    if( d < w) return dd - w*blendFunc(d/w);
	else return dd;
}

float blendMax(float a, float b, float w){

    float dd = max(a,b);
    float d = fabs(a-b);
    if( d < w) return dd + w*blendFunc(d/w);
    else return dd;
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

    if (radius > 0.) {
        return r-radius;
    } else {
        return -radius-r;
    }
}

float box(float vs, float xmin, float xmax, float ymin, float ymax, float zmin, float zmax, float3 pnt) {

	float x = pnt.x;
	float y = pnt.y;
	float z = pnt.z;
	float w = BlendWidth; 
	float dx = max(xmin-x, x-xmax);
	float dy = max(ymin-y, y-ymax);
	float dz = max(zmin-z, z-zmax);
	float d = blendMax(dx, dy,w);
    d = blendMax(d, dz,w);
    return d;
}

float torus(float vs, float3 center, float rout, float rin, float3 pnt) {
    float x = pnt.x - center.x;
    float y = pnt.y - center.y;
    float z = pnt.z - center.z;

    float rxy = sqrt(x*x + y*y) - rout;

    return step10(((rxy*rxy + z*z) - rin*rin)/(2*rin), 0, vs);
}

float subtraction(float a, float b) {

	float w = BlendWidth;// blending width 0.2mm       
    return blendMin(a,-b,w);
	
}

float intersectionOp(float a, float b) {

	float w = BlendWidth;// blending width 0.2mm       
    return blendMax(a,b,w);
	
}

float intersectionArr(float * src, int len) {

	float w = BlendWidth;// blending width 0.2mm       
    float ret = src[0];
#pragma unroll
    for(int i=1; i < len; i++) {
       ret = blendMax(ret, src[i], w);
    }

    return ret;
}

// TODO: benchmark if this is worth having, in theory loops are expensive
float unionOp(float a, float b) {
    float w = BlendWidth;
    return blendMin(a,b,w);    
}

float unionArr(float * src, int len) {
    float w = BlendWidth;
    float ret = src[0];
#pragma unroll
    for(int i=1; i < len; i++) {
       ret = blendMin(ret,src[i],w);
    }
    return ret;
}
