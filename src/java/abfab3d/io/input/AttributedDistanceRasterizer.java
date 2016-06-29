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

package abfab3d.io.input;


import javax.vecmath.Vector3d;

import abfab3d.core.ResultCodes;
import abfab3d.core.Vec;
import abfab3d.core.AttributedTriangleCollector;
import abfab3d.core.AttributedTriangleProducer;
import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.util.PointSetCoordArrays;

import abfab3d.core.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.GridMask;

import abfab3d.grid.op.ClosestPointIndexer;
import abfab3d.grid.op.ClosestPointIndexerMT;

import abfab3d.geom.AttributedTriangleMeshSurfaceBuilder;
import abfab3d.grid.op.PointSetShellBuilder;


import static java.lang.Math.abs;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.MathUtil.step10;


/**
   creates antialiased signed distance grid to attributed triangle mesh 
   each triangle vertex shall have 3 coordinates and few double attributes
   output grid is contains both distance and attributes information stored in its GridDataDesc 
   triangles are sent via TraingleCollector2 interface 
   
   - each triangle is rasterized on a regular grid and we obtain AttributedPointSet of points on the mesh
   - thin shell is build around AttributedPointSet with exact distances calculated at grid points inside of the shell 
   - shell points are sweeped to the whole grid using ClosestPointIndexer and each grid point is assigned ndex of closest point
   - z-buffer rasterizer calculates interior of the mesh
   - voxels distances are calculated as distances to closes point. Interior points are assigned negative distance value
   - voxels attributes are calculated as attributes of the closest point 

   @author Vladimir Bulatov

 */
public class AttributedDistanceRasterizer implements AttributedTriangleCollector {

    static final boolean DEBUG = true;
    static final boolean DEBUG_TIMING = true;


    // this is used purely for precision of distance calculations on distance grid    
    //long m_subvoxelResolution=100;
    // size of grid 
    protected int gridX,gridY,gridZ;

    // z-buffer rasterizer to get mesh interior 
    protected MeshRasterizer m_rasterizer;     
    // triangles rasterizer 
    protected AttributedTriangleMeshSurfaceBuilder m_surfaceBuilder;
    // builder of shell around rasterized points 
    protected PointSetShellBuilder m_shellBuilder;

    protected Bounds m_bounds;
    protected AttributeGrid m_indexGrid;

    // flag to use distance calculation with limited range. Setting it true increases perpormance especiualy if needed 
    // distance range is small
    protected boolean m_useDistanceRange = true;

    // range to calculate distances 
    protected double m_minDistance = -1.*MM;
    protected double m_maxDistance = 1.*MM;

    protected double m_maxDistanceVoxels; // max distance to calculate in case of using distance range 
    protected int m_threadCount = 1;

    // size of surface voxels relative to size fo grid voxles 
    protected double m_surfaceVoxelSize = 1.;
    // total dimnenson of data (3 coord + attributes)
    protected int m_dataDimension = 3;

    protected int m_triCount = 0;

    // half thickness of initial shell around the mesh (in voxels )
    protected double m_shellHalfThickness = 1.0;

    public AttributedDistanceRasterizer(Bounds bounds, int gridX, int gridY, int gridZ){
        
        this.gridX = gridX;
        this.gridY = gridY;
        this.gridZ = gridZ;
        this.m_bounds = bounds.clone();
        
    }
    
    
    /**
       set relative size of voxel used for mesh surface rasterization
       Default value is 1 
       smalller value will cause generation of more points on surface and will increase distance precision
     */
    public void setSurfaceVoxelSize(double voxelSize){

        m_surfaceVoxelSize = voxelSize;

    }

    /**
       set range to calculate distances
     */
    public void setDistanceRange(double minDistance, double maxDistance){

        m_minDistance = minDistance;
        m_maxDistance = maxDistance;
        

    }

    public void setUseDistanceRange(boolean value){

        m_useDistanceRange = value;

    }

    /**
       set thread count for MT parts of algorithm 
     */
    public void setThreadCount(int threadCount){

        m_threadCount = threadCount;

    }

    /**
       set thickness of initial shel whiuch is build around rastrerised surface
     */
    public void setShellHalfThickness(double value){  

        m_shellHalfThickness = value;

    }

    /**
       sets dimension of data (3 coord + attributes) 
     */
    public void setDataDimension(int dataDimension){

        if(DEBUG) printf("AttributedDIstanceRasterizer.setDataDimension(%d)\n", dataDimension);
        m_dataDimension = dataDimension;
    }

    
    protected int init(){

        m_rasterizer = new MeshRasterizer(m_bounds, gridX, gridY, gridZ);
        m_rasterizer.setInteriorValue(1);

        m_indexGrid = createIndexGrid();
        
        Bounds surfaceBounds = m_bounds.clone();
        surfaceBounds.setVoxelSize(m_bounds.getVoxelSize()*m_surfaceVoxelSize);
        m_surfaceBuilder = new AttributedTriangleMeshSurfaceBuilder(surfaceBounds);        
        m_surfaceBuilder.setDataDimension(m_dataDimension);
        m_surfaceBuilder.initialize();

        m_shellBuilder = new PointSetShellBuilder();
        
        m_shellBuilder.setShellHalfThickness(m_shellHalfThickness);

        if(m_useDistanceRange) m_maxDistanceVoxels = Math.max(abs(m_maxDistance), abs(m_minDistance))/m_bounds.getVoxelSize();
        else m_maxDistanceVoxels = 0.;

        return ResultCodes.RESULT_OK;
    }


