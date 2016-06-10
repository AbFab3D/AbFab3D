/** 
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

package abfab3d.geom;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit;

//import javax.vecmath.Vector3d; 

import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit;

import abfab3d.util.Vec;
import abfab3d.util.Bounds;
import abfab3d.util.TriangleCollector2;
import abfab3d.util.AttributedPointSet;
import abfab3d.util.AttributedPointSetArray;
import abfab3d.util.TriangleRenderer;
import abfab3d.util.PointToTriangleDistance;

import static java.lang.Math.sqrt;
import static java.lang.Math.max;
import static java.lang.Math.abs;
import static java.lang.Math.min;

import static abfab3d.util.Output.time;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.MathUtil.L2S;
import static abfab3d.util.MathUtil.M00;
import static abfab3d.util.MathUtil.M01;
import static abfab3d.util.MathUtil.M02;
import static abfab3d.util.MathUtil.M10;
import static abfab3d.util.MathUtil.M11;
import static abfab3d.util.MathUtil.M12;
import static abfab3d.util.MathUtil.M20;
import static abfab3d.util.MathUtil.M21;
import static abfab3d.util.MathUtil.M22;
import static abfab3d.util.MathUtil.invertMatrix3;
import static abfab3d.util.MathUtil.multMV3;
import static abfab3d.util.MathUtil.iround;
import static abfab3d.util.MathUtil.clamp;

/**
   generated set of points on the surface of attributed triangle mesh on a given grid 
   
   Each triangle of the mesh is rasterized in the plane orthogonal to the best of 3 possible projections (USE_BEAST_AXIS = true)
   each triangle has few attributes 
   attributes are all components of traingle data after first 3 
   attributes are interpolated inside of triangle and are stored in AttributedPointSet together x,y,z cordinates 

   @author Vladimir Bulatov
 */
public class AttributedTriangleMeshSurfaceBuilder implements TriangleCollector2 {

    final int MAX_DIMENSION = 6;  // max dimension of data (to pre-allocate arrays) 
    static final boolean DEBUG = false;    
    static final double TOL = 1.e-2;
    static final double HALF = 0.5; // half voxel offset to the center of voxel

    protected double m_voxelSize;
    protected Bounds m_bounds;
    // triangles ransterizer 
    protected TriangleRenderer m_triRenderer;

    // callback class for triangles ransterizer 
    protected AttributedPointRenderer m_voxelRenderer;

    // grid to world conversion params 
    protected double m_xmin, m_ymin, m_zmin, m_scale;
    // grid dimension 
    protected int m_nx, m_ny, m_nz;
        
    protected int m_triCount = 0; // count of processed triangles 
    protected int m_estimatedPointCounts = 0;

    protected boolean m_useBestPlane = false;
    protected boolean m_useVertices = false;
    protected boolean m_sortPoints = false;

    AttributedPointSet m_points;

    int m_dataDimension=6; 

    /**
       @aram gridBounds 
     */
    public AttributedTriangleMeshSurfaceBuilder(Bounds gridBounds){

        m_bounds = gridBounds.clone();

    }
    
    /**
       set dimension of data (x,y,z plus attributes) 
     */
    public void setDataDimension(int dimension){
        m_dataDimension = dimension;
    }

    /**
       @param value if true only uses plane with maximal projection 
       if false - all 3 planes are used (xy, yz, zx) 
     */
    public void setUseBestPlane(boolean value){
        m_useBestPlane = value;
    }

    /**
       if
     */
    public void setSortPoints(boolean value) {
        if(DEBUG)printf("setSortPoints(%s)\n", value);
        m_sortPoints = value;
    }

    /**
       @return count of points 
     */
    public int getPointCount(){
        return m_points.size();
    }

    public int getTriCount(){
        return m_triCount;
    }
    
    /**
       returs surface points into 2D array 
       return points into array pnt[dataDimesion][pointsCount]
       point index i data component d is stored in pnts[d][i] 

       @param pnts  array of dimension[dataDimension][pointsCount]
     */
    public void getPoints(double pnt[][]){
        
        if(DEBUG)printf("getPoints()\n");
        getPointsInGridUnits(pnt);
        //
        // original coordinates are in grid units. we need to transform into physical units 
        //
        int count = pnt[0].length;
        for(int i = 0; i < count; i++){
            if(DEBUG)printf("(%5.1f %5.1f %5.1f)\n", pnt[0][i],pnt[1][i],pnt[2][i]);
            pnt[0][i] = toWorldX(pnt[0][i]);
            pnt[1][i] = toWorldY(pnt[1][i]);
            pnt[2][i] = toWorldZ(pnt[2][i]);           
            // other component need no transformation 
        }
    }


