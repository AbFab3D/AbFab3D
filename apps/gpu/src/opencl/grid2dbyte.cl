//
// describes 2D grid in space 
// the shape is places inside of 3D box 
// each pixels in xy plane are extended in z-direction 
typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode 
    // custom parameters
    // coefficients to calculate data value
    float vOffset; // value = byteValue*vFactor + vOffset;
    float vFactor; 

    float rounding; // edges rounding      
    int options; // (repeatX | tilesy << 1)
    int nx; // grid count in x direction
    int ny; // grid count in y direction

    float3 center;  // center in world units

    float3 halfsize; // size in world units

    float3 origin; // location of bottom left corner
    float xscale; // world->girdx
    float yscale; // world->girdy
    float outsideValue; 
    int dataOffset; // location of data in the data buffer 
} sGrid2dByte;

void oGrid2dByte(PTRS sGrid2dByte *grid, sVec *pnt, sVec *out, Scene *pScene){

    float3 v = pnt->v.xyz;
    //v -= grid->origin;

    // TODO - repeatX and repeatY
    //int repeatX = (grid->options & 1);
    //int repeatY = ((grid->options >> 1) & 1);    
    //if(repeatX) v.x = fmod(v.x, 2*grid->halfsize.x);
    //if(repeatY) v.y = fmod(v.y, 2*grid->halfsize.y);
    
    v -= grid->center;
    v = fabs(v);
    v -= grid->halfsize;
    
    float d = max(max(v.x,v.y),v.z);
    if(d < 0) { // inside of grid 
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

        out->v.x = (grid->vFactor*value + grid->vOffset);
        //out->v.x = (grid->vFactor*255 + grid->vOffset);

    } else {
        //out->v.x = (grid->vFactor*0 + grid->vOffset);
        out->v.x = grid->outsideValue;
    }
    
}
