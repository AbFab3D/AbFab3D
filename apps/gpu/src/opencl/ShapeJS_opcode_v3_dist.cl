// size of struct in words 
#define WSIZE(A) (sizeof(A)/sizeof(int))
//#define NAN (0xFFFF)

// stack size for intermediate memory 
#define STACK_SIZE  10

// this list should be identical to list in src/java/opencl/Opcodes.java
#define oSPHERE        1
#define oGYROID        2
#define oBOX           3
#define oTORUS         4
#define oMAX           5
#define oMIN           6
#define oBLEND         7
#define oBLENDMAX      8
#define oBLENDMIN      9
#define oSUBTRACT      10
#define oBLENDSUBTRACT 11
#define oCOPY_D1D2     12
#define oCOPY_D2D1     13
#define oPUSH_D2       14
#define oPOP_D2        15
#define oPUSH_P1       16
#define oPOP_P1        17
#define oTRANSLATION   18
#define oROTATION      19
#define oSCALE         20
#define oENGRAVE       21

#define oEND   0


// poimnter to buffer of opcodes 
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
    float factor;
    float3 offset;
} sGyroid;

void oGyroid(PTR sGyroid *g, sVec *in, sVec *out){
    float3 pnt = in->v.xyz;
    pnt -= g->offset;
    pnt *= g->factor;
    
    float d = fabs((sin(pnt.x) * cos(pnt.y) + sin(pnt.y) * cos(pnt.z) + sin(pnt.z) * cos(pnt.x) - g->level) / g->factor) - (g->thickness);

    out->v.x = d;    

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


void oMax(PTR void *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = max(in1->v.x, in2->v.x);

}

void oMin(PTR void *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = min(in1->v.x, in2->v.x);

}

void oSubtract(PTR void *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = max(in1->v.x, -in2->v.x);

}

// blend 
typedef struct {
    int size;
    int opcode;
    float width;
    float padding;
} sBlend;


void oBlendMin(PTR sBlend *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = blendMin(in1->v.x, in2->v.x, ptr->width);

}

void oBlendMax(PTR sBlend *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = blendMax(in1->v.x, in2->v.x, ptr->width);

}

void oBlendSubtract(PTR sBlend *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = blendMax(in1->v.x, -in2->v.x, ptr->width);

}

// edgrave 
typedef struct {
    int size;
    int opcode;
    float blendWidth;
    float depth;
} sEngrave;
// engrave shape1 with shape2 
void oEngrave(PTR sEngrave *ptr, sVec *in1, sVec *in2, sVec *out){

    //float eng = max(0.,min(ptr->depth, -in2->v.x));
    float eng = blendMax(0.,blendMin(ptr->depth, -in2->v.x,ptr->blendWidth ),ptr->blendWidth);
    //out->v.x = blendMax(in1->v.x, -eng, ptr->blendWidth);
    out->v.x = in1->v.x + eng;

}

typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode to perform 
    // custom parameters of DataSource 
    float3 translation; 
} sTranslation;

void oTranslation(PTR sTranslation *trans, sVec *inout){
    // inverse translation 
    inout->v.xyz -= trans->translation;

}

typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode to perform 
    // custom parameters of sScale
    float averageFactor; // inverse of average scale 
    float3 factor; // inverse of scale 
    float3 center; // center of scale 
} sScale;

void oScale(PTR sScale *pS, sVec *inout){
    
    inout->v.xyz -= pS->center;
    inout->v.xyz *= pS->factor;
    inout->v.xyz += pS->center;
    // TODO - adjustment of total scale 
}

typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode to perform 
    // custom parameters of sRotation 
    float3 center; 
    float3 m0; 
    float3 m1; 
    float3 m2; 
} sRotation;

void oRotation(PTR sRotation *rot, sVec *inout){

    float3 vec = inout->v.xyz;
    vec -= rot->center;   
    vec = (float3){dot(rot->m0,vec),dot(rot->m1,vec),dot(rot->m2,vec)};
    vec += rot->center;    
    
    inout->v.xyz = vec;
}


// union to "safely" convert pointers 
typedef union {
    PTR void *pv;  
    PTR int *w;
} CPtr;


// for debugging 
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

/**
   opcode - stream of opcodes 
   opCount total count of opcodes 
   pnt - input point 
   result - output data value 
 */
void getShapeJSData(PTR int* opcode, int opCount, sVec *pnt, sVec *result) {
    
    CPtr ptr;  // pointer to dta struct to convert from int* to structs* 
    int offsetIn = 0;  // current offset in the input data 
    sVec stack[STACK_SIZE];
    int stackPos = 0; // current stack position

    sVec pnt1;   // current working point 
    
    sVec data1 = (sVec){.v=(float4)(0,0,0,0)}; // register to store current data value
    sVec data2;  // register for intermediate data 

    // original point 
    pnt1 = *pnt;

    int resOffset = 0;
    
    for(int i=0; i < opCount; i++) {
        int size = opcode[offsetIn];

        if(size <= 0)
            break;

        int code = opcode[offsetIn+1];
        ptr.w = (opcode + offsetIn);

        switch(code){
        default:

            *result = data1;
            return;            

        case oSPHERE:
            
            oSphere(ptr.pv, &pnt1, &data1);
            break;

        case oGYROID:
            
            oGyroid(ptr.pv, &pnt1, &data1);        
            break;

        case oBOX:
            
            oBox(ptr.pv, &pnt1, &data1);
            break;

        case oTORUS:
            
            oTorus(ptr.pv, &pnt1, &data1);
            break;

        case oCOPY_D1D2:
            
            data2 = data1;
            break;            
            
        case oCOPY_D2D1:
            
            data1 = data2;
            break;
                                    
        case oMAX:
            
            oMax(ptr.pv,&data2, &data1,&data2);
            break;
            
        case oMIN:
            
            oMin(ptr.pv,&data2, &data1,&data2);
            break;
            
        case oBLENDMIN:            

            oBlendMin(ptr.pv, &data1,&data2, &data2);            
            break;
            
        case oBLENDMAX:
            
            oBlendMax(ptr.pv, &data1,&data2, &data2);        
            break;
            
        case oSUBTRACT:
            
            oSubtract(ptr.pv,&data1, &data2,&data2);            
            break;
            
        case oBLENDSUBTRACT:
            
            oBlendSubtract(ptr.pv,&data1, &data2,&data2);
            break;            

        case oENGRAVE:
            
            oEngrave(ptr.pv,&data1, &data2,&data2);
            break;            

        case oPUSH_D2:
            
            stack[stackPos++] = data2;
            break;            
        case oPOP_D2:
            
            data2 = stack[--stackPos];            
            break;

        case oPUSH_P1:
            
            stack[stackPos++] = pnt1;
            break;            
        case oPOP_P1:
            
            pnt1 = stack[--stackPos];            
            break;

        case oTRANSLATION:
            
            oTranslation(ptr.pv,&pnt1);
            break;            

        case oROTATION:
            
            oRotation(ptr.pv,&pnt1);
            break;            

        case oSCALE:
            
            oScale(ptr.pv,&pnt1);
            break;            


        }
        offsetIn += size;
    }

    *result = data1;
    return;            
}