    /**
       writes points into array pnt[dataDimesion][pointsCount]
       point index i data component d is stored in pnts[d][i] 
       
     */
    private void getPointsInGridUnits(double pnt[][]){

        if(DEBUG)printf("getPointsInGridUnits()\n");
        if(m_sortPoints){
            throw new RuntimeException("not implemented");
            // do point sorting in the inreased Y-coordinate wih grid precision 
            // sorting seems to be increasing timing 
            //getPointsInGridUnitsSorted(pntx, pnty, pntz, m_points, m_ny);
        } else {
            //
            // coordinates are in grid units 
            //
            int dim = m_dataDimension;
            int npnt = m_points.size();
            Vec p = new Vec(m_dataDimension);
            for(int i = 0; i < npnt; i++){
                m_points.getPoint(i, p);
                if(DEBUG)printf("[%5.1f %5.1f %5.1f]\n", p.v[0],p.v[1],p.v[2]);
                for(int d = 0; d < dim; d++){
                    pnt[d][i] = p.v[d];
                }
            }
        }
    }


    private void getPointsInGridUnitsSorted(double coordx[],double coordy[],double coordz[], AttributedPointSet pnts, int binCount){
        long t0 = time();
        Vec p = new Vec(m_dataDimension);
        int pcount = pnts.size();        
        int binCounts[] = new int[binCount];
        int maxBin = binCount-1;
        // first point is not used 
        for(int i = 1; i < pcount;i++){
            pnts.getPoint(i, p);
            int bin = clamp((int)p.v[1], 0, maxBin);
            binCounts[bin]++;
        }

        //
        // array of bins offsets 
        //
        int binOffset[] = new int[binCount];

        int offset = 0;
        for(int bin = 0; bin < binCount;bin++){
            binOffset[bin] = offset;
            offset += binCounts[bin];
            binCounts[bin] = 0;
        }

        pnts.getPoint(0, p);
        coordx[0] = p.v[0];
        coordy[0] = p.v[1];
        coordz[0] = p.v[2];

        // first point is not used 
        for(int i = 1; i < pcount;i++){

            pnts.getPoint(i, p);
            int bin = clamp((int)p.v[1], 0, maxBin);
            int cindex = (binOffset[bin] + binCounts[bin] + 1);
            binCounts[bin]++;            
            coordx[cindex] = p.v[0];
            coordy[cindex] = p.v[1];
            coordz[cindex] = p.v[2];            
        }        
        if(DEBUG)printf("points sorting time: %d ms\n", time() - t0);
    }

    /**
x       this method MUST be called before starting adding triangles 
     */
    public boolean initialize(){

        
        m_voxelSize = m_bounds.getVoxelSize();
        m_nx = m_bounds.getGridWidth();
        m_ny = m_bounds.getGridHeight();
        m_nz = m_bounds.getGridDepth();

        m_triRenderer = new TriangleRenderer();
        m_voxelRenderer = new AttributedPointRenderer();
        m_xmin = m_bounds.xmin;
        m_ymin = m_bounds.ymin;
        m_zmin = m_bounds.zmin;
        m_scale = 1/m_voxelSize;

        if(m_estimatedPointCounts <= 0) {
            // unknow estimation  use surface of the bounding box
            m_estimatedPointCounts = (m_nx*m_ny + m_ny*m_nz + m_nz*m_nx)*2;
        }
        m_points = new AttributedPointSetArray(m_dataDimension, m_estimatedPointCounts);

        // add unused point to have index start from 1
        m_points.addPoint(new Vec(6));

        // successfull initialization 
        return true;
    }
    
    /*
    Vector3d // work vectors 
        v0 = new Vector3d(),
        v1 = new Vector3d(),
        v2 = new Vector3d(),
        m_v1 = new Vector3d(),
        m_v2 = new Vector3d(),
        m_normal = new Vector3d();
    */ 
    /**
       method of interface TriangleCollector2 
       
     */
    public boolean addTri2(Vec p0, Vec p1, Vec p2){
        if(m_useBestPlane) {
            throw new RuntimeException("not implemented");
            //return addTri_bestPlane(p0,p1,p2);
        } else {
            return addTri_allPlanes(p0,p1,p2);
        }
    }

