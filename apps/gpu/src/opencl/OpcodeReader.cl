// size of struct in words 
#define WSIZE(A) (sizeof(A)/sizeof(int))
//#define NAN (0xFFFF)

#define oSPHERE 1001
#define oGYROID 1002
#define oBOX    1003
#define oTORUS    1004

#define PTR global const


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


typedef struct{
    float4 v;  // 3D point
    float scale; // scaling factor
} sVec;


typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode to perform 
    // custom patrameters of DataSource 
    float radius;  // sphere radius (positive radius - interior of sphere, negative - exterior) 
    float3 center; // center   
} sSphere;

void oSphere(PTR sSphere *sphere, sVec *in, sVec *out){

    float3 v = in->v.xyz;
    v -= sphere->center;
    float len = length(v);
    float radius = sphere->radius;
    float d = sign(radius) * len - radius;

    out->v.x = d;

}

// box 
typedef struct {
    int size;
    int opcode;
    float rounding;
    float3 center;
    float3 halfsize;
} sBox;

void oBox(PTR sBox *box, sVec *in, sVec *out){

    float3 v = in->v.xyz;
    v -= box->center;    
    v = fabs(v);
    v -= box->halfsize;

    float d = blendMax(blendMax(v.x,v.y,box->rounding),v.z,box->rounding);

    out->v.x = d;
}

// gyroid 
typedef struct {
    int size;
    int opcode;
    float level;
    float thickness;
    float period;
    float3 offset;
} sGyroid;

void oGyroid(PTR sGyroid *p, sVec *in, sVec *out){
    //TODO
    out->v.x = p->offset.z;    

}

// torus 
typedef struct {
    int size;
    int opcode;
    float r;
    float R;
    float3 center;
} sTorus;

void oTorus(PTR sTorus *torus, sVec *in, sVec *out){

    float3 p = in->v.xyz;
    p -= torus->center;

    p.y = length(p.xy) - torus->R;

    float d = length(p.yz) - torus->r;

    out->v.x = d;    

}


// union to "safely" convert pointers 
typedef union {
    PTR void *pv;  
    PTR int *w;
} CPtr;


void copyOpcodes(PTR int *opcode, global int *outdata){

    int opcount = 0;
    int maxCount = 10;
    int offsetIn = 0;
    int offsetOut = 0;

    while(opcount++ < maxCount){
        
        int size = opcode[offsetIn++];
        outdata[offsetOut++] = size;
        // end of opcode queue 
        if(size <= 0) 
            break;        
        int code = opcode[offsetIn++];
        outdata[offsetOut++] = code;
        int scount = (size-2);
        while(scount-- > 0){
            outdata[offsetOut++] = opcode[offsetIn++];
        }                           
    }    
}


// OpenCL Kernel Function for opcode reading 
kernel void OpcodeReader(PTR int* opcode, int opCount, global int* outdata, int outCount, global int *results) {
		
    int iGID = get_global_id(0);
    if(iGID > 0) { 
        return;
    }
    
    // for debugging 
    copyOpcodes(opcode, outdata);
    
    CPtr ptr;
    int count = 0;
    int offsetIn = 0;
    sVec pnt = (sVec){.v=(float4)(0,0,0,0), .scale = 1.f};
    sVec data;

    int resOffset = 0;

    while(count++ < opCount) {

        int size = opcode[offsetIn];
        if(size == 0)
            break;
        int code = opcode[offsetIn+1];
        ptr.w = (opcode + offsetIn);
        switch(code){
        default:
            results[resOffset++] = NAN;
            break;
        case oSPHERE:
            {
                oSphere(ptr.pv, &pnt, &data);
                results[resOffset++] = as_int(data.v.x);
            }
            break;
        case oGYROID:
            {
                oGyroid(ptr.pv, &pnt, &data);
                results[resOffset++] = as_int(data.v.x);
            }
            break;
        case oBOX:
            {
                oBox(ptr.pv, &pnt, &data);
                results[resOffset++] = as_int(data.v.x);
            }
            break;
        case oTORUS:
            {
                oTorus(ptr.pv, &pnt, &data);
                results[resOffset++] = as_int(data.v.x);
            }
            break;
        }        
        offsetIn += size;
    }
    results[resOffset++] = 0;
    
}
