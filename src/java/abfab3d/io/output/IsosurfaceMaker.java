/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.io.output;

import java.util.Vector;
import java.util.Arrays;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Grid;
import abfab3d.util.TriangleCollector;

import static java.lang.Math.abs;
import static abfab3d.util.Output.printf;


/**
   generates isosurface from data defined as a function

   @author Vladimir Bulatov

   used fragments of code by Paul Bourke

*/
public class IsosurfaceMaker {

    static final int edgeTable[] = IsosurfaceTables.edgeTable;
    static final int triTable[][] = IsosurfaceTables.triTable;
    // value to offset from 0.
    static final double TINY_VALUE = 1.e-4;


    public static final int CUBES = 0; 
    public static final int TETRAHEDRA = 1; 


    int m_algorithm = CUBES;

    AttributeGrid m_grid;

    
    double m_isoValue = 0.; // value of isosurface 
    double m_bounds[] = new double[]{-1, 1, -1, 1, -1, 1}; // bounds of the area 
    int m_nx=10, m_ny=10, m_nz=10;

    SliceFunction m_sfunction = null;
    TriangleCollector m_tcollector = null;

    /**
       set bounds of area where isosurface is made
     */
    public void setBounds(double bounds[]){

        m_bounds = bounds.clone();

    }

    /**
       set grid size to calculate isosirface on 
       the area is divide into [(nx-1) x (ny-1) x (nz-1)] cubes 
       data is calculated in the corners of the cubes 
     */
    public void setGridSize(int nx, int ny, int nz ){

        m_nx = nx;
        m_ny = ny;
        m_nz = nz;

    }
    
    /**
       set value of isosurface
     */
    public void setIsovalue(double isoValue){
        m_isoValue = isoValue; 
    }

    /**

       set algorithm used for isosurface extraction 
       
       possible values: CUBES, TETRAHDRA
       
     */
    public void setAlgorithm(int algorithm){
        m_algorithm = algorithm;
    }

    /**
       generates isosurface from given @sfunction and passes triangles to @tcollector
       
       slice calculator is called sequentially from zmin to zmax and is expected to fill 
       the slice data 

       triangles are passed to triangle collector 
       
     */
    public void makeIsosurface(SliceCalculator scalculator, TriangleCollector tcollector){
        double xmin = m_bounds[0];        double xmax = m_bounds[1];
        double ymin = m_bounds[2];
        double ymax = m_bounds[3];
        double zmin = m_bounds[4];
        double zmax = m_bounds[5];

        int nx1 = m_nx-1;
        int ny1 = m_ny-1;
        int nz1 = m_nz-1;

        double dx = (xmax - xmin)/nx1;
        double dy = (ymax - ymin)/ny1;
        double dz = (zmax - zmin)/nz1;
        
        //printf("x0: %12.10f %12.10f %12.10f\n",xmin,ymin, zmin);
        //printf("dx: %12.10f %12.10f %12.10f \n", dx, dy, dz);
        //printf("bounds: %12.10f %12.10f %12.10f %12.10f %12.10f %12.10f \n", m_bounds[0],m_bounds[1],m_bounds[2],m_bounds[3],m_bounds[4],m_bounds[5]);

        SliceData slice0 = new SliceData(m_nx, m_ny, xmin, xmax, ymin, ymax);
        SliceData slice1 = new SliceData(m_nx, m_ny, xmin, xmax, ymin, ymax);

        Vector3d triangles[] = new Vector3d[15]; // max number of triagles is 5         
        Cell cell = new Cell();

        slice0.setZ(zmin);
        scalculator.getSlice(slice0); 

        for(int iz = 0; iz < nz1; iz++) {
            double z = zmin + dz * iz;
            double z1 = z+dz;

            slice1.setZ(z1);
            scalculator.getSlice(slice1); 
            
            for(int iy = 0; iy < ny1; iy++) {

                double y = ymin + dy*iy;
                int iy1 = iy+1;
                double y1 = y+dy;
                
                for(int ix = 0; ix < nx1; ix++) {

                    int ix1 = ix+1;
                    double x = xmin + dx*ix;
                    double x1 = x+dx;

                    cell.p[0].set(x, y, z );
                    cell.p[1].set(x1,y, z );
                    cell.p[2].set(x1,y, z1);
                    cell.p[3].set(x, y, z1);
                    cell.p[4].set(x, y1,z );
                    cell.p[5].set(x1,y1,z );
                    cell.p[6].set(x1,y1,z1);
                    cell.p[7].set(x, y1,z1);

                    int base = ix  + iy * m_nx; // offset of point (x,y)
                    int base1 = base + m_nx;     // offset of point (x, y+1)

                    cell.val[0] = slice0.data[base] - m_isoValue;
                    cell.val[1] = slice0.data[base + 1] - m_isoValue;
                    cell.val[2] = slice1.data[base + 1] - m_isoValue;
                    cell.val[3] = slice1.data[base] - m_isoValue;
                    cell.val[4] = slice0.data[base1] - m_isoValue;
                    cell.val[5] = slice0.data[base1 + 1] - m_isoValue;
                    cell.val[6] = slice1.data[base1 + 1] - m_isoValue;
                    cell.val[7] = slice1.data[base1] - m_isoValue;
                    shiftFromZero(cell.val);

                    switch(m_algorithm){
                    default:
                    case CUBES:
                        polygonizeCubeNew(cell, triangles, tcollector);
                        break;
                    case TETRAHEDRA:
                        polygonizeCube_tetra(cell, 0., triangles, tcollector);
                        break;
                    }                    
                }// ix
            } // iy 

            // switch calculated slices
            SliceData stmp = slice0;
            slice0 = slice1;
            slice1 = stmp;            

        }  // for(iz...           
    }

    static final double ISOEPS = 1.e-2;

    /**
       shifts values close to zeto to EPS distance above zero 
     */
    public static void shiftFromZero(double v[]){

        for(int i = 0; i < v.length; i++){
            if(abs(v[i]) < ISOEPS){
                v[i] = ISOEPS; 
            }
        }        
    }
    
    public static void polygonizeCube(Cell g, Vector3d triangles[], TriangleCollector ggen){

        int cubeindex = 0;
        double iso = 0.0;

        if (g.val[0] < 0) cubeindex |= 1;
        if (g.val[1] < 0) cubeindex |= 2;
        if (g.val[2] < 0) cubeindex |= 4;
        if (g.val[3] < 0) cubeindex |= 8;
        if (g.val[4] < 0) cubeindex |= 16;
        if (g.val[5] < 0) cubeindex |= 32;
        if (g.val[6] < 0) cubeindex |= 64;
        if (g.val[7] < 0) cubeindex |= 128;

        /* Cube is entirely in/out of the surface */
        if (edgeTable[cubeindex] == 0)
            return;

        /* Find the vertices where the surface intersects the cube */
        if ((edgeTable[cubeindex] & 1)  != 0)
            g.e[0] = vertexInterp(iso,g.p[0],g.p[1],g.val[0],g.val[1]);

        if ((edgeTable[cubeindex] & 2) != 0)
            g.e[1] = vertexInterp(iso,g.p[1],g.p[2],g.val[1],g.val[2]);

        if ((edgeTable[cubeindex] & 4) != 0)
            g.e[2] = vertexInterp(iso,g.p[2],g.p[3],g.val[2],g.val[3]);

        if ((edgeTable[cubeindex] & 8) != 0)
            g.e[3] = vertexInterp(iso,g.p[3],g.p[0],g.val[3],g.val[0]);

        if ((edgeTable[cubeindex] & 16) != 0)
            g.e[4] = vertexInterp(iso,g.p[4],g.p[5],g.val[4],g.val[5]);

        if ((edgeTable[cubeindex] & 32) != 0)
            g.e[5] = vertexInterp(iso,g.p[5],g.p[6],g.val[5],g.val[6]);

        if ((edgeTable[cubeindex] & 64) != 0)
            g.e[6] = vertexInterp(iso,g.p[6],g.p[7],g.val[6],g.val[7]);

        if ((edgeTable[cubeindex] & 128) != 0)
            g.e[7] = vertexInterp(iso,g.p[7],g.p[4],g.val[7],g.val[4]);

        if ((edgeTable[cubeindex] & 256) != 0)
            g.e[8] = vertexInterp(iso,g.p[0],g.p[4],g.val[0],g.val[4]);
        
        if ((edgeTable[cubeindex] & 512) != 0)
            g.e[9] = vertexInterp(iso,g.p[1],g.p[5],g.val[1],g.val[5]);

        if ((edgeTable[cubeindex] & 1024) != 0)
            g.e[10] = vertexInterp(iso,g.p[2],g.p[6],g.val[2],g.val[6]);

        if ((edgeTable[cubeindex] & 2048) != 0)
            g.e[11] = vertexInterp(iso,g.p[3],g.p[7],g.val[3],g.val[7]);
        
        /* Create the triangles */
        int ntriang = 0;
        for (int i=0; i < triTable[cubeindex].length; i+=3) {
            
            triangles[i]   = g.e[triTable[cubeindex][i  ]];
            triangles[i+1] = g.e[triTable[cubeindex][i+1]];
            triangles[i+2] = g.e[triTable[cubeindex][i+2]];
            ntriang++;
        }

        addTri(ggen, triangles, ntriang);        
        
    }

