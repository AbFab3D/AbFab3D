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

/**
   calculates data source values for block of grid with given x,y,z offset and size
   takes 
    
*/
kernel void GridCalculator(
                           float voxelSize, 
                           float gridXmin, // grid origin
                           float gridYmin, 
                           float gridZmin, 
                           int offsetX,  // grid block origin 
                           int offsetY,
                           int offsetZ,
                           int blockSizeX,    // size of grid block  
                           int blockSizeY,
                           int blockSizeZ,
                           
                           global const int * pgOps, // operations 
                           int opCount, // operations count 
                           int opBufferSize, // operations buffer size 
                           local int *plOps, // ops in local memory 
                           global const char *pgData, // large global data
                           global float *outGrid // output grid data 
                           ) {
    uint ix = get_global_id(0);
    uint iy = get_global_id(1);
    if(ix >= blockSizeX) return;
    if(iy >= blockSizeY) return;

    // make opcode data local 
    copyToLocal(pgOps, plOps,opBufferSize);
    
    // init the scene from kernel params 
    Scene scene = (Scene){//.worldScale=worldScale, 
                          //.worldCenter=(float3)(worldCenterX,worldCenterY,worldCenterZ),
                          .pOps=plOps, 
                          .opCount=opCount, 
                          .pgData=pgData
                          }; 

    float x = (ix + offsetX + 0.5f)*voxelSize + gridXmin;
    float y = (iy + offsetY + 0.5f)*voxelSize + gridYmin;

    sVec pnt = (sVec){.v =(float4)(x,y,0,0), .scale = 1}; 
    sVec out;

    int iz0 = (ix + iy*blockSizeX)*blockSizeZ;

    for(int iz = 0; iz < blockSizeZ; iz++){
        float z = (iz + offsetZ + 0.5f)*voxelSize + gridZmin;
        pnt.v = (float4)(x,y,z,0);
        pnt.scale = 1;
        
        getShapeJSData(&scene, &pnt, &out);
        outGrid[iz0 + iz] = step10(out.v.x, 0, voxelSize);
        // store data into output buffer 
    }
}
