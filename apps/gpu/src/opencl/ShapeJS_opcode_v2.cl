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

/*
float intervalCap(float x, float xmin, float xmax, float vs){

    if(xmin >= xmax-vs)
        return 0;

    float vs2 = vs*2;
    float vxi = clamp((x-(xmin-vs))/vs2,0.0f,1.0f);
    float vxa = clamp((((xmax+vs)-x))/vs2,0.0f,1.0f);

    return vxi*vxa;
}
*/

float intervalCap(float x, float xmin, float xmax, float vs){
    return smoothstep(xmin-vs,xmin+vs,x) * (1 - smoothstep(xmax-vs,xmax+vs,x));
}

