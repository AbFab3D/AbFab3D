/**
 * Make grid kernel
 */
kernel void makeGrid(private float voxelX, private float voxelY, private float voxelZ,
    private float offsetX, private float offsetY, private float offsetZ, private int sliceSize, 
    private int width, private int height, private int depth, global uchar* dest) {

    int x = get_global_id(0);
    int y = get_global_id(1);
    int z = get_global_id(2);
    
    if (x >= width || y >= height || z >= depth) {
        // Handle outside ranges from work item rounding
        return;
    } else if (x < 1 || y < 1 || z < 1) {
        dest[y*sliceSize + x * depth + z] = 0;
        return;
    } else if (x > width - 2 || y > height -2 || z > (depth -2) ) {
        dest[y*sliceSize + x * depth + z] = 0;
        return;
    }

    float3 voxel = (float3)(voxelX,voxelY,voxelZ);
    float3 offset = (float3) (offsetX,offsetY,offsetZ);

    // gyroid params
    float factor = 2 * 3.14159265 / 0.01;
    float vs = 0.0001;
    float thickness = 0.001;
    float level = 0;
    float voxelScale = 1;

    // sphere params
    float radius = 0.02;

    float3 pos = (float3) ((float)x,(float)y,(float)z);

    float3 worldPnt = fma(pos,voxel,offset);


    // Gyroid subtracted from a Sphere
    float data1 = gyroid(vs,voxelScale,level,factor,thickness, offset,worldPnt);
    float data2a = sphere(vs, radius, 0, 0, 0, true, worldPnt);
    float data2b = sphere(vs, radius, radius*2, 0, 0, true, worldPnt);

    // Intersection op
    float data3a = subtraction(data2a,data1);
    float data3b = subtraction(data2b,data1);
    
    float data4 = unionop(data3a,data3b);
    int v = (int) (255.0 * data4 + 0.5);

/*
    float data = gyroid(vs,voxelScale,level,factor,thickness, offset,worldPnt);

    int v = (int) (255.0 * data + 0.5);
*/

/*
    if (x == 512 && z == 512) {
       printf("gpu voxel: %f %f %f\n", voxelX, voxelY, voxelZ);
       printf("%3d %3d %3d   wp: %4.3f %4.3f %4.3f  --> %4.5f  --> %d\n", x,y,z,worldPnt.x, worldPnt.y, worldPnt.z,data3,v);
    }
*/
/*
    if ((y*sliceSize + x * depth + z) >= 100*100*100) {
    	printf("Over limit at: %d %d %d\n",x,y,z);
    }
*/    
    dest[y*sliceSize + x * depth + z] = (uchar) v;

//    printf("%d %d %d\n", get_global_id(0),get_global_id(1),get_global_id(2));
//    printf("%d %d %d %f\n", get_global_id(0),get_global_id(1),get_global_id(2),v);
}