    /**       
       render triangle in the plane orthogonal to the longest normal projection 
       rendering generates points on intersection of triangle and voxel grid lines 
    */
    /*
    protected boolean addTri_bestPlane(Vec p0, Vec p1, Vec p2){
        m_triCount++;
        p0.get(v0);
        p1.get(v1);
        p2.get(v2);

        toGrid(v0);
        toGrid(v1);
        toGrid(v2);
        //if(false) printf("addTri( %4.1f, %4.1f, %4.1f;  %4.1f, %4.1f, %4.1f;  %4.1f, %4.1f,%4.1f )\n",v0.x,v0.y,v0.z,v1.x,v1.y,v1.z,v2.x,v2.y,v2.z);
        m_v1.sub(v1, v0);
        m_v2.sub(v2, v0);
        m_normal.cross(m_v2,m_v1);
        double 
            nv0 = m_normal.dot(v0),
            nx = m_normal.x,
            ny = m_normal.y,
            nz = m_normal.z,
            anx = abs(nx),
            any = abs(ny),
            anz = abs(nz);        
        //if(false) printf("normal: [%4.1f, %4.1f, %4.1f]\n",nx, ny, nz);

        // select best axis to rasterize 
        // it is axis which has longest normal projection 
        int axis = 2;
        if(anx >= any) {
            if(anx >= anz) axis = 0;
            else axis = 2;
        } else { // anx < any 
            if(any > anz) axis = 1;
            else axis = 2;            
        }
        m_voxelRenderer.setAxis(axis);
        //m_voxelRenderer.setTriangle(v0, v1, v2);
        // pass plane equation to voxelRenderer 
        // pass triangle to voxel renderer 
        switch(axis){
        default:
        case 0: 
            m_voxelRenderer.setPlane(-ny/nx, -nz/nx, nv0/nx); 
            m_triRenderer.fillTriangle(m_voxelRenderer, v0.y, v0.z,v1.y, v1.z, v2.y, v2.z);
            break;
        case 1: 
            m_voxelRenderer.setPlane(-nz/ny, -nx/ny, nv0/ny); 
            m_triRenderer.fillTriangle(m_voxelRenderer, v0.z, v0.x,v1.z, v1.x, v2.z, v2.x);
            break;
        case 2: 
            m_voxelRenderer.setPlane(-nx/nz, -ny/nz, nv0/nz); 
            m_triRenderer.fillTriangle(m_voxelRenderer, v0.x, v0.y,v1.x, v1.y, v2.x, v2.y);
            break;
        }
        // add triangle vertices to deal with super small triangles 
        // this makes each vertex point added 6 times
        // and vertex points are probably unnecessary
        if(m_useVertices){
            m_points.addPoint(p0);
            m_points.addPoint(p1);
            m_points.addPoint(p2);
        }
        //TODO 
        // add triangle edges to deal with super thin triangles  (may be) 

        return true;
    }
    */
    Vec m_v0 = new Vec(6);
    Vec m_v1 = new Vec(6);
    Vec m_v2 = new Vec(6);
    double m_triMat[] = new double[9]; // working matrix
    double m_coeff[][] = new double[MAX_DIMENSION][3];
    double m_values[] = new double[3];
    /**
      render triangle in all 3 planes 
      
    */
    protected boolean addTri_allPlanes(Vec p0, Vec p1, Vec p2){

        m_triCount++;


        //for(int axis = 2; axis < 3; axis++){
        for(int axis = 0; axis < 3; axis++){
        //for(int axis = 1; axis < 2; axis++){

            m_v0.set(p0);
            m_v1.set(p1);
            m_v2.set(p2);
            
            toGrid(m_v0);
            toGrid(m_v1);
            toGrid(m_v2);

            if(DEBUG) printf("axis: %d tri: (%5.1f %5.1f %5.1f; %5.1f %5.1f %5.1f;%5.1f %5.1f %5.1f)\n",
                             axis, m_v0.v[0],m_v0.v[1],m_v0.v[2], m_v1.v[0],m_v1.v[1],m_v1.v[2],m_v2.v[0],m_v2.v[1],m_v2.v[2]);
            
            double u0, v0, u1, v1, u2, v2, w0, w1, w2;
            int iu = (axis+1)%3;
            int iv = (axis+2)%3;
            int iw = (axis)%3;

            u0 = m_v0.v[iu]; 
            u1 = m_v1.v[iu];
            u2 = m_v2.v[iu];

            v0 = m_v0.v[iv]; 
            v1 = m_v1.v[iv];
            v2 = m_v2.v[iv];

            w0 = m_v0.v[iw]; 
            w1 = m_v1.v[iw];
            w2 = m_v2.v[iw];
            // save w values into z component 
            m_v0.v[2] = w0;
            m_v1.v[2] = w1;
            m_v2.v[2] = w2;
            if(DEBUG) printf("triUV: (%5.1f %5.1f %5.1f; %5.1f %5.1f %5.1f;%5.1f %5.1f %5.1f)\n", u0, v0, w0,u1, v1, w1,u2, v2, w2);
            m_triMat[M00] = u0;
            m_triMat[M01] = v0;
            m_triMat[M02] = 1;
            m_triMat[M10] = u1;
            m_triMat[M11] = v1;
            m_triMat[M12] = 1;
            m_triMat[M20] = u2;
            m_triMat[M21] = v2;
            m_triMat[M22] = 1;
            
            if(invertMatrix3(m_triMat) == 0) {
                // non inversible matrix ignore triangle in that orientation
                continue;
            }
            // calculate interpolation coefficients for z and remaining attributes
            for(int d = 2; d < m_dataDimension; d++){
                m_values[0] = m_v0.v[d];
                m_values[1] = m_v1.v[d];
                m_values[2] = m_v2.v[d];
                if(DEBUG)printf("axis: %d values:%7.5f %7.5f %7.5f\n",axis, m_values[0],m_values[1],m_values[2]);
                multMV3(m_triMat, m_values,m_coeff[d]);
                if(DEBUG)printf("axis:%d coeff[%d]: %7.5f %7.5f %7.5f\n",axis, d, m_coeff[d][0],m_coeff[d][1],m_coeff[d][2]);
            }
            m_voxelRenderer.setAxis(axis);
            m_voxelRenderer.setCoeff(m_coeff); 
            m_triRenderer.fillTriangle(m_voxelRenderer, u0,v0,  u1,v1, u2,v2);
        }
        return true;
    }

    final void toGrid(Vec v){
        v.v[0] = toGridX(v.v[0]);
        v.v[1] = toGridY(v.v[1]);
        v.v[2] = toGridZ(v.v[2]);
    }

    final double toGridX(double x){
        return (x - m_xmin)*m_scale;
    }
    final double toGridY(double y){
        return (y - m_ymin)*m_scale;
    }
    final double toGridZ(double z){
        return (z - m_zmin)*m_scale;
    }
    final double toWorldX(double x){
        return (x*m_voxelSize + m_xmin);
    }
    final double toWorldY(double y){
        return (y*m_voxelSize + m_ymin);
    }
    final double toWorldZ(double z){
        return (z*m_voxelSize + m_zmin);
    }

    /**
       renders actual point with imnterpolated attributes 
     */
    class AttributedPointRenderer implements TriangleRenderer.PixelRenderer {

        int m_axis = 2;
        Vec m_work = new Vec(MAX_DIMENSION);
        double m_coeff[][];

        AttributedPointRenderer(){            
        }

        final void setAxis(int axis){
            m_axis = axis;
        }
        
        final void setCoeff(double coeff[][]){
            m_coeff = coeff;
            if(DEBUG) {
                printf("setCoeff()\n");
                for(int d = 0; d < m_coeff.length; d++){
                    printf("%7.5f %7.5f %7.5f\n", m_coeff[d][0],m_coeff[d][1],m_coeff[d][2]);
                }
            }
        }
        
        /**
           caled by TriangeleRenderer for each pixel inside of triangle
           triangle is rasterized in one of 3 possible planes 
           0 yz 
           1 zx 
           2 xy 
           @param iu integer coordinate of pixel 
           @param iv integer coordinate of pixel 
           
         */
        public final void setPixel(int iu, int iv){
            
            // double coordoinates of pixel center (it is shifted by 0.5) 
            double 
                u = iu + HALF,
                v = iv + HALF;
            //
            // interpolate z-component 
            //
            double w = m_coeff[2][0]*u + m_coeff[2][1]*v + m_coeff[2][2];

            //if(DEBUG) printf("uvw: %5.1f  %5.1f  %5.1f\n", u,v,w);

            double x,y,z; // point coordinates in grid units 
            switch(m_axis){
            default:
            case 0: x = w; y = u; z = v; break;
            case 1: x = v; y = w; z = u; break;
            case 2: x = u; y = v; z = w; break;                
            }
            if(DEBUG) printf("          x,y,z: %5.1f %5.1f %5.1f \n", x,y,z);
            // save coord 
            m_work.v[0] = x;
            m_work.v[1] = y;
            m_work.v[2] = z;
            // interpolate other attribiutes
            switch(m_dataDimension){
            case 6:
                m_work.v[5] = m_coeff[5][0] * u + m_coeff[5][1] * v + m_coeff[5][2];
            case 5:
                m_work.v[4] = m_coeff[4][0] * u + m_coeff[4][1] * v + m_coeff[4][2];
            case 4:
                m_work.v[3] = m_coeff[3][0] * u + m_coeff[3][1] * v + m_coeff[3][2];
            }
            m_points.addPoint(m_work);
        }                   
    } //  class AttributedPointRenderer 

} // class AttributedTriangleMeshSurfaceBuilder 