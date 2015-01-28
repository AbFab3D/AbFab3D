typedef struct {
   size_t size;
   float radius;
   float3 center;
} CSphere;
   
typedef struct{
   float4 v;
} CVec;

typedef struct {
   size_t size;
   float4 t;
} CTranslation;


typedef union {
     global CSphere *pSphere;
     global uchar *c;
 } CPtr;

// OpenCL Kernel Function for byte code reading 
kernel void ByteReader(global uchar* opcode, int dataCount, global uchar* outdata, int outCount) {
		
		CPtr p;
		p.c = opcode;
		global CSphere *pS = p.pSphere;
		
		// get index into global data array
		int iGID = get_global_id(0);
		if (iGID >= outCount)  {
			return;
		}
		outdata[iGID] = (uchar)(pS->size);
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
