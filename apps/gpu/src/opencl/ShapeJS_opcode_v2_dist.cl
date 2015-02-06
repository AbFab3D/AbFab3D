#define BlendWidth 0.0005
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

//float step10(float x, float x0, float vs) { // used for distance
//     return (x-x0);
//}


float intervalCap(float x, float xmin, float xmax, float vs){
    return smoothstep(xmin-vs,xmin+vs,x) * (1 - smoothstep(xmax-vs,xmax+vs,x));
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
    //if( d < w) return dd - w*blendQuadric(d/w);	else return dd;
    //return dd;
    return dd - (1.f-step(w,d))* w*blendQuadric(d/w);
}

float blendMax(float a, float b, float w){

    float dd = max(a,b);
    float d = fabs(a-b);
    //return dd;
    //if( d < w) return dd + w*blendQuadric(d/w); else return dd;
    return dd + (1.f-step(w,d))*w*blendQuadric(d/w);;
}
