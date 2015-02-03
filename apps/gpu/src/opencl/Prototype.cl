typedef struct {
    float3 center;
    float3 offset;
    float3 dist;
    float test;
    float radius;
} CSphere;

typedef struct {
    float v1;
    float v2;
    float v3;
    float v4;
    float v5;
    float v6;
} CTest1;

typedef struct {
    float3 v1;
    float3 v2;
} CTest2;
   
typedef struct{
    float4 v;
} CVec;

typedef struct {
    size_t size;
    float4 t;
} CTranslation;


typedef union {
    CSphere *pSphere;
    CTest2 *pTest2;
    CTest2 *pTest1;
    uchar *c;
    int *w;
} CPtr;

// OpenCL Kernel Function for byte code reading 
kernel void WordWriter(global int* opcode, int dataCount, global int* outdata, int outCount) {
		
    //CPtr p;
    //p.c = opcode;

    //CSphere sphere = (CSphere){.radius=1.11f, .center=(float3)(2.21f,2.22f, 2.23f), .test=3.33f, .offset = (float3)(4.41f,4.42f,4.43f), .dist = (float3)(5.51f,5.52f,5.53f)};

    //CTest2 test2 = (CTest2){.v1=(float3)(1.11f,1.12f, 1.13f), .v2=(float3)(2.11f,2.12f, 2.13f), .v3=(float3)(3.11f,3.12f, 3.13f)};
    //CTest2 test2 = (CTest2){.v1=(float3)(1.11f,1.12f, 1.13f), .v2=(float3)(2.11f,2.12f, 2.13f)};
    CTest1 test1 = (CTest1){.v1=1.11f,.v2=1.12f,.v3=1.13f,.v4=1.14f,.v5=1.15f,.v6=1.16f};
    //CSphere *pS = &sphere;
    //CTest2 *pT = &test2;
    CTest1 *pT = &test1;
    
    // get index into global data array
    int iGID = get_global_id(0);


    if(iGID > 10) 
        return;
    
    int size = sizeof(*pT)/sizeof(int);

    CPtr p;
    p.pTest1 = pT;

    int *pW = p.w;

    int offset = iGID*(size+1);
    global int *writebuf = (outdata + offset);
    int cnt = 0;
    *(writebuf+cnt) = size; 
    while(cnt++ < size) {
        *(writebuf+cnt) = *(pW+cnt); 
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
