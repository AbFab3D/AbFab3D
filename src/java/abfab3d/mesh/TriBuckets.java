 /*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.mesh;

import abfab3d.core.MathUtil;
import abfab3d.grid.ArrayInt;

import static java.lang.Math.round;
import static java.lang.Math.floor;

import static abfab3d.core.Output.time;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;

/**
   class puts triangles into buckets of given size and returns array of triangles in the neighbourhod of given point 
   
   @author Vladimir Bulatov
 */
public class TriBuckets {       

    static final boolean DEBUG = false;

    int nx, ny, nz;
    ArrayInt m_buckets[];
    
    double m_bucketSize; // size of the bucket 
    double m_bounds[];

    int returnedTriCount = 0;
    int maxReturnedTriCount = 0;
    int callCount = 0;


    public TriBuckets(double vert[], int faces[], double bucketSize){
        
        long t0 = time();
        m_bucketSize = bucketSize; 

        m_bounds = MathUtil.calculateBounds(vert);
        MathUtil.roundBounds(m_bounds, bucketSize);
        m_bounds = MathUtil.extendBounds(m_bounds, bucketSize);
        
        nx = (int)round((m_bounds[1] - m_bounds[0])/bucketSize);
        ny = (int)round((m_bounds[3] - m_bounds[2])/bucketSize);
        nz = (int)round((m_bounds[5] - m_bounds[4])/bucketSize);
        
        if(DEBUG){
                printf("target mesh bounds: [%7.2f %7.2f %7.2f %7.2f %7.2f %7.2f]MM \n", 
                       m_bounds[0]/MM,m_bounds[1]/MM,m_bounds[2]/MM,m_bounds[3]/MM,m_bounds[4]/MM,m_bounds[5]/MM);
                printf("buckets grid: [%d x %d x %d]\n", nx, ny, nz);
            }
        
        long size = (long)nx*ny*nz;
        if(size >= 0xFFFFFFFFL){
            throw new IllegalArgumentException(fmt("bucket array size: %d exceeds oxFFFFFFFF", size));
        }
        
        m_buckets = new ArrayInt[(int)size];
        
        int fcount = faces.length/3;

        for(int f = 0; f < fcount; f ++){

            int f3 = 3*f;
            int v0 = faces[f3];
            int v1 = faces[f3 + 1];
            int v2 = faces[f3 + 2];

            putTriToBuckets(vert, f, v0, v1, v2);
            
        }

        if(DEBUG){
            printf(" TriBuckets initialization time: %d ms\n", (time() - t0));
            printf(" target mesh tri count: %d\n", fcount);
        }
    }

    void printStat(){

        int minCount = m_buckets.length; 
        int maxCount = 0; 
        int totalCount = 0;
        int bucketCount = 0;
        for(int i = 0; i < m_buckets.length; i++){

            ArrayInt bucket = m_buckets[i];
            if(bucket == null)
                continue;
            bucketCount++;
            int s = bucket.size();
            totalCount += s;
            if(s < minCount)
                minCount = s;
            if(s > maxCount)
                maxCount = s;
            
        }
        printf("TriBuckets.printStat()\n");
        printf("  maxCount: %d\n", maxCount);
        printf("  minCount: %d\n", minCount);
        printf("  triangles in buckets: %d\n", totalCount);
        printf("  filled buckets: %d (%5.1f%%) \n", bucketCount, (100.*bucketCount/ m_buckets.length));

        if(bucketCount != 0)
            printf("  average tri per bucket: %5.1f\n", ((double)totalCount/bucketCount));
        
        printf("  call count: %d \n", callCount);
        printf("  maxReturnedTriCount: %d \n", maxReturnedTriCount);
        printf("  averageReturnedTriCount: %5.1f \n", ((double)returnedTriCount/callCount));
        

    }

