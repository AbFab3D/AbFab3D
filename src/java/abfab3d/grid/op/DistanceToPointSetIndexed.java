/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/


package abfab3d.grid.op;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.abs;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Grid2D;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;

/**
   methods to calculate shortest distance to set of points on a grid
   
   @author Vladimir Bulatov
 */
public class DistanceToPointSetIndexed {
    

    static final double EPS = 1.e-5;  // tolerance for parabolas intersection 
    static final double INF = 1.e10;  // infinity 
    static final boolean DEBUG = false;
    static final boolean DEBUG1 = false;
    static final boolean DEBUG_TIMING = true;
    


    /**
       Indexed Coord Distance Transform 
       calculates 1D distance transform on one dimensional grid of voxels 
       closest distance is calculated to the array of sorted points located at arbitrary positions 
       each oiint has cordinate and value

       @param gridSize grid size
       @param pointCount count of points       
       @param index  array of point indices 
       @param coord  array of point coordinates. Coordinate of point i is coord[index[i]]
       @param value  array of points distance values 
       @param gpindex  output array of closest point indices
       @param v work array of length (pointCount+1) to store parablas of the envelope
       @param w work array of length (count+1) to store cpoord of intersections between parabolas
     */
    public static void ICDT1(int gridSize, 
                             int pointCount, 
                             int index[], 
                             double coord[], 
                             double value[], 
                             int gpindex[], 
                             int v[], 
                             double w[]){
        if(DEBUG) printD("coord:", coord, pointCount);
        if(DEBUG) printD("value: ", value, pointCount);
        int k = 0; // index of current active parabola in the envelope 
        v[0] = 0;  // initial parabola is originaly  lowest in the envelope         
        w[0] = -INF; // boundaries of the first parabola 
        w[1] = INF;
        if(DEBUG1)printD("w:", w, k+2);
        double s = 0;
        for (int p = 1; p < pointCount; p++) {
            // checking next parabola 
            double x1 = coord[index[p]]; // vertex of next parabola
            if(DEBUG1)printf("q:%2d x1:%4.1f \n", p, x1);
            while ( k >=0) {
                // vertex of parabola in the envelope 
                double x0 = coord[index[v[k]]];
                if(DEBUG1)printf("  k:%2d x0:%4.1f \n", k, x0);
                if(abs(x0 - x1) > EPS){ // parabolas have intersection
                    s = (sqr(x1) - sqr(x0) + value[p] - value[v[k]])/(2*(x1-x0));
                    if(DEBUG1)printf("     s: %7.1f\n", s);
                    if (s > w[k]) {
                        // found place for new parabola in envelope 
                        break;
                    }
                }
                k--;
            }   
            k++;
            
            v[k] = p;
            w[k] = s;
            w[k + 1] = INF;
            
            if(DEBUG1) {
                printf("v:");
                printI(v, k+1);
                printf("w:");
                printD(w, k+2);
            }            
        }
        
        k = 0;
        for (int q = 0; q < gridSize; q++) {            
            while (w[k + 1] < q) {
                // these parabolas are ignored 
                k++;
            }
            gpindex[q] = index[v[k]];
        } 
        
    }

    /**
     *  calculates indexed distance transform on 2D grid for a set of points 
     *  @param npnt points count 
     *  @param coordx  array of x coordinates. atrray should have one unused coord at the beginning 
     *  @param coordy  array of y coordinates 
     *  @param indexGrid - on input has indices of closest points in thin layer around the surface, 
     *                   - on output has indices of closest point for each grid point 
     */
    public static void ICDT2(int npnt, double coordx[], double coordy[], AttributeGrid indexGrid){
        
        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();

        Grid2D indexGrid2 = (Grid2D)indexGrid;
            
        // maximal dimension 
        int nm = max(nx, ny);
        // work arrays
        int v[] = new int[nm];
        double w[] = new double[nm+1];
        int ipnt[] = new int[nm+1];
        double value1[] = new double[nm];
        int gpnt[] = new int[nm];

        // make 1D x transforms for each y row 
        for(int iy = 0; iy < ny; iy++){
            int pcnt = 0;
            // prepare 1D chain of points 
            for(int ix = 0; ix < nx; ix++){
                int ind = (int)indexGrid2.getAttribute(ix, iy);
                if(ind > 0){
                    ipnt[pcnt++] = ind;
                }
            }
            if(pcnt > 0){ 
                ICDT1(nx,pcnt, ipnt, coordx, value1, gpnt, v, w);
                // write chain of indices back into 2D grid 
                for(int ix = 0; ix < nx; ix++){
                    indexGrid2.setAttribute(ix, iy, gpnt[ix]);
                }            
            }
        }
        //if(true) return;
        // make 1D y transforms for each x column 
        for(int ix = 0; ix < nx; ix++){
            int pcnt = 0;
            // prepare 1D chain of points 
            for(int iy = 0; iy < ny; iy++){
                int ind = (int)indexGrid2.getAttribute(ix, iy);
                if(ind > 0){
                    ipnt[pcnt] = ind;
                    double x = coordx[ind]-ix;
                    value1[pcnt] = x*x;
                    pcnt++;
                }
            }
            if(pcnt > 0){ 
                ICDT1(ny, pcnt, ipnt, coordy, value1, gpnt, v, w);
                // write chain of indices back into 2D grid 
                for(int iy = 0; iy < ny; iy++){
                    indexGrid2.setAttribute(ix, iy, gpnt[iy]);
                }            
            }
        }
    }