    public static void polygonizeCubeNew(Cell g, Vector3d triangles[], TriangleCollector ggen){

        int cubeindex = 0;
        double iso = 0.0;

        if (g.val[0] < 0) cubeindex |= 1;
        if (g.val[1] < 0) cubeindex |= 2;
        if (g.val[2] < 0) cubeindex |= 4;
        if (g.val[3] < 0) cubeindex |= 8;
        if (g.val[4] < 0) cubeindex |= 16;
        if (g.val[5] < 0) cubeindex |= 32;
        if (g.val[6] < 0) cubeindex |= 64;
        if (g.val[7] < 0) cubeindex |= 128;

        /* Cube is entirely in/out of the surface */
        if (edgeTable[cubeindex] == 0)
            return;

        /* Find the vertices where the surface intersects the cube */
        if ((edgeTable[cubeindex] & 1)  != 0)
            vertexInterp(iso,g.p[0],g.p[1],g.val[0],g.val[1],g.e[0]);

        if ((edgeTable[cubeindex] & 2) != 0)
            vertexInterp(iso,g.p[1],g.p[2],g.val[1],g.val[2],g.e[1]);

        if ((edgeTable[cubeindex] & 4) != 0)
            vertexInterp(iso,g.p[2],g.p[3],g.val[2],g.val[3],g.e[2]);

        if ((edgeTable[cubeindex] & 8) != 0)
            vertexInterp(iso,g.p[3],g.p[0],g.val[3],g.val[0],g.e[3]);

        if ((edgeTable[cubeindex] & 16) != 0)
            vertexInterp(iso,g.p[4],g.p[5],g.val[4],g.val[5],g.e[4]);

        if ((edgeTable[cubeindex] & 32) != 0)
            vertexInterp(iso,g.p[5],g.p[6],g.val[5],g.val[6],g.e[5]);

        if ((edgeTable[cubeindex] & 64) != 0)
            vertexInterp(iso,g.p[6],g.p[7],g.val[6],g.val[7],g.e[6]);

        if ((edgeTable[cubeindex] & 128) != 0)
            vertexInterp(iso,g.p[7],g.p[4],g.val[7],g.val[4],g.e[7]);

        if ((edgeTable[cubeindex] & 256) != 0)
            vertexInterp(iso,g.p[0],g.p[4],g.val[0],g.val[4],g.e[8]);

        if ((edgeTable[cubeindex] & 512) != 0)
            vertexInterp(iso,g.p[1],g.p[5],g.val[1],g.val[5],g.e[9]);

        if ((edgeTable[cubeindex] & 1024) != 0)
            vertexInterp(iso,g.p[2],g.p[6],g.val[2],g.val[6],g.e[10]);

        if ((edgeTable[cubeindex] & 2048) != 0)
            vertexInterp(iso,g.p[3],g.p[7],g.val[3],g.val[7],g.e[11]);

        /* Create the triangles */
        int ntriang = 0;
        for (int i=0; i < triTable[cubeindex].length; i+=3) {

            triangles[i]   = g.e[triTable[cubeindex][i  ]];
            triangles[i+1] = g.e[triTable[cubeindex][i+1]];
            triangles[i+2] = g.e[triTable[cubeindex][i+2]];
            ntriang++;
        }

        addTri(ggen, triangles, ntriang);

    }

    /*
      Polygonise a tetrahedron given its vertices within a cube
      This is an alternative algorithm to polygonisegrid.
      It results in a smoother surface but more triangular facets.
      
                      + 0
                     /|\
                    / | \
                   /  |  \
                  /   |   \
                 /    |    \
                /     |     \
               +-------------+ 1
              3 \     |     /
                 \    |    /
                  \   |   /
                   \  |  /
                    \ | /
                     \|/
                      + 2

     It can be used for polygonization of cube: 
   
      polygoniseTetra(grid,iso,triangles,0,2,3,7);
      polygoniseTetra(grid,iso,triangles,0,2,6,7);
      polygoniseTetra(grid,iso,triangles,0,4,6,7);
      polygoniseTetra(grid,iso,triangles,0,6,1,2);
      polygoniseTetra(grid,iso,triangles,0,6,1,4);
      polygoniseTetra(grid,iso,triangles,5,6,1,4);


      vertices and edges 

                                      
                   4+-------4----------+5
                   /|                 /|                                     
                  7 |                5 |                                      
                 /  |               /  |                                      
               7+---------6--------+6  9                                                         
                |   |8             |   |                                     
               11   |             10   |                                     
                |  0+-------0------|---+1
                |  /               |  /                                     
                | 3                | 1                                        
                |/                 |/                                        
               3+--------2---------+2
                                   
    */
    public static void polygonizeCube_tetra(Cell cell, double iso, Vector3d triangles[], TriangleCollector ggen){

        int count;

        count = polygonizeTetra(cell, iso, 0,2,3,7, triangles); if(count > 0) addTri(ggen, triangles, count);
        count = polygonizeTetra(cell, iso, 0,6,2,7, triangles); if(count > 0) addTri(ggen, triangles, count);
        count = polygonizeTetra(cell, iso, 0,4,6,7, triangles); if(count > 0) addTri(ggen, triangles, count);
        count = polygonizeTetra(cell, iso, 0,6,1,2, triangles); if(count > 0) addTri(ggen, triangles, count);
        count = polygonizeTetra(cell, iso, 0,1,6,4, triangles); if(count > 0) addTri(ggen, triangles, count);
        count = polygonizeTetra(cell, iso, 5,6,1,4, triangles); if(count > 0) addTri(ggen, triangles, count);
        
    }


    /**
       return number of triangles via given tetrahedron
       
       triangles are stored in tri[]
       
       normals to triangle is pointed toward negative values of function
       this is to make positive valued area looks solid from outside 
              
    */
    public static int polygonizeTetra(Cell g, double iso,int v0,int v1,int v2,int v3,Vector3d tri[]) {
        
        /*
          Determine which of the 16 cases we have given which vertices
          are above or below the isosurface
        */
        int triindex = 0;
        if (g.val[v0] < iso) triindex |= 1;
        if (g.val[v1] < iso) triindex |= 2;
        if (g.val[v2] < iso) triindex |= 4;
        if (g.val[v3] < iso) triindex |= 8;
        
        // Form the vertices of the triangles for each case      
        switch (triindex) {
        case 0x00:
        case 0x0F:
            return 0;
            
        case 0x0E: // 1110  01 03 02 
            tri[0] = vertexInterp(iso,g.p[v0],g.p[v1],g.val[v0],g.val[v1]);
            tri[1] = vertexInterp(iso,g.p[v0],g.p[v3],g.val[v0],g.val[v3]);
            tri[2] = vertexInterp(iso,g.p[v0],g.p[v2],g.val[v0],g.val[v2]);
            return 1;
            
        case 0x01: //  0001 01 02 03 
            tri[0] = vertexInterp(iso,g.p[v0],g.p[v1],g.val[v0],g.val[v1]);
            tri[1] = vertexInterp(iso,g.p[v0],g.p[v2],g.val[v0],g.val[v2]);
            tri[2] = vertexInterp(iso,g.p[v0],g.p[v3],g.val[v0],g.val[v3]);
            return 1;
            
        case 0x0D: // 1101 
            tri[0] = vertexInterp(iso,g.p[v1],g.p[v0],g.val[v1],g.val[v0]);
            tri[1] = vertexInterp(iso,g.p[v1],g.p[v2],g.val[v1],g.val[v2]);
            tri[2] = vertexInterp(iso,g.p[v1],g.p[v3],g.val[v1],g.val[v3]);
            return 1;
            
        case 0x02: // 0010   10 13 12 
            tri[0] = vertexInterp(iso,g.p[v1],g.p[v0],g.val[v1],g.val[v0]);
            tri[1] = vertexInterp(iso,g.p[v1],g.p[v3],g.val[v1],g.val[v3]);
            tri[2] = vertexInterp(iso,g.p[v1],g.p[v2],g.val[v1],g.val[v2]);
            return 1;
            
        case 0x0C: // 1100  12 13 03, 12 03 02 
            tri[0] = vertexInterp(iso,g.p[v1],g.p[v2],g.val[v1],g.val[v2]);
            tri[1] = vertexInterp(iso,g.p[v1],g.p[v3],g.val[v1],g.val[v3]);
            tri[2] = vertexInterp(iso,g.p[v0],g.p[v3],g.val[v0],g.val[v3]);
            
            tri[3] = tri[0];
            tri[4] = tri[2];
            tri[5] = vertexInterp(iso,g.p[v0],g.p[v2],g.val[v0],g.val[v2]);
            return 2;
            
        case 0x03: // 0011  12 03 13, 12 02 03 
            tri[0] = vertexInterp(iso,g.p[v1],g.p[v2],g.val[v1],g.val[v2]);
            tri[1] = vertexInterp(iso,g.p[v0],g.p[v3],g.val[v0],g.val[v3]);
            tri[2] = vertexInterp(iso,g.p[v1],g.p[v3],g.val[v1],g.val[v3]);
            tri[3] = tri[0];
            tri[4] = vertexInterp(iso,g.p[v0],g.p[v2],g.val[v0],g.val[v2]);
            tri[5] = tri[1];
            return 2;
            
        case 0x0B: // 1011  2-> 013
            tri[0] = vertexInterp(iso,g.p[v2],g.p[v0],g.val[v2],g.val[v0]);
            tri[1] = vertexInterp(iso,g.p[v2],g.p[v3],g.val[v2],g.val[v3]);
            tri[2] = vertexInterp(iso,g.p[v2],g.p[v1],g.val[v2],g.val[v1]);
            return 1;
            
        case 0x04: // 0100  2 -> 013
            tri[0] = vertexInterp(iso,g.p[v2],g.p[v0],g.val[v2],g.val[v0]);
            tri[1] = vertexInterp(iso,g.p[v2],g.p[v1],g.val[v2],g.val[v1]);
            tri[2] = vertexInterp(iso,g.p[v2],g.p[v3],g.val[v2],g.val[v3]);
            return 1;
            
        case 0x0A: // 1010 
            tri[0] = vertexInterp(iso,g.p[v0],g.p[v1],g.val[v0],g.val[v1]);
            tri[1] = vertexInterp(iso,g.p[v0],g.p[v3],g.val[v0],g.val[v3]);
            tri[2] = vertexInterp(iso,g.p[v2],g.p[v3],g.val[v2],g.val[v3]);
            tri[3] = tri[0];
            tri[4] = tri[2];
            tri[5] = vertexInterp(iso,g.p[v1],g.p[v2],g.val[v1],g.val[v2]);
            return 2;        
            
        case 0x05: // 0101 
            tri[0] = vertexInterp(iso,g.p[v0],g.p[v1],g.val[v0],g.val[v1]);
            tri[1] = vertexInterp(iso,g.p[v1],g.p[v2],g.val[v1],g.val[v2]);
            tri[2] = vertexInterp(iso,g.p[v2],g.p[v3],g.val[v2],g.val[v3]);
            tri[3] = tri[0];
            tri[4] = tri[2];
            tri[5] = vertexInterp(iso,g.p[v0],g.p[v3],g.val[v0],g.val[v3]);
            return 2;
            
        case 0x09: // 1001
            tri[0] = vertexInterp(iso,g.p[v0],g.p[v1],g.val[v0],g.val[v1]);
            tri[1] = vertexInterp(iso,g.p[v2],g.p[v3],g.val[v2],g.val[v3]);
            tri[2] = vertexInterp(iso,g.p[v1],g.p[v3],g.val[v1],g.val[v3]);
            tri[3] = tri[0];
            tri[4] = vertexInterp(iso,g.p[v0],g.p[v2],g.val[v0],g.val[v2]);
            tri[5] = tri[1];
            return 2;
            
        case 0x06: // 0110
            tri[0] = vertexInterp(iso,g.p[v0],g.p[v1],g.val[v0],g.val[v1]);
            tri[1] = vertexInterp(iso,g.p[v1],g.p[v3],g.val[v1],g.val[v3]);
            tri[2] = vertexInterp(iso,g.p[v2],g.p[v3],g.val[v2],g.val[v3]);
            tri[3] = tri[0];
            tri[4] = tri[2];
            tri[5] = vertexInterp(iso,g.p[v0],g.p[v2],g.val[v0],g.val[v2]);
            return 2;
            
        case 0x07: // 0111
            tri[0] = vertexInterp(iso,g.p[v0],g.p[v3],g.val[v0],g.val[v3]);
            tri[1] = vertexInterp(iso,g.p[v1],g.p[v3],g.val[v1],g.val[v3]);
            tri[2] = vertexInterp(iso,g.p[v2],g.p[v3],g.val[v2],g.val[v3]);
            return 1;
            
        case 0x08: // 1000
            tri[0] = vertexInterp(iso,g.p[v0],g.p[v3],g.val[v0],g.val[v3]);
            tri[1] = vertexInterp(iso,g.p[v2],g.p[v3],g.val[v2],g.val[v3]);
            tri[2] = vertexInterp(iso,g.p[v1],g.p[v3],g.val[v1],g.val[v3]);
            return 1;
        }
        return 0;
    } //polygoniseTetra()
    
    
    
    /**
       add one or two triangles to ggen 
     */
    static void addTri(TriangleCollector ggen, Vector3d tri[], int count){

        switch(count){
        default:
            return;
        case 5:
            ggen.addTri(tri[12],tri[13],tri[14]);
            // no break 
        case 4:
            ggen.addTri(tri[9],tri[10],tri[11]);      
            // no break 
        case 3:
            ggen.addTri(tri[6], tri[7], tri[8]);      
            // no break 
        case 2:
            ggen.addTri(tri[3], tri[4], tri[5]);      
            // no break 
        case 1:
            ggen.addTri(tri[0],tri[1],tri[2]);      
        }
    }

    static final double EPS = 1.e-12;

    /*
      Linearly interpolate the position where an isosurface cuts
      an edge between two vertices, each with their own scalar value
    */
    public static Vector3d vertexInterp(double isolevel,Vector3d p1,Vector3d p2, double valp1, double valp2){    
        
        if (abs(isolevel-valp1) < EPS)
            return(p1);
        if (abs(isolevel-valp2) < EPS)
            return(p2);
        if (abs(valp1-valp2) < EPS)
            return(p1);
        
        double mu = (isolevel - valp1) / (valp2 - valp1);
        
        double x = p1.x + mu * (p2.x - p1.x);
        double y = p1.y + mu * (p2.y - p1.y);
        double z = p1.z + mu * (p2.z - p1.z);

        return new Vector3d(x,y,z);
                                    
    }

    /*
      Linearly interpolate the position where an isosurface cuts
      an edge between two vertices, each with their own scalar value
    */
    public static void vertexInterp(double isolevel,Vector3d p1,Vector3d p2, double valp1, double valp2, Vector3d dest){

        if (abs(isolevel-valp1) < EPS)
            dest.set(p1);
        if (abs(isolevel-valp2) < EPS)
            dest.set(p2);
        if (abs(valp1-valp2) < EPS)
            dest.set(p1);

        double mu = (isolevel - valp1) / (valp2 - valp1);

        double x = p1.x + mu * (p2.x - p1.x);
        double y = p1.y + mu * (p2.y - p1.y);
        double z = p1.z + mu * (p2.z - p1.z);

        dest.set(x,y,z);

    }


    public static class Cell {

        double val[]; // values at corners of the cube         
        Vector3d p[]; // coordinates of corners of he cube 
        Vector3d e[]; // coordinates of isosurface-edges intersections 
        
        Cell(){
            
            val = new double[8];
            p = new Vector3d[8];
            e = new Vector3d[12];
            
            for(int i = 0; i < p.length; i++){
                p[i] = new Vector3d();
            }
            for(int i = 0; i < e.length; i++){
                e[i] = new Vector3d();
            }
        }    
    }

    /**
       inteface to calculate one scice of data in x,y plane 

     */
    public static interface SliceCalculator {
        
        /**
           method shall fill data array of sliceData with values 
         */
        public void getSlice(SliceData sliceData);
    }


    /**
       interface to return data value at the given point 
     */
    public interface DataXYZ {

        public double getData(double x, double y, double z); 

    }

    

    /**
       data holder for one slice of data in xy-plane 
       
     */
    public static class SliceData {

        public int nx, ny;
        // bounds of the slice 
        public double xmin, ymin, xmax, ymax;  
        // data values in x,y points 
        public double data[];
        public double z; 
        
        SliceData(int nx, int ny, double xmin, double xmax, double ymin, double ymax){

            this.nx = nx;
            this.ny = ny;
            
            this.xmin = xmin;
            this.ymin = ymin;
            this.xmax = xmax;
            this.ymax = ymax;

            data = new double[nx * ny];
            
        }

        void setZ(double z){
            this.z = z;
        }

    } // class SliceData 


    /**
       class calculates slice of data from DataXYZ 
       
     */
    public static class SliceFunction implements SliceCalculator {
        
        DataXYZ fdata;
        
        public SliceFunction(DataXYZ fdata){

            this.fdata = fdata;

        }

        public void getSlice(SliceData sliceData){

            int nx = sliceData.nx;
            int ny = sliceData.ny;
            
            double xmin = sliceData.xmin;
            double ymin = sliceData.ymin;

            double dx = (sliceData.xmax - xmin)/(nx-1);
            double dy = (sliceData.ymax - ymin)/(ny-1);
            double z = sliceData.z;
            double data[] = sliceData.data;

            for(int iy = 0; iy < ny; iy++){

                double y = ymin + iy * dy;

                int offset = iy*nx;

                for(int ix = 0; ix < nx; ix++){

                    double x = xmin + ix * dx;
                    data[offset + ix] = fdata.getData(x,y,z);
                }
            }
            
        }
    } // class SliceFunction 



