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

import javax.vecmath.Vector3d; 

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

    static final boolean DEBUG = true;    
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
       set dimension of data (x,y,z and attributes) 
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
        printf("setSortPoints(%s)\n", value);
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
       returs surface points into 3 arrays 
       @param pntx  x coordinates 
       @param pnty  y coordinates 
       @param pntz  z coordinates 
     */
    public void getPoints(double pntx[],double pnty[],double pntz[]){
        
        getPointsInGridUnits(pntx,pnty,pntz);
        //
        // original coordinates are in grid units. we need to transform into physical units 
        //
        for(int i = 0; i < pntx.length; i++){
            pntx[i] = toWorldX(pntx[i]);
            pnty[i] = toWorldY(pnty[i]);
            pntz[i] = toWorldZ(pntz[i]);
        }
    }


    /**
       
     */
    private void getPointsInGridUnits(double pntx[],double pnty[],double pntz[]){
        printf("getPointsInGridUnits()\n");
        if(m_sortPoints){
            // do point sorting in the inreased Y-coordinate wih grid precision 
            getPointsInGridUnitsSorted(pntx, pnty, pntz, m_points, m_ny);

        } else {
            //
            // coordinates are in grid units 
            //
            int npnt = m_points.size();
            Vec pnt = new Vec(m_dataDimension);
            for(int i = 0; i < npnt; i++){
                m_points.getPoint(i, pnt);
                pntx[i] = pnt.v[0];
                pnty[i] = pnt.v[1];
                pntz[i] = pnt.v[2];
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
        printf("points sorting time: %d ms\n", time() - t0);
    }

    /**
       this method MUST be called before starting adding triangles 
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
    
    Vector3d // work vectors 
        v0 = new Vector3d(),
        v1 = new Vector3d(),
        v2 = new Vector3d(),
        m_v1 = new Vector3d(),
        m_v2 = new Vector3d(),
        m_normal = new Vector3d();
    
    /**
       method of interface TriangleCollector2 
       
     */
    public boolean addTri2(Vec p0, Vec p1, Vec p2){
        if(m_useBestPlane) 
            return addTri_bestPlane(p0,p1,p2);
        else 
            return addTri_allPlanes(p0,p1,p2);
    }

    /**       
       render triangle in the plane orthogonal to the longest normal projection 
       rendering generates points on intersection of triangle and voxel grid lines 
    */
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

    /**
      render triangle in all 3 planes 
      
    */
    protected boolean addTri_allPlanes(Vec p0, Vec p1, Vec p2){

        m_triCount++;
        p0.get(v0);
        p1.get(v1);
        p2.get(v2);

        toGrid(v0);
        toGrid(v1);
        toGrid(v2);
        // calculate triangle normal 
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

        // minimal normal projection to render in that orientation 
        final double NEPS = 0.01;

        // render triangle in 3 orthogonal directions
        if(abs(nx) > NEPS) {
            m_voxelRenderer.setAxis(0);
            m_voxelRenderer.setPlane(-ny/nx, -nz/nx, nv0/nx); 
            m_triRenderer.fillTriangle(m_voxelRenderer, v0.y, v0.z,v1.y, v1.z, v2.y, v2.z);
        }
        if(abs(ny) > NEPS) {
            m_voxelRenderer.setAxis(1);
            m_voxelRenderer.setPlane(-nz/ny, -nx/ny, nv0/ny); 
            m_triRenderer.fillTriangle(m_voxelRenderer, v0.z, v0.x,v1.z, v1.x, v2.z, v2.x);
        }
        if(abs(nz) > NEPS) {
            m_voxelRenderer.setAxis(2);
            m_voxelRenderer.setPlane(-nx/nz, -ny/nz, nv0/nz); 
            m_triRenderer.fillTriangle(m_voxelRenderer, v0.x, v0.y,v1.x, v1.y, v2.x, v2.y);
        }
        return true;
    }

    final void toGrid(Vector3d v){
        v.x = toGridX(v.x);
        v.y = toGridY(v.y);
        v.z = toGridZ(v.z);
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

        double m_ax, m_ay, m_az;
        int m_axis = 2;
        Vec m_work = new Vec(m_dataDimension);
        double m_u[] = new double[m_dataDimension];
        double m_v[] = new double[m_dataDimension];
        double m_w[] = new double[m_dataDimension];

        final void setAxis(int axis){
            m_axis = axis;
        }
        
        final void setPlane(double ax, double ay, double az){
            m_ax = ax;
            m_ay = ay;
            m_az = az;
        }
        
        public void setPixel(int ix, int iy){
            
            double 
                x = ix + HALF,
                y = iy + HALF,                
                z = m_ax*x + m_ay*y + m_az;
            
            switch(m_axis){
            case 0: m_work.set(z,x,y); break;
            case 1: m_work.set(y,z,x); break;
            case 2: m_work.set(x,y,z); break;                
            }
            
            switch(m_dataDimension){
            case 3:
                m_work.v[5] = m_u[2]*x + m_v[2]*y + m_w[2];
            case 2:
                m_work.v[4] = m_u[1]*x + m_v[1]*y + m_w[1];
            case 1:
                m_work.v[3] = m_u[0]*x + m_v[0]*y + m_w[0];
            }
            m_points.addPoint(m_work);
        }                   
    } //  class AttributedPointRenderer 

} // class AttributedTriangleMeshSurfaceBuilder 