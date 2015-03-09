// size of struct in words 
#define WSIZE(A) (sizeof(A)/sizeof(int))
//#define NAN (0xFFFF)

// stack size for intermediate memory 
// stack >= 4000 generates CL_OUT_OF_HOST_MEMORY error 
#define STACK_SIZE  1000
// maximal size of struct for single operation (in words) should be the size of largest operation 
#define MAXOPSIZE 30 

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
#define oGRID2DBYTE    22
#define oGRID3DBYTE    23

#define oEND   0


// pointer to buffer of opcodes 
//#define PTROPS global const
// pointer to local ops buffer
#define PTROPS local 
// pointer to struct in private memory 
#define PTRS local 
// pointer to buffer of large data 
#define PTRDATA global const


// desription of the scene to render 
typedef struct {
    float worldScale;      //
    float3 worldCenter;    // center of the world box
    PTROPS int *pOps;      // operations 
    int opCount;           // count of operations 
    PTRDATA char* pgData;  // global large data
    global const float* invvm; // inverse view matrix
} Scene; 


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

void oSphere(PTRS sSphere *sphere, sVec *in, sVec *out){

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
    float rounding;  // rounding of box edges in 
    float3 center;   // center of the box 
    float3 halfsize; // half size of the box 
} sBox;

void oBox(PTRS sBox *box, sVec *in, sVec *out){

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

void oGyroid(PTRS sGyroid *g, sVec *in, sVec *out){
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

void oTorus(PTRS sTorus *torus, sVec *in, sVec *out){

    float3 p = in->v.xyz;
    p -= torus->center;

    p.y = length(p.xy) - torus->R;

    float d = length(p.yz) - torus->r;

    out->v.x = d;    

}


void oMax(PTRS void *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = max(in1->v.x, in2->v.x);

}

void oMin(PTRS void *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = min(in1->v.x, in2->v.x);

}

void oSubtract(PTRS void *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = max(in1->v.x, -in2->v.x);

}

// blend 
typedef struct {
    int size;
    int opcode;
    float width;
    float padding;
} sBlend;


void oBlendMin(PTRS sBlend *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = blendMin(in1->v.x, in2->v.x, ptr->width);

}

void oBlendMax(PTRS sBlend *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = blendMax(in1->v.x, in2->v.x, ptr->width);

}

void oBlendSubtract(PTRS sBlend *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = blendMax(in1->v.x, -in2->v.x, ptr->width);

}

// engrave 
typedef struct {
    int size;
    int opcode;
    float blendWidth;
    float depth;
} sEngrave;

// engrave shape1 with shape2 
void oEngrave(PTRS sEngrave *ptr, sVec *in1, sVec *in2, sVec *out){
    // bump mapping version 
    float eng = max(0.f,min(ptr->depth, -in2->v.x));
    //float eng = blendMax(0.,blendMin(ptr->depth, -in2->v.x,ptr->blendWidth ),ptr->blendWidth);
    //out->v.x = in1->v.x + eng;
    out->v.x = in1->v.x + ptr->depth*in2->v.x;
    /*
    // subtraction version 
    float d = in1->v.x;
    // sub surface layer of shape     
     d = max(d, -d - ptr->depth);
    // intersection of subsurface layer and engraver
    d = blendMax(d, in2->v.x, ptr->blendWidth);
    // subtraction of shape and intersected engraver 
    out->v.x = blendMax(in1->v.x, -d, ptr->blendWidth);
    */
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
    int size;  // size of struct in words 
    int opcode; // opcode to perform 
    // custom parameters of sScale
    float averageFactor; // inverse of average scale 
    float3 factor; // inverse of scale 
    float3 center; // center of scale 
} sScale;

void oScale(PTRS sScale *pS, sVec *inout){
    
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

void oRotation(PTRS sRotation *rot, sVec *inout){

    float3 vec = inout->v.xyz;
    vec -= rot->center;   
    vec = (float3){dot(rot->m0,vec),dot(rot->m1,vec),dot(rot->m2,vec)};
    vec += rot->center;    
    
    inout->v.xyz = vec;
}

//
// describes 2D grid in space 
// the shape is places inside of 3D box 
// each pixels in xy plane are extended in z-direction 
typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode 
    // custom parameters
    // coefficients to calculate data value
    float valueOffset; // value = byteValue*vFactor + vOffset;
    float valueFactor; 

    float rounding; // edges rounding      
    int tiling; // (tilesx | tilesy << 16)
    int nx; // grid count in x direction
    int ny; // grid count in y direction

    float3 center;  // center in world units

    float3 halfsize; // size in world units

    float3 origin; // location of bottom left corner
    float xscale; // world->girdx
    float yscale; // world->girdy

    int dataOffset; // location of data in the data buffer 
    PTRDATA char *pData; // actual grid data 
} sGrid2dByte;

void oGrid2dByte(PTRS sGrid2dByte *grid, sVec *pnt, sVec *out, Scene *pScene){
    // do box for now 
    float3 v = pnt->v.xyz;
    v -= grid->center;    
    v = fabs(v);
    v -= grid->halfsize;
    
    float d = blendMax(blendMax(v.x,v.y,grid->rounding),v.z,grid->rounding);
    if(d < 0.) { // inside of grid 
        // vector in grid units 
        float3 gpnt = (pnt->v.xyz - grid->origin) * (float3)(grid->xscale, grid->yscale,grid->yscale);
        int nx = grid->nx;
        int ny = grid->ny;

        gpnt.y = ny - gpnt.y;
        PTRDATA uchar *pData = (PTRDATA uchar*)(pScene->pgData + grid->dataOffset);        
        int ix = (int)gpnt.x;
        int iy = (int)gpnt.y;
        float x = gpnt.x - ix;
        float y = gpnt.y - iy;
        ix = clamp(ix, 0, nx-1);
        iy = clamp(iy, 0, ny-1);

        int ix1 = ix+1;
        int iy1 = iy+1;
        ix1 = clamp(ix1, 0, nx-1);
        iy1 = clamp(iy1, 0, ny-1);
        uchar v00 = pData[ix  + iy *  nx];
        uchar v10 = pData[ix1 + iy *  nx];
        uchar v11 = pData[ix1 + iy1 * nx];
        uchar v01 = pData[ix  + iy1 * nx];
        float value = v00 *(1-x)*(1-y) + v10*x*(1-y) + v01*(1-x)*y + v11*x*y;

        out->v.x = (grid->valueFactor*value + grid->valueOffset);

    } else {
        out->v.x = 0;
    }
    
}

//
// describes 2D grid in space 
// the shape is places inside of 3D box 
// each pixels in xy plane are extended in z-direction 
typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode 
    // custom parameters
    // coefficients to calculate data value
    float valueOffset; // value = byteValue*valueFactor + valueOffset;
    float valueFactor; 

    int nx; // grid count in x direction
    int ny; // grid count in y direction
    int nz; // grid count in y direction

    float3 center;  // center in world units

    float3 halfsize; // size in world units

    float3 origin; // location of bottom left corner
    
    float xscale; // pntGrid = (pnt-origin)*scale
    float outsideValue; // value to use outside of grid 
    int dataOffset; // location of data in the data buffer 
    PTRDATA char *pData; // actual grid data 
} sGrid3dByte;

void oGrid3dByte(PTRS sGrid3dByte *grid, sVec *pnt, sVec *out, Scene *pScene){

    float3 v = pnt->v.xyz;
    v -= grid->center;    
    v = fabs(v);
    v -= grid->halfsize;
    
    float d = max(max(v.x,v.y),v.z);
    if(d >= 0.) {
        // outside of grid 
        out->v.x = grid->outsideValue;        
        return;
    } 
    //else {
    //    out->v.x = d;
    //    return;
    //}

    // we are inside of grid 
    // gpnt in voxel units
    float3 gpnt = (pnt->v.xyz - grid->origin) * (float3)(grid->xscale,grid->xscale, grid->xscale);
    int nx = grid->nx;
    int ny = grid->ny;
    int nz = grid->nz;
    int nxy = nx*ny;
    
    gpnt.y = ny - gpnt.y;
    PTRDATA uchar *pData = (PTRDATA uchar*)(pScene->pgData + grid->dataOffset);        
    int ix = (int)gpnt.x;
    int iy = (int)gpnt.y;
    int iz = (int)gpnt.z;
    
    float x = gpnt.x - ix;
    float y = gpnt.y - iy;
    float z = gpnt.z - iz;
    ix = clamp(ix, 0, nx-1);

    iy = clamp(iy, 0, ny-1);
    
    int ix1 = ix+1;
    int iy1 = iy+1;
    int iz1 = iz+1;

    ix1 = clamp(ix1, 0, nx-1);
    iy1 = clamp(iy1, 0, ny-1);
    iz1 = clamp(iz1, 0, nz-1);
    float x1 = 1-x;
    float y1 = 1-y;
    float z1 = 1-z;

    uchar v000 = pData[ix  + iy *  nx + iz * nxy];
    uchar v100 = pData[ix1 + iy *  nx + iz * nxy];
    uchar v110 = pData[ix1 + iy1 * nx + iz * nxy];
    uchar v010 = pData[ix  + iy1 * nx + iz * nxy];
    uchar v001 = pData[ix  + iy *  nx + iz1 * nxy];
    uchar v101 = pData[ix1 + iy *  nx + iz1 * nxy];
    uchar v111 = pData[ix1 + iy1 * nx + iz1 * nxy];
    uchar v011 = pData[ix  + iy1 * nx + iz1 * nxy];
    
    float value = 
        ((v000*x1 + v100*x)*y1 + (v010*x1 + v110*x)*y)*z1 + 
        ((v001*x1 + v101*x)*y1 + (v011*x1 + v111*x)*y)*z;
    
    out->v.x = (grid->valueFactor*value + grid->valueOffset);
        
}


// union to "safely" convert pointers 
typedef union {
    PTRS void *pv;  
    PTRS int *w;
} CPtr;



/**
   opcode - stream of opcodes 
   opCount total count of opcodes 
   pnt - input point 
   result - output data value 
 */
void getShapeJSData(Scene *pScene, sVec *pnt, sVec *result) {
    
    CPtr ptr;  // pointer to opcodes struct to convert from int* to structs* 
    int offsetIn = 0;  // current offset in the opcodes 
    sVec stack[STACK_SIZE];
    int stackPos = 0; // current stack position
    
    //int popcode[MAXOPSIZE]; // private opcode, no benefits so far 

    PTROPS int *opcode = pScene->pOps; 
    int opCount = pScene->opCount;

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
        
        /*
        // copy data into local makes things slower 
        for(int k = 0; k < size/4; k++){
            popcode[k] = opcode[offsetIn+k];
        }
        int code = popcode[1];
        ptr.w = popcode;
        */

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

        case oGRID2DBYTE:
            
            oGrid2dByte(ptr.pv, &pnt1, &data1, pScene);
            break;

        case oGRID3DBYTE:
            
            oGrid3dByte(ptr.pv, &pnt1, &data1, pScene);
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