    /**
       class calculates slice of data from a Grid
       
     */
    public static class SliceGrid implements SliceCalculator {
        
        // minimal value of distance from zero 
        static double MIN_DISTANCE = 1.e-3; 

        Grid grid;
        double bounds[];
        int gnx, gny, gnz; // size of grid 
        double gdx, gdy, gdz; // pixel size of grid 
        double gxmin, gymin, gzmin; // origin of the grid 
        int m_smoothSteps = 0;

        public SliceGrid(Grid grid, double bounds[], int smoothSteps){

            this.grid = grid;
            this.bounds = bounds.clone();
            m_smoothSteps = smoothSteps;
            gnx = grid.getWidth();
            gny = grid.getHeight();
            gnz = grid.getDepth();
            
            gdx = (bounds[1] - bounds[0])/(gnx-1);
            gdy = (bounds[3] - bounds[2])/(gny-1);
            gdz = (bounds[5] - bounds[4])/(gnz-1);

            gxmin = bounds[0];
            gymin = bounds[2];
            gzmin = bounds[4];

        }

        static int round(double x){
            return (int)Math.floor(x + 0.5);
        }

        public void getSlice(SliceData sliceData){

            int nx = sliceData.nx;
            int ny = sliceData.ny;

            double xmin = sliceData.xmin;
            double ymin = sliceData.ymin;

            double dx = (sliceData.xmax - xmin)/(nx-1);
            double dy = (sliceData.ymax - ymin)/(ny-1);            

            double z = sliceData.z;

            int gz = round((z - gzmin)/gdz);
            
            double data[] = sliceData.data;
           
            for(int iy = 0; iy < ny; iy++){

                double y = ymin + iy*dy;
                int gy = round((y - gymin)/gdy);

                int offset = iy * nx;

                for(int ix = 0; ix < nx; ix++){

                    double x = xmin + ix*dx;

                    int gx = round((x - gxmin)/gdx);
                    data[offset + ix] = getGridData(gx,gy,gz, m_smoothSteps);
                }
            }            
        }

        /**
           return data at the grid point 
           does recursive averaging
         */
        double getGridData(int gx, int gy, int gz, int smoothSteps){

            if(smoothSteps == 0){
                if(gx <  0 || gy < 0 || gz < 0 || gx >= gnx || gy >= gny || gz >= gnz){
                    return 1;
                } else {
                    byte state = grid.getState(gx,gy,gz);
                    if(state == Grid.OUTSIDE)
                        return 1;
                    else 
                        return -1;
                }            
            } else {
                smoothSteps--;
                double sum = 0.;
                //double orig = getGridData(gx,   gy,   gz, smoothSteps);
                sum += getGridData(gx+1, gy,   gz, smoothSteps);
                sum += getGridData(gx-1, gy,   gz, smoothSteps);
                sum += getGridData(gx,   gy+1, gz, smoothSteps);
                sum += getGridData(gx,   gy-1, gz, smoothSteps);
                sum += getGridData(gx,   gy,   gz+1, smoothSteps);
                sum += getGridData(gx,   gy,   gz-1, smoothSteps);
                sum /= 6;
                
                return sum;
                /*
                  this makes some odd sharp corners 
                if(orig > 0 && sum < 0)
                    return MIN_DISTANCE;
                else if(orig < 0 && sum > 0)
                    return -MIN_DISTANCE;
                else 
                    return sum;
                */
            }
        }
    } // class SliceGrid 



    /**
       class calculates slice of data from a Grid doing averaging over neighbors 
       
     */
    public static class SliceGrid2 implements SliceCalculator {

        
        Grid grid;
        double bounds[];
        int gnx, gny, gnz; // size of grid 
        double gdx, gdy, gdz; // pixel size of grid 
        double gxmin, gymin, gzmin; // origin of the grid 

        int m_cubeHalfSize = 0; // allowed values are 0, 1, 2, ...
        double m_bodyVoxelWeight = 1.0;

        public SliceGrid2(Grid grid, double bounds[], int resamplingFactor){
            this(grid,bounds, resamplingFactor, 1.0);
        }

        /**
           body voxels may have larger weight ( > 1. ) or smaller weight ( < 1.)  
         */
        public SliceGrid2(Grid grid, double bounds[], int resamplingFactor, double bodyVoxelWeight){

            this.grid = grid;
            this.bounds = bounds.clone();
            m_cubeHalfSize = resamplingFactor / 2;
            m_bodyVoxelWeight = bodyVoxelWeight;

            gnx = grid.getWidth();
            gny = grid.getHeight();
            gnz = grid.getDepth();
            
            gdx = (bounds[1] - bounds[0])/(gnx-1);
            gdy = (bounds[3] - bounds[2])/(gny-1);
            gdz = (bounds[5] - bounds[4])/(gnz-1);

            gxmin = bounds[0];
            gymin = bounds[2];
            gzmin = bounds[4];

        }

        static int round(double x){
            return (int)Math.floor(x + 0.5);
        }

        public void getSlice(SliceData sliceData){

            int nx = sliceData.nx;
            int ny = sliceData.ny;

            double xmin = sliceData.xmin;
            double ymin = sliceData.ymin;

            double dx = (sliceData.xmax - xmin)/(nx-1);
            double dy = (sliceData.ymax - ymin)/(ny-1);            

            double z = sliceData.z;

            int gz = round((z - gzmin)/gdz);
            
            double data[] = sliceData.data;
           
            for(int iy = 0; iy < ny; iy++){

                double y = ymin + iy*dy;
                int gy = round((y - gymin)/gdy);

                int offset = iy * nx;

                for(int ix = 0; ix < nx; ix++){

                    double x = xmin + ix*dx;

                    int gx = round((x - gxmin)/gdx);
                    data[offset + ix] = getGridData(gx,gy,gz);
                }
            }            
        }

        /**
           return data at the grid point 
           does averaging
         */
        double getGridData(int gx, int gy, int gz){

            double sum =0; 
            double norm = 0;
            for(int dx = -m_cubeHalfSize; dx <= m_cubeHalfSize; dx++){
                for(int dy = -m_cubeHalfSize; dy <= m_cubeHalfSize; dy++){
                    for(int dz = -m_cubeHalfSize; dz <= m_cubeHalfSize; dz++){
                        double v = getGridState(gx+dx, gy+dy, gz+dz);
                        if( v < 0.0){// body voxel 
                            sum += v * m_bodyVoxelWeight; 
                            norm += m_bodyVoxelWeight;
                        } else {
                            sum += v; 
                            norm += 1;
                        }
                    }
                }
            }
            if(abs(sum) < TINY_VALUE){
                sum = (sum > 0.)? TINY_VALUE: -TINY_VALUE;
                //printf("zero sum in getGridData: %g\n", sum);
            }
            return sum * norm;

        }

        int getGridState(int gx, int gy, int gz){

            if(gx <  0 || gy < 0 || gz < 0 || gx >= gnx || gy >= gny || gz >= gnz){
                return 1; // outside
            } else {
                byte state = grid.getState(gx,gy,gz);
                if(state == Grid.OUTSIDE)
                    return 1;
                else 
                    return -1;
            }            
        }
    } // class SliceGrid2


    /**
       
       allocates copy of block of grid and does optional convolution over that block using 
       given kernel 

     */
    public static class BlockSmoothingSlices implements SliceCalculator {


        AttributeGrid agrid;
        Grid grid;
        double bounds[] = new double[6];


        int gnx, gny, gnz; // size of grid 
        double gdx, gdy, gdz; // pixel size of grid 
        double gxmin, gymin, gzmin; // origin of the grid 
        
        double blockData[]; // data of the block 
        double rowData[];// data for one row for convolution 

        // bondary of 3D block of grid 
        // it is larger than actual block of data due to increase by size of the kernel
        // the data along the boundary of the block should not be used
        int bxmin, bymin, bzmin;
        int bsizex, bsizey,bsizez;  
        // if this is positive - the class will use attribute instead of state
        // and will assume, that 
        //  inside voxels have attribute value gridMaxAttributeValue an 
        //  outside voxles have attribute value 0  
        //  and linear interpolation on the boundary 
        int gridMaxAttributeValue = 0;
        
        boolean containsIsosurface = false;
        /**
           
         */
        public BlockSmoothingSlices(Grid grid){
            
            this.grid = (Grid)grid;
            if (grid instanceof AttributeGrid) {
                agrid = (AttributeGrid) grid;
            }
            grid.getGridBounds(this.bounds);

            gnx = grid.getWidth();
            gny = grid.getHeight();
            gnz = grid.getDepth();
            
            gdx = (bounds[1] - bounds[0])/(gnx-1);
            gdy = (bounds[3] - bounds[2])/(gny-1);
            gdz = (bounds[5] - bounds[4])/(gnz-1);

            gxmin = bounds[0];
            gymin = bounds[2];
            gzmin = bounds[4];

        }

        public void setGridMaxAttributeValue(int value){

            gridMaxAttributeValue = value;
            
        }
        /**
           
           block bounds are given in integer cordinates of voxels 
           block bounds are inclusive 
           if kernel is null - no convolution is performed 
         */
        public void initBlock(int xmin, int xmax, int ymin, int ymax, int zmin, int zmax, double kernel[]){ 
            
            //printf("initBloc(%d %d %d %d %d %d)\n",xmin, xmax, ymin, ymax, zmin, zmax);
            
            int kernelSize = 0; 
            if(kernel != null){
                kernelSize = kernel.length/2;
            }

            bxmin = xmin - kernelSize;
            bymin = ymin - kernelSize;
            bzmin = zmin - kernelSize;

            bsizex = (xmax - xmin + 1) + 2*kernelSize;
            bsizey = (ymax - ymin + 1) + 2*kernelSize;
            bsizez = (zmax - zmin + 1) + 2*kernelSize;            
            
            int dataSize = bsizex * bsizey * bsizez;
            
            if(blockData == null || dataSize > blockData.length){
                blockData = new double[dataSize];
            }
            
            int maxSize = bsizex;
            if(bsizey > maxSize)maxSize = bsizey;
            if(bsizez > maxSize)maxSize = bsizez;
            
            if(rowData == null || rowData.length < maxSize)
                rowData = new double[maxSize];
            
            boolean hasPlus = false, hasMinus = false;
            
            // fill block with data from grid 
            for(int y = 0; y < bsizey; y++){
                int y0 = y + bymin;
                int xoffset  = y*bsizez*bsizex;

                for(int x = 0; x < bsizex; x++){
                    int x0 = x + bxmin;
                    int zoffset  = xoffset + x*bsizez;

                    for(int z = 0; z < bsizez; z++){
                        // TODO et data for z row in one call 
                        double v = getGridData(x0, y0, z + bzmin);
                        if(v > 0.)
                            hasPlus = true;
                        else if(v < 0.)
                            hasMinus = true;
                        blockData[zoffset + z] = v;
                        
                    }
                }
            } 
            
            if(hasPlus && hasMinus){

                containsIsosurface = true;
                if(kernelSize > 0){
                    convoluteX(blockData, kernel);
                    convoluteY(blockData, kernel);
                    convoluteZ(blockData, kernel);              
                }
            } else {
                containsIsosurface = false;
            }
            //printf("containsIsosurface:%s\n", containsIsosurface);

        }