    protected AttributeGrid createIndexGrid(){
        
        double vs = m_bounds.getVoxelSize();
        if(DEBUG)printf("index grid bounds: %s  voxelSize: %7.5f\n", m_bounds, vs);
        return new ArrayAttributeGridInt(m_bounds, vs, vs);

    }

    Vector3d  // work vectors
        w0 = new Vector3d(), 
        w1 = new Vector3d(), 
        w2 = new Vector3d();

    /**
       interface of AttributedTriangleCollector
     */
    public boolean addAttTri(Vec v0,Vec v1,Vec v2){
        v0.get(w0);
        v1.get(w1);
        v2.get(w2);
        m_rasterizer.addTri(w0, w1, w2);
        m_surfaceBuilder.addAttTri(v0, v1, v2);
        m_triCount++;
        return true;
    }


    /**
       Calculates distances on distanceGrid from given mesh 
       @param triProducer - the mesh 
       @param attributeColorizer colorizer which converts point attributes into grid data channel
       @param outGrid grid to contain distances to the mesh 
     */
    public void getAttributedDistances(AttributedTriangleProducer triProducer, DataSource attributeColorizer, AttributeGrid outGrid){
        
        if(DEBUG)printf("AttributedDistanceRasterizer.getAttributedDistances(grid) threadCount: %d\n", m_threadCount);
        long t0 = time();

        init();
        triProducer.getAttTriangles(this);
        if(DEBUG_TIMING)printf("AttributedDistanceRasterizer..getTriangles(this) time: %d ms\n", (time() - t0));

        int pcount = m_surfaceBuilder.getPointCount();
        if(DEBUG)printf("pcount: %d\n", pcount);
        
        double pnt[][] = new double[m_dataDimension][pcount];
        t0 = time();

        m_surfaceBuilder.getPoints(pnt);
        if(false){
            int n = Math.min(pnt[0].length, 100);
            for(int k = 0; k < n; k++){
                for(int d = 0; d < pnt.length; d++){
                    printf("%8.5f ", pnt[d][k]);
                }                
                printf("\n");
            }
        }
        m_shellBuilder.setPoints(new PointSetCoordArrays(pnt[0], pnt[1], pnt[2]));
        m_shellBuilder.setShellHalfThickness(m_shellHalfThickness);

        t0 = time();
        m_shellBuilder.execute(m_indexGrid);

        if(DEBUG_TIMING)printf("m_shellBuilder.execute() %d ms\n", (time() - t0));

        AttributeGrid interiorGrid = new GridMask(gridX,gridY,gridZ);
        
        t0 = time();
        m_rasterizer.getRaster(interiorGrid);

        if(DEBUG_TIMING)printf("m_rasterizer.getRaster(interiorGrid) time: %d ms\n", (time() - t0));

        t0 = time();

        
        // initially point are in grid units 
        ClosestPointIndexer.getPointsInGridUnits(m_indexGrid, pnt[0], pnt[1], pnt[2]);

        // distribute indices on the whole indexGrid                
        if(m_threadCount <= 1) {
            ClosestPointIndexer.PI3_bounded(pnt[0], pnt[1], pnt[2], m_maxDistanceVoxels, m_indexGrid);
            if(DEBUG_TIMING)printf("ClosestPointIndexer.PI3_sorted time: %d ms\n", (time() - t0));
        } else {
            ClosestPointIndexerMT.PI3_MT(pnt[0], pnt[1], pnt[2], m_maxDistanceVoxels, m_indexGrid, m_threadCount);
            if(DEBUG_TIMING)printf("ClosestPointIndexerMT.PI3_MT time: %d ms\n", (time() - t0));
        }
        
        t0 = time();
        // transform points into world units
        ClosestPointIndexer.getPointsInWorldUnits(m_indexGrid, pnt[0], pnt[1], pnt[2]);
        if(DEBUG_TIMING)printf("ClosestPointIndexer.getPointsInWorldUnits(): %d ms\n", (time() - t0));
        
        t0 = time();
        if(m_threadCount <= 1) {

            ClosestPointIndexer.makeAttributedDistanceGrid(m_indexGrid, pnt, interiorGrid, m_minDistance, m_maxDistance, attributeColorizer,outGrid);
            //ClosestPointIndexer.makeDistanceGrid(m_indexGrid, pnt[0], pnt[1], pnt[2], interiorGrid, m_maxInDistance, m_maxOutDistance, distanceGrid);

            if(DEBUG_TIMING)printf("ClosestPointIndexer.makeAttributedDistanceGrid() time: %d ms\n", (time() - t0));
        } else {

            printf("calling ClosestPointIndexerMT.makeAttributedDistanceGrid_MT()\n");            
            ClosestPointIndexerMT.makeAttributedDistanceGrid_MT(m_indexGrid, pnt, interiorGrid, m_minDistance, m_maxDistance, attributeColorizer,m_threadCount, outGrid);
            if(DEBUG_TIMING)printf("ClosestPointIndexerMT.makeAttributedDistanceGrid_MT() time: %d ms\n", (time() - t0));
        }
       
    } // getDistances()

}