// size of struct in words 
#define WSIZE(A) (sizeof(A)/sizeof(int))
//#define NAN (0xFFFF)

// stack size for intermediate memory 
// stack >= 4000 generates CL_OUT_OF_HOST_MEMORY error 
#define STACK_SIZE  10
// maximal size of struct for single operation (in words) should be the size of largest operation 
#define MAXOPSIZE 30 

#include "opcodes.h"


// pointer to buffer of opcodes 
//#define PTROPS global const
// pointer to local ops buffer
#define PTROPS global 
// pointer to struct in private memory 
#define PTRS global
// pointer to buffer of large data 
#define PTRDATA global 


// union to "safely" convert pointers 
typedef union {
    PTRS void *pv;  
    PTRS int *w;
} CPtr;

//#define NAN 0xFFFFFFFF

// desription of the scene to render 
typedef struct {
    float worldScale;      //
    float3 worldCenter;    // center of the world box
    PTROPS int *pOps;      // operations 
    int opCount;           // count of operations 
    PTRDATA char* pgData;  // global large data
    global const float* invvm; // inverse view matrix
} Scene; 


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

void oSphere(PTRS sSphere *sphere, sVec *in, sVec *out){

    float3 v = in->v.xyz;
    v -= sphere->center;
    float len = length(v);
    float radius = sphere->radius;
    float d = sign(radius) * len - radius;

    out->v.x = d;

}


typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode to perform 
    // custom parameters of DataSource 
    float3 translation; 
} sTranslation;

void oTranslation(PTRS sTranslation *trans, sVec *inout){
    // inverse translation 
    inout->v.xyz -= trans->translation;

}


typedef struct {
    int type;
    float radius;
    float3 center; 
} sSPlane;
#define PLANE  0
#define SPHERE  1


void oReflect(PTRS sSPlane *s, sVec *inout){

    switch(s->type){

    case PLANE: 
        {
            float3 center = s->center;
            float radius = s->radius;
            //float vn = dot( inout->v.xyz - center * radius, center);
            inout->v.xyz -= (2.*dot( inout->v.xyz - center * radius, center))* center;
            
        } break;
    case SPHERE:
        {
            float3 v = inout->v.xyz;
            v -= s->center;
            float len2 = dot(v,v);
            float r2 = s->radius;
            r2 *= r2;
            float factor = (r2/len2);
            v *= factor;
            v += s->center; 
            
            inout->scale *= factor;
            inout->v.xyz = v;           
        } break;
    } 
}


#include "noise3d.cl"

// OpenCL Kernel Function for opcode reading 
kernel void opcodeReader(PTRDATA int* opcode, int opCount, PTRDATA int* outdata, int outCount, PTRDATA int *result) {
    
    CPtr ptr;  // pointer to opcodes struct to convert from int* to structs* 
    sVec pnt1 = (sVec){.v =(float4)(1.,1.,0.,0), .scale = 1}; 
    sVec pnt2 = (sVec){.v =(float4)(0,0,0,0), .scale = 1}; 
    sVec data1 = (sVec){.v =(float4)(0,0,0,0), .scale = 1}; 
    
    int offsetIn = 0;  // current offset in the opcodes 
    int cnt = 0;

    *(result+ cnt++) = as_int(pnt1.v.x);
    *(result+ cnt++) = as_int(pnt1.v.y);
    *(result+ cnt++) = as_int(pnt1.v.z);
    *(result+ cnt++) = NAN;

    for(int i=0; i < opCount; i++) {
        int size = opcode[offsetIn];

        if(size <= 0)
            break;
        
        int code = opcode[offsetIn+1];
        ptr.w = (opcode + offsetIn);
        
        switch(code){
        default:
            return;            

        case oSPHERE:
            
            oSphere(ptr.pv, &pnt1, &data1);
            *(result+ cnt++) = as_int(data1.v.x);
            *(result+ cnt++) = as_int(data1.v.y);
            *(result+ cnt++) = as_int(data1.v.z);
            *(result+ cnt++) = NAN;
            break;

        case oTRANSLATION:
            
            oTranslation(ptr.pv,&pnt1);
            *(result+ cnt++) = as_int(pnt1.v.x);
            *(result+ cnt++) = as_int(pnt1.v.y);
            *(result+ cnt++) = as_int(pnt1.v.z);
            *(result+ cnt++) = NAN;
            break;            

        case oREFLECT:

            oReflect(ptr.pv, &pnt1);
            
            *(result+ cnt++) = as_int(pnt1.v.x);
            *(result+ cnt++) = as_int(pnt1.v.y);
            *(result+ cnt++) = as_int(pnt1.v.z);
            *(result+ cnt++) = NAN;
            break;

        case oNOISE3D:
            //oNoise3D(ptr, &pnt1, &data1, 0);
            break;
        }

        offsetIn += size;
    }
}
