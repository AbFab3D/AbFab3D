
#define WSIZE(A) (sizeof(A)/sizeof(int))

typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode to perform 
    // custom patrameters of DataSource 
    float radius;  // sphere radius (positive radius - interior of sphere, negative - exterior) 
    float3 center; // center   
} CSphere;

typedef struct {
    int size;
    int opcode;
    float v1;
    float v2;
    float3 pnt;
} CTest1;


typedef struct {
    int size;
    float8 f8;
} CTest2;

typedef struct {
    float v[8];
} CTest3;
   
typedef struct{
    float4 v;
} CVec;

typedef struct {
    size_t size;
    float4 t;
} CTranslation;


// union to "safely" convert pointers 
typedef union {
    void *pv;  
    int *w;
} CPtr;



// OpenCL Kernel Function for byte code reading 
kernel void WordWriter(global int* opcode, int dataCount, global int* outdata, int outCount) {
		
    //CPtr p;
    //p.c = opcode;

    //CSphere sphere = (CSphere){.size=WSIZE(CSphere),.radius=-1.1f,.center=(float3)(2.11f,2.12f, 2.13f)};
    CTest2 test = (CTest2){.size=WSIZE(CTest2),.f8=(float8)(1.11f,1.12f,1.13f,1.14f,1.15f,1.16f,1.17f,1.18f)};
    //CTest3 test3 = (CTest3){.v={1.1f,2.2f,3.3f,4.4f,5.5f,6.6f,7.7f,8.8f}};
    //CTest1 test1 = (CTest1){.size=WSIZE(CTest1),.v1=1.11f,.v2=2.11, .pnt = (float3)(2.21f,2.22f,2.23f)};
    //CSphere *pT = &sphere;
    //CTest3 *pT = &test3;
    //CTest1 *pT = &test1;
    //CTest1 *pT = &test2;
    
    // get index into global data array
    int iGID = get_global_id(0);

    if(iGID > 10) 
        return;
    
    // size in int words 
    int size = test.size;

    CPtr p;
    p.pv = &test;

    int *pW = p.w;

    int offset = iGID*(size+1);
    // each kernel writres into it's own memory 
    global int *writebuf = (outdata + offset);
    *(writebuf) = size; 
    writebuf++;
    int cnt = 0;
    while(cnt < size) {
        *(writebuf+cnt) = *(pW+cnt); 
        cnt++;
    }
}



// OpenCL Kernel Function for byte code reading 
kernel void ByteReader(global uchar* opcode, int dataCount, global float* outdata, int outCount) {
    /*
    CPtr p;
    p.c = opcode;
    global CSphere *pS = p.pSphere;
		
    // get index into global data array
    int iGID = get_global_id(0);
    if (iGID >= outCount)  {
        return;
    }
    int c = iGID % 5;
    switch(c){
    default: break;
    case 0: 
        outdata[0] = (float)(pS->size); break;
        outdata[1] = pS->radius; break;
        outdata[2] = pS->center.x; break;
        outdata[3] = pS->center.y; break;
        outdata[4] = pS->center.z; break;
    }	
			
    //outdata[iGID] = (uchar)(c);
    */
}


// OpenCL Kernel Function for element by element vector addition
kernel void VectorAdd(global const float* a, global const float* b, global float* c, int numElements) {

    // get index into global data array
    int iGID = get_global_id(0);

    // bound check (equivalent to the limit on a 'for' loop for standard/serial C code
    if (iGID >= numElements)  {
        return;
    }

    // add the vector elements
    c[iGID] = a[iGID] + b[iGID];
}
