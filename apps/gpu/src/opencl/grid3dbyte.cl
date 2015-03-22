//
// describes 3D grid in space 
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