    /**
       fills aray with triangles from buckets which intersect box with center (x,y,z) and size 2*bucketSize
     */
    public void getTriangles(double x, double y, double z, ArrayInt triangles){

        callCount++;

        triangles.clear();
        int 
            xmin = (int)floor((x - m_bounds[0])/m_bucketSize)-1,
            ymin = (int)floor((y - m_bounds[2])/m_bucketSize)-1,
            zmin = (int)floor((z - m_bounds[4])/m_bucketSize)-1,
            xmax = xmin + 2,
            ymax = ymin + 2,
            zmax = zmin + 2;
        if(xmax < 0)return;
        if(ymax < 0)return;
        if(zmax < 0)return;
        if(xmin >= nx)return;
        if(ymin >= ny)return;
        if(zmin >= nz)return;


        if(xmin < 0) xmin = 0;
        if(ymin < 0) ymin = 0;
        if(zmin < 0) zmin = 0;

        if(xmax >= nx) xmax = nx-1;
        if(ymax >= ny) ymax = ny-1;
        if(zmax >= nz) zmax = nz-1;
        
        
        for(int ix = xmin; ix <= xmax; ix++){
            for(int iy = ymin; iy <= ymax; iy++){
                for(int iz = zmin; iz <= zmax; iz++){
                    ArrayInt bucket = m_buckets[ix + iy*nx + iz*nx*ny];
                    if(bucket != null){
                        // TODO optimization we don't want duplicated triangles here 
                        triangles.add(bucket);                        
                    }
                }
            }
        }    

        if(false)
            printf("returnedTriCount: %d\n", triangles.size());

        triangles.sortAndRemoveDuplicates();

        returnedTriCount += triangles.size();
        if(triangles.size() > maxReturnedTriCount)
            maxReturnedTriCount = triangles.size();
    }

    /**
       find bounds of the triangle and stores triIndex in each bucket which intersects the bounds 
     */
    int triBounds[] = new int[6];
    protected void putTriToBuckets(double vert[], int triIndex, int v0, int v1, int v2){
        
        getTriBounds(vert, v0, v1, v2, triBounds);
        if(false)
            printf("triBounds: [%d %d %d %d %d %d]\n", triBounds[0],triBounds[1],triBounds[2],triBounds[3],triBounds[4],triBounds[5]);

        for(int x = triBounds[0]; x <= triBounds[1]; x++){
            for(int y = triBounds[2]; y <= triBounds[3]; y++){
                for(int z = triBounds[4]; z <= triBounds[5]; z++){
                    addTriToBucket(x,y,z,triIndex);
                }
            }
        }                
    }    

    protected void addTriToBucket(int x, int y, int z, int triIndex){
        
        int ind = x + y*nx + z*nx*ny;
        ArrayInt bucket = m_buckets[ind];
        if(bucket == null){
            bucket = new ArrayInt(1);
            m_buckets[ind] = bucket;
        }
        bucket.add(triIndex);
        
    }
    
    
    protected void getTriBounds(double vert[], int v0, int v1, int v2, int bounds[] ){

        bounds[0] = nx;
        bounds[1] = 0;
        bounds[2] = ny;
        bounds[3] = 0;
        bounds[4] = nz;
        bounds[5] = 0;

        getVertBounds(vert, v0, bounds);
        getVertBounds(vert, v1, bounds);
        getVertBounds(vert, v2, bounds);
    }


    protected void getVertBounds(double vert[], int v, int triBounds[]){
        
        int v3 = 3*v;
        int 
            x = (int)floor((vert[v3] - m_bounds[0])/m_bucketSize),
            y = (int)floor((vert[v3+1] - m_bounds[2])/m_bucketSize),
            z = (int)floor((vert[v3+2] - m_bounds[4])/m_bucketSize);
        
        if(x < triBounds[0]) triBounds[0] = x;
        if(x > triBounds[1]) triBounds[1] = x;
        if(y < triBounds[2]) triBounds[2] = y;
        if(y > triBounds[3]) triBounds[3] = y;
        if(z < triBounds[4]) triBounds[4] = z;
        if(z > triBounds[5]) triBounds[5] = z;
            

    }
    
}
