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
    float outsideValue; 
    int dataOffset; // location of data in the data buffer 
} sImage3D;

void oImage3D(PTRS sImage3D *img, sVec *pnt, sVec *out, Scene *pScene){

    float3 v = pnt->v.xyz;
    v -= img->center;    
    v = fabs(v);
    v -= img->halfsize;
    
    float d = blendMax(blendMax(v.x,v.y,img->rounding),v.z,img->rounding);
    if(d < 0.) { // inside of box 
        int nx = img->nx;
        int ny = img->ny;

        // vector in grid units 
        float3 gpnt = (pnt->v.xyz - img->origin);

        gpnt.xy *= (float2)(img->xscale, img->yscale);
        
        int imagePlace = (((img->tiling)>>16) & 3); // 0 - top, 1 - bottom, 2 - both 

        int tileX = (img->tiling)& 0xFF;
        int tileY = ((img->tiling) >> 8)& 0xFF; 
        if(tileX > 1) gpnt.x = fmod(gpnt.x*tileX, (float)nx);
        if(tileY > 1) gpnt.y = fmod(gpnt.y*tileY, (float)ny);
        
        gpnt.y = ny - gpnt.y; // flip y-axis shall be here? 
        //PTRDATA uchar *pData = (PTRDATA uchar*)(pScene->pgData + img->dataOffset);        
        PTRDATA ushort *pData = (PTRDATA ushort*)(pScene->pgData + img->dataOffset);        
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
        ushort v00 = pData[ix  + iy *  nx];
        ushort v10 = pData[ix1 + iy *  nx];
        ushort v11 = pData[ix1 + iy1 * nx];
        ushort v01 = pData[ix  + iy1 * nx];
        float value = v00 *(1-x)*(1-y) + v10*x*(1-y) + v01*(1-x)*y + v11*x*y;

        d =  (img->valueFactor*value + img->valueOffset);

        if(imagePlace == 0) {
            out->v.x = gpnt.z - d;
        } else if(imagePlace == 1) {
            out->v.x = (2*(img->halfsize.z) - gpnt.z) - d;
        } else if(imagePlace == 2){// both sides
            out->v.x = fabs(gpnt.z - img->halfsize.z) - d;
        }
        
    } else {
        out->v.x = d;
    }
    
} // oImage3D
