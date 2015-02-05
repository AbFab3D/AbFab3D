// size of struct in words 
#define WSIZE(A) (sizeof(A)/sizeof(int))

#define oSPHERE 1001
#define oGYROID 1002


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

typedef struct {
    int size;
    int opcode;
    float level;
    float thickness;
    float period;
    float3 offset;
} sGyroid;


void oSphere(sSphere *p, sVec *in, sVec *out){
    out->v.x = p->radius;
}

void oGyroid(sGyroid *p, sVec *in, sVec *out){
    out->v.x = p->offset.z;    
}


// union to "safely" convert pointers 
typedef union {
    global void *pv;  
    global int *w;
} CPtr;


void copyOpcodes(global int *opcode, global int *outdata){

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
kernel void OpcodeReader(global const int* opcode, int dataCount, global int* outdata, int outCount, global int *results) {
		
    int iGID = get_global_id(0);
    if(iGID > 0) { 
        return;
    }
    
    // for debugging 
    copyOpcodes(opcode, outdata);
    
    CPtr ptr;
    int count = 0;
    int offsetIn = 0;
    sVec pnt;
    sVec data;
    pnt.v.x = 1.f;
    int resOffset = 0;

    while(count++ < 10) {

        int size = opcode[offsetIn];
        if(size == 0)
            break;
        int code = opcode[offsetIn+1];
        ptr.w = (opcode + offsetIn);
        switch(code){
        case oSPHERE:
            {
                global sSphere *pS = ptr.pv;
                sSphere sphere = *pS;
                oSphere(&sphere, &pnt, &data);
                results[resOffset++] = as_int(data.v.x);
            }
            break;
        case oGYROID:
            {
                global sGyroid *pS = ptr.pv;
                sGyroid gyroid = *pS;
                oGyroid(&gyroid, &pnt, &data);
                results[resOffset++] = as_int(data.v.x);
            }
            break;
        }        
        offsetIn += size;
    }
    results[resOffset++] = 0;
    
}