    /**
     *  calculates Indexed Cordinates Distance Transform of 3D grid for a set of points 
     *  @param nx grid x dimension 
     *  @param ny grid y dimension 
     *  @param nz grid z dimension 
     *  @param npnt points count 
     *  @param coordx  array of x coordinates. coordx[0] is unused 
     *  @param coordy  array of y coordinates. coordy[0] is unused  
     *  @param coordz  array of y coordinates. coordz[0] is unused 
     *  @param value array of values of distances at the points 
     *  @param indexGrid - on input has indices of closest points in thin layer around the surface, 
     *                   - on output has indices of closest point for each grid point 
     */
    public static void ICDT3(int npnt, double coordx[], double coordy[], double coordz[], AttributeGrid indexGrid){

        long t0 = time(), t1 = t0;

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();

        // maximal dimension 
        int nm = max(max(nx, ny),nz);
        int nxz = nx*nz;

        // work arrays
        int v[] = new int[nm];
        double w[] = new double[nm+1];
        int ipnt[] = new int[nm+1];
        double value1[] = new double[nm];
        int gpnt[] = new int[nm];

        // make 1D X-transforms 
        for(int iz = 0; iz < nz; iz++){
            for(int iy = 0; iy < ny; iy++){
                int pcnt = 0;
                // prepare 1D chain of points 
                for(int ix = 0; ix < nx; ix++){
                    int ind = (int)indexGrid.getAttribute(ix, iy, iz);
                    if(ind > 0){
                        ipnt[pcnt++] = ind;
                    }
                }
                if(pcnt > 0){ 
                    ICDT1(nx,pcnt, ipnt, coordx, value1, gpnt, v, w);
                    // write chain of indices back into 3D grid 
                    for(int ix = 0; ix < nx; ix++){
                        indexGrid.setAttribute(ix, iy, iz, gpnt[ix]);
                    }            
                }
            }
        }

        if(DEBUG_TIMING){t1= time();printf("x-pass: %d ms\n", t1 - t0);t0 = t1;}
        //if(true) return;
        // make 1D Y-transforms 
        for(int iz = 0; iz < nz; iz++){
            for(int ix = 0; ix < nx; ix++){
                int pcnt = 0;
                // prepare 1D chain of points 
                for(int iy = 0; iy < ny; iy++){
                    int ind = (int)indexGrid.getAttribute(ix, iy, iz);
                    if(ind > 0){
                        ipnt[pcnt] = ind;
                        double x = coordx[ind]-ix;
                        value1[pcnt] = x*x;
                        pcnt++;
                    }
                }
                if(pcnt > 0){ 
                    ICDT1(ny, pcnt, ipnt, coordy, value1, gpnt, v, w);
                    // write chain of indices back into 3D grid 
                    for(int iy = 0; iy < ny; iy++){
                        indexGrid.setAttribute(ix, iy, iz, gpnt[iy]);
                    }            
                }
            }
        }

        if(DEBUG_TIMING){t1= time();printf("y-pass: %d ms\n", t1 - t0);t0 = t1;}
        // make 1D Z-transforms 
        for(int iy = 0; iy < ny; iy++){
            for(int ix = 0; ix < nx; ix++){
                int pcnt = 0;
                // prepare 1D chain of points 
                for(int iz = 0; iz < nz; iz++){
                    int ind = (int)indexGrid.getAttribute(ix, iy, iz);
                    if(ind > 0){
                        ipnt[pcnt] = ind;
                        double x = coordx[ind]-ix;
                        double y = coordy[ind]-iy;
                        value1[pcnt] = x*x + y*y;
                        pcnt++;
                    }
                }
                if(pcnt > 0){ 
                    ICDT1(nz, pcnt, ipnt, coordz, value1, gpnt, v, w);
                    // write chain of indices back into 3D grid 
                    for(int iz = 0; iz < nz; iz++){
                        indexGrid.setAttribute(ix, iy, iz, gpnt[iz]);
                    }            
                }
            }
        }
        if(DEBUG_TIMING){t1 = time();printf("z-pass: %d ms\n", t1 - t0);t0 = t1;}
    }
    
    static void printI(int v[], int n){
        for(int i = 0; i < n; i++){
            printf("%3d ", v[i]);
        }
        printf("\n");
    }

    static void printD(double w[], int n){
        for(int i = 0; i < n; i++){
            double d = w[i];
            if(abs(d + INF) < EPS)
                printf("-INF ");
            else if (abs(d - INF) < EPS)
                printf("+INF ");
            else 
                printf("%4.1f ", d);
        }
        printf("\n");
    }

    static void printD(String s, double w[], int n){
        printf(s);
        printD(w,n);
    }

    static final double sqr(double x){
        return x*x;
    }

    static final int iround(double x){
        return (int)(x + 0.5);
    }    
}