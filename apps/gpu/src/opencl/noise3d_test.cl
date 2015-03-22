
typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode to perform 
    // custom parameters of DataSource 
    float offset;
    float factor;
    float3 scale; // scale to transform to grid coords 
    int3 dimension; // grid dimensions
    int gradOffset; // offset to gradient data in bytes
} sNoise3D;


// positive reminder
float preminder(float x, int y){
    return (x - y*floor(x/y));
}

float3 prem3(float3 p, int3 dim){
    return (float3)(preminder(p.x,dim.x),preminder(p.y,dim.y),preminder(p.z,dim.z));
}

float cubic(float t) {
    return t*t *(3 - 2*t); 
}

int noiseGrad(int x, int y, int z, int nx, int nxy){
    return (x + y*nx + z * nxy)*3;
}

float noiseMag(PTRDATA char *grad, float x, float y, float z){

    return (grad[0]*x + grad[1]*y + grad[2]*z)/127.;

}



float noiseWeight(float dx, float dy, float dz){
    return cubic(1-dx)*cubic(1-dy)*cubic(1-dz);     
}

int wrap0(int x,int n){
    //return x;
    if(x < 0) return (x+n);    
    else return (x);    
}

int wrap1(int x,int n){
    if(x >= n) return (x-n);    
    else return (x);    
}

void oNoise3D(PTRS sNoise3D *s, sVec *in, sVec *out, Scene *pScene){


    float3 p = in->v.xyz;
    p *= s->scale;
    int3 dim = s->dimension;
    int nx = dim.x;
    int ny = dim.y;
    int nz = dim.z;
    int nxy = nx*ny;

    //p = prem3(p,dim);
    p.x = preminder(p.x,nx);
    p.y = preminder(p.y,ny);
    p.z = preminder(p.z,nz);
    
    // half voxel shift 
    p -= 0.5;

    int3 ip = (int3)((int)floor(p.x),(int)floor(p.y),(int)floor(p.z));
    
    int3 ip1 = ip+1;
    float3 dp = (float3)(p.x - ip.x,p.y - ip.y,p.z - ip.z);
    float3 dp1 = dp-1.;

    //if(ip.x < 0 ) ip.x += dim.x;
    ip.x = wrap0(ip.x,nx);
    ip.y = wrap0(ip.y,ny);
    ip.z = wrap0(ip.z,nz);
    ip1.x = wrap1(ip1.x,nx);
    ip1.y = wrap1(ip1.y,ny);
    ip1.z = wrap1(ip1.z,nz);

    
    int 
        g000 = noiseGrad( ip.x,  ip.y,  ip.z, nx, nxy),
        g100 = noiseGrad(ip1.x,  ip.y,  ip.z, nx, nxy),
        g110 = noiseGrad(ip1.x, ip1.y,  ip.z, nx, nxy),
        g010 = noiseGrad( ip.x, ip1.y,  ip.z, nx, nxy),
        g001 = noiseGrad( ip.x,  ip.y, ip1.z, nx, nxy),
        g101 = noiseGrad(ip1.x,  ip.y, ip1.z, nx, nxy),
        g111 = noiseGrad(ip1.x, ip1.y, ip1.z, nx, nxy),
        g011 = noiseGrad( ip.x, ip1.y, ip1.z, nx, nxy);

    PTRDATA char *pGrad = (PTRDATA char *)(pScene->pgData + s->gradOffset);

    float 
        m000 = noiseMag(pGrad+g000, dp.x,  dp.y,  dp.z),
        m100 = noiseMag(pGrad+g100, dp1.x, dp.y,  dp.z),
        m110 = noiseMag(pGrad+g110, dp1.x, dp1.y, dp.z),
        m010 = noiseMag(pGrad+g010, dp.x,  dp1.y, dp.z),
        m001 = noiseMag(pGrad+g001, dp.x,  dp.y,  dp1.z),
        m101 = noiseMag(pGrad+g101, dp1.x, dp.y,  dp1.z),
        m111 = noiseMag(pGrad+g111, dp1.x, dp1.y, dp1.z),
        m011 = noiseMag(pGrad+g011, dp.x,  dp1.y, dp1.z);

    float 
        w000 = noiseWeight(  dp.x, dp.y,  dp.z),
        w100 = noiseWeight(-dp1.x, dp.y,  dp.z),
        w010 = noiseWeight( dp.x, -dp1.y, dp.z),
        w110 = noiseWeight(-dp1.x,-dp1.y, dp.z),
        w001 = noiseWeight( dp.x,  dp.y, -dp1.z),
        w101 = noiseWeight(-dp1.x, dp.y, -dp1.z),
        w011 = noiseWeight( dp.x, -dp1.y,-dp1.z),
        w111 = noiseWeight(-dp1.x,-dp1.y,-dp1.z);
    
    float d = 
        w000*m000 + w100*m100 + w110*m110 + w010*m010 +
        w001*m001 + w101*m101 + w111*m111 + w011*m011;            

    // for test 
    out->v.x = 0.0005*sin(in->v.x*pGrad[0]*10)*sin(in->v.y*pGrad[1]*10)*sin(in->v.z*pGrad[2]*10);
    //out->v.x = d*s->factor + s->offset;
}