        void convoluteX(double data[], double kernel[]){
            
            int ksize = kernel.length/2;
            int bsizexz = bsizex*bsizez;
            
            for(int y = 0; y < bsizey; y++){

                int offsetx = y * bsizexz;
                
                for(int z = 0; z < bsizez; z++){

                    // init accumulator array 
                    Arrays.fill(rowData, 0, bsizex, 0.);

                    for(int x = 0; x < bsizex; x++){
                        
                        int offsetz = offsetx + x * bsizez;
                        double v = blockData[offsetz + z];
                        // add this value to accumulator
                        for(int k = 0; k < kernel.length; k++){
                            int kx = x + k - ksize;
                            if(kx >= 0 && kx < bsizex)
                                rowData[kx] += kernel[k] * v;                            
                        }
                    } 
                    
                    for(int x = 0; x < bsizex; x++){
                        int offsetz = offsetx + x * bsizez;
                        blockData[offsetz + z] = rowData[x];
                    }                    
                }
            }
        }

        void convoluteY(double data[], double kernel[]){

            int ksize = kernel.length/2;
            int bsizexz = bsizex*bsizez;
            
            for(int x = 0; x < bsizex; x++){
                for(int z = 0; z < bsizez; z++){
                    // init accumulator array 
                    Arrays.fill(rowData, 0, bsizey, 0.);
                    for(int y = 0; y < bsizey; y++){
                        
                        int offsetz = y * bsizexz + x * bsizez;
                        double v = blockData[offsetz + z];
                        // add this value to accumulator
                        for(int k = 0; k < kernel.length; k++){
                            int ky = y + k - ksize;
                            if(ky >= 0 && ky < bsizey)
                                rowData[ky] += kernel[k] * v;                            
                        }
                    } 

                    for(int y = 0; y < bsizey; y++){
                        int offsetz = y * bsizexz + x * bsizez;
                        blockData[offsetz + z] = rowData[y];
                    }                    
                }
            }

        }

        void convoluteZ(double data[], double kernel[]){
            
            int ksize = kernel.length/2;
            int bsizexz = bsizex*bsizez;
            for(int y = 0; y < bsizey; y++){

                int offsetx = y * bsizexz;
                
                for(int x = 0; x < bsizex; x++){

                    int offsetz = offsetx + x * bsizez;
                    // init accumulator array 
                    Arrays.fill(rowData, 0, bsizez, 0.);

                    for(int z = 0; z < bsizez; z++){
                        
                        double v = blockData[offsetz + z];
                        // add this value to accumulator
                        for(int k = 0; k < kernel.length; k++){
                            int kz = z + k - ksize;
                            if(kz >= 0 && kz < bsizez)
                                rowData[kz] += kernel[k] * v;                            
                        }
                    } 
                    for(int z = 0; z < bsizez; z++){
                        blockData[offsetz + z] = rowData[z];
                    }                    
                }
            }
        }
        
        /**
           
         */
        public boolean containsIsosurface(){
            return containsIsosurface; 
        }

        static final int round(double x){
            return (int)Math.floor(x + 0.5);
        }

        public void getSlice(SliceData sliceData){

            int nx = sliceData.nx;
            int ny = sliceData.ny;

            double xmin = sliceData.xmin;
            double ymin = sliceData.ymin;

            double dx = (sliceData.xmax - xmin)/(nx-1);
            double dy = (sliceData.ymax - ymin)/(ny-1);            

            double z = sliceData.z;

            int gz = round((z - gzmin)/gdz);
            
            double data[] = sliceData.data;
           
            for(int iy = 0; iy < ny; iy++){

                double y = ymin + iy*dy;
                int gy = round((y - gymin)/gdy);

                int offset = iy * nx;

                for(int ix = 0; ix < nx; ix++){

                    double x = xmin + ix*dx;

                    int gx = round((x - gxmin)/gdx);
                    data[offset + ix] = getBlockData(gx,gy,gz);
                }
            }            
        }

        /**
           return data at the grid point 
           does averaging
         */
        double getBlockData(int gx, int gy, int gz){

            gx -= bxmin;
            gy -= bymin;
            gz -= bzmin;
            
            if(gx < 0 || gx >= bsizex || gy < 0 || gy >= bsizey || gz < 0 || gz >= bsizez )
                return 1.;
                        
            return blockData[(gy*bsizex + gx) * bsizez + gz];
            
        }


        double getGridData(int gx, int gy, int gz){

            if(gx <  0 || gy < 0 || gz < 0 || gx >= gnx || gy >= gny || gz >= gnz){
                return 1.; // outside
            } else {
                switch(gridMaxAttributeValue){
                case 0: 
                    {
                        byte state = grid.getState(gx,gy,gz);
                        if(state == Grid.OUTSIDE)
                            return 1.; // outside 
                        else 
                            return -1.; // inside
                    }
                default: 
                    {
                        int att = (agrid.getAttribute(gx,gy,gz));
                        return 1. - (2.*att / gridMaxAttributeValue);
                    }
                    
                }            
            }
        }
    } // class BlockSmoothingSlices
    
}
