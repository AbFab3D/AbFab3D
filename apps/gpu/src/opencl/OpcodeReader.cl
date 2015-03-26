// size of struct in words 
#define WSIZE(A) (sizeof(A)/sizeof(int))
#define iNAN (0xFFFF)

// stack size for intermediate memory 
// stack >= 4000 generates CL_OUT_OF_HOST_MEMORY error 
#define STACK_SIZE  10
// maximal size of struct for single operation (in words) should be the size of largest operation 
//#define MAXOPSIZE 30 

#include "opcodes.h"
#include "structs.cl"
#include "functions.cl"

#include "boolean.cl"
#include "shapes.cl"
#include "volumepatterns.cl"
#include "transforms.cl"
#include "grid2dbyte.cl"
#include "grid3dbyte.cl"
#include "image3d.cl"
#include "noise3d.cl"
#include "ShapeJSData.cl"


// OpenCL Kernel Function for opcode reading 
kernel void opcodeReader(
                         global const int * pgOps, // operations 
                         int opCount, // operations count 
                         int opBufferSize, // operations buffer size 
                         local int *plOps, // ops in local memory 
                         global const char *pgData, // large global data
                         global int *result
                         ) {
    // maker opcode data local 
    copyToLocal(pgOps, plOps,opBufferSize);
    
    // init the scene from kernel params 
    Scene scene = (Scene){//.worldScale=worldScale, 
                          //.worldCenter=(float3)(worldCenterX,worldCenterY,worldCenterZ),
                          .pOps=plOps, 
                          .opCount=opCount, 
                          .pgData=pgData
                          }; 
    
    sVec pnt = (sVec){.v =(float4)(1,1,0,0), .scale = 1}; 
    sVec out;

    getShapeJSData(&scene, &pnt, &out);

}
