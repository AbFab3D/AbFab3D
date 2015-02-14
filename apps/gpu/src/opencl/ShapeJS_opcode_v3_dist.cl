// size of struct in words 
#define WSIZE(A) (sizeof(A)/sizeof(int))
//#define NAN (0xFFFF)

// stack size for intermediate memory 
#define STACK_SIZE  10  

#define oSPHERE 1001
#define oGYROID 1002
#define oBOX    1003
#define oTORUS  1004
#define oMAX    1005
#define oMIN    1006
#define oBLEND  1007
#define oBLENDMAX  1008
#define oBLENDMIN  1009
#define oSUBTRACT  1010
#define oBLENDSUBTRACT  1011
#define oCOPY_D1D2  1012
#define oCOPY_D2D1  1013
#define oPUSH_D2  1014
#define oPOP_D2  1015



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


void getShapeJSData(PTR int* opcode, int opCount, sVec *pnt, sVec *result) {
		
    int iGID = get_global_id(0);
    if(iGID > 0) { 
        return;
    }    
    // for debugging 
    //copyOpcodes(opcode, outdata);
    
    CPtr ptr;
    int count = 0; // count of codes 
    int offsetIn = 0;  // current input location 
    sVec stack[STACK_SIZE];
    int stackPos = 0; // current stack position
    sVec pnt1;
    // original point 
    pnt1 = *pnt;
    //sVec pnt2 = (sVec){.v=(float4)(0,0,0,0), .scale = 1.f};

    sVec data1 = (sVec){.v=(float4)(0,0,0,0)}; // register to store data value
    sVec data2; // register for intermediate data storage 

    int resOffset = 0;
    
    // to prevent infinite cycle

    int maxcount = 100;
    while(count++ < maxcount) {

        int size = opcode[offsetIn];
        if(size == 0)
            break;
        int code = opcode[offsetIn+1];
        ptr.w = (opcode + offsetIn);

        switch(code){
        default:

            *result = data1;
            return;            

        case oEND:

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
            
            oSubtract(ptr.pv,&data1, &data2,&data1);            
            break;
            
        case oBLENDSUBTRACT:
            
            oBlendSubtract(ptr.pv,&data1, &data2,&data1);            
            break;            

        case oPUSH_D2:
            
            stack[stackPos++] = data2;
            break;            
        case oPOP_D2:
            
            data2 = stack[--stackPos];            
            break;            
        }        
        offsetIn += size;
    }

    *result = data1;
    return;            
}

