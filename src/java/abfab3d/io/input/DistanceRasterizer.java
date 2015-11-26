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

import abfab3d.geom.ZBuffer;
import abfab3d.util.TriangleCollector;
import abfab3d.util.TriangleProducer;
import abfab3d.util.Bounds;
import abfab3d.util.Initializable;
import abfab3d.util.DataSource;

import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.AttributeChannel;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.GridMask;

import abfab3d.grid.op.ClosestPointIndexer;
import abfab3d.grid.op.ClosestPointIndexerMT;

import abfab3d.grid.util.GridUtil;

import abfab3d.geom.TriangleMeshShellBuilder;


import static abfab3d.util.Units.MM;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.MathUtil.step10;


/**
   creates antialiased rasterization using distance to mesh calculations
   
   distance is calculated in thin shell around the mesh 

 */
public class DistanceRasterizer implements TriangleCollector {

    // this is used purely for precision of distance calculations on distance grid    
    long m_subvoxelResolution=100;
    // size of grid 
    int gridX,gridY,gridZ;

    MeshRasterizer m_rasterizer;     
    TriangleMeshShellBuilder m_shellBuilder;
    Bounds m_bounds;
    AttributeGrid m_indexGrid;
    double m_maxInDistance = 1.*MM;
    double m_maxOutDistance = 1*MM;
    double m_maxDistance;
    protected int m_threadCount = 1;
    int m_estimatedPoints;

    int m_triCount = 0;

    // half thickness of initial shell around the mesh (in voxels )
    double m_shellHalfThickness = 1.0;

    public DistanceRasterizer(Bounds bounds, int gridX, int gridY, int gridZ){
        
        this.gridX = gridX;
        this.gridY = gridY;
        this.gridZ = gridZ;
        this.m_bounds = bounds.clone();
        
    }

    public void setMaxInDistance(double value){
        m_maxInDistance = value;
    }

    public void setMaxOutDistance(double value){

        m_maxOutDistance = value;

    }

    public void setThreadCount(int threadCount){

        m_threadCount = threadCount;

    }


    public void setShellHalfThickness(double value){  

        m_shellHalfThickness = value;

    }

    public void setEstimatePoints(int val) {
        m_estimatedPoints = val;
    }

    //public void setSubvoxelResolution(long value){        
        //m_subvoxelResolution = value;
    //}

    protected int initialize(){

        m_rasterizer = new MeshRasterizer(m_bounds, gridX, gridY, gridZ);
        m_rasterizer.setInteriorValue(1);

        m_indexGrid = createIndexGrid();
        m_shellBuilder = new TriangleMeshShellBuilder(m_indexGrid, m_subvoxelResolution,m_estimatedPoints);
        
        m_shellBuilder.setShellHalfThickness(m_shellHalfThickness);
        printf("calling m_shellBuilder.initialize()\n");
        m_shellBuilder.initialize();

        return DataSource.RESULT_OK;
    }


    protected AttributeGrid createIndexGrid(){
        
        double vs = m_bounds.getVoxelSize();
        printf("index grid bounds: %s  voxelSize: %7.5f\n", m_bounds, vs);
        return new ArrayAttributeGridInt(m_bounds, vs, vs);

    }

    /**
       interface of triangle consumer 
     */
    public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){

        m_rasterizer.addTri(v0, v1, v2);
        m_shellBuilder.addTri(v0, v1, v2);
        m_triCount++;
        return true;
    }

    public void getDistances(TriangleProducer triProducer, AttributeGrid distanceGrid){

        printf("DistanceRasterizer.getDistances(grid)\n");
        long t0 = time();
        initialize();
        triProducer.getTriangles(this);
        printf("triProducer.getTriangles(this) time: %d ms\n", (time() - t0));

        AttributeGrid interiorGrid = new GridMask(gridX,gridY,gridZ);
        
        t0 = time();
        m_rasterizer.getRaster(interiorGrid);
        printf("m_rasterizer.getRaster(interiorGrid) time: %d ms\n", (time() - t0));

        int pcount = m_shellBuilder.getPointCount();
        printf("generated points count: %d\n", pcount);

        t0 = time();

        double pntx[] = new double[pcount];
        double pnty[] = new double[pcount];
        double pntz[] = new double[pcount];
        m_shellBuilder.getPointsInGridUnits(pntx, pnty, pntz);
        printf("getPointsInGridUnits.  time: %d ms\n", (time() - t0));
        t0 = time();

        //if(true) return indexGrid;

        // distribute indices on the whole indexGrid        
        if(m_threadCount <= 1) {
            ClosestPointIndexer.PI3_sorted(pntx, pnty, pntz, m_indexGrid);
            printf("ClosestPointIndexer.PI3_sorted time: %d ms\n", (time() - t0));
        } else {
            ClosestPointIndexerMT.PI3_MT(pntx, pnty, pntz, m_indexGrid, m_threadCount);
            printf("ClosestPointIndexerMT.PI3_MT time: %d ms\n", (time() - t0));
        }

        t0 = time();
        // transform points into world units
        ClosestPointIndexer.getPointsInWorldUnits(m_indexGrid, pntx, pnty, pntz);
        printf("ClosestPointIndexer.getPointsInWorldUnits(): %d ms\n", (time() - t0));
        
        t0 = time();
        ClosestPointIndexer.makeDistanceGrid(m_indexGrid, 
                                             pntx, pnty, pntz, 
                                             interiorGrid, 
                                             distanceGrid, 
                                             m_maxInDistance, 
                                             m_maxOutDistance);
        printf("ClosestPointIndexer.makeDistanceGrid time: %d ms\n", (time() - t0));
       
    }


    public void getDensity(TriangleProducer triProducer, AttributeGrid densityGrid){

        printf("DistanceRasterizer.getRaster(grid)\n");
        long t0 = time();

        long t1 = time();
        initialize();        
        printf("DistanceRasterizer  initialize() %d ms\n", (time() - t1));
        t1 = time();
        t0 = t1;
        triProducer.getTriangles(this);
        //triProducer.getTriangles(m_rasterizer);
        //printf("triProducer.getTriangles(m_rasterizer) %d ms\n", (time() - t1));
        //t1 = time();
        //triProducer.getTriangles(m_shellBuilder);
        //printf("triProducer.getTriangles(m_shellBuilder) %d ms\n", (time() - t1));
        printf("DistanceRasterizer  getTriangles() %d ms\n", (time() - t0));
        
        int pcount = m_shellBuilder.getPointCount();
        printf("pcount: %d\n", pcount);
        double pntx[] = new double[pcount];
        double pnty[] = new double[pcount];
        double pntz[] = new double[pcount];
        t0 = time();
        m_shellBuilder.getPoints(pntx, pnty, pntz);
        printf("m_shellBuilder.getPoints() %d ms\n", (time() - t0));
        t0 = time();
        
        AttributeGrid interiorGrid = new GridMask(gridX,gridY,gridZ);

        m_rasterizer.getRaster(interiorGrid);
        printf("m_rasterizer.getRaster(interiorGrid) %d ms\n", (time() - t0));
        //makeDensityFromDistance(m_shellBuilder.getDistanceGrid(), interiorGrid, densityGrid );
        t0 = time();
        ClosestPointIndexer.makeDensityGrid(m_indexGrid, pntx, pnty, pntz,
                                           interiorGrid, densityGrid, densityGrid.getAttributeDesc().getChannel(0));
        printf("ClosestPointIndexer.makeDensityGrid() %d ms\n", (time() - t0));
        if(false){
            int z = interiorGrid.getDepth()/2;
            printf("interior grid:\n");
            GridUtil.printSliceAttribute(interiorGrid, z);
            printf("distanceGrid:\n");
            GridUtil.printSliceAttribute(m_shellBuilder.getDistanceGrid(), z);
            printf("indexGrid:\n");
            GridUtil.printSliceAttribute(m_indexGrid, z);
            printf("densityGrid:\n");
            GridUtil.printSliceAttribute(densityGrid, z);
        }

    }

    
    void makeDensityFromDistance(AttributeGrid distGrid, AttributeGrid interiorGrid, AttributeGrid densityGrid){

        int 
            nx = distGrid.getWidth(),
            ny = distGrid.getHeight(),
            nz = distGrid.getDepth();

        AttributeChannel dataChannel = densityGrid.getAttributeDesc().getChannel(0);

        double voxelSize = distGrid.getVoxelSize();        
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    long dd = distGrid.getAttribute(x,y,z);
                    double dist = (voxelSize*dd) / m_subvoxelResolution;
                    if(interiorGrid != null && interiorGrid.getAttribute(x,y,z) != 0){
                        dist = -dist;
                    }

                    //printf("att: %d dist: %5.1f mm\n", dd, dist/MM);
                    double density = step10(dist, voxelSize);
                    long att = dataChannel.makeAtt(density);
                    densityGrid.setAttribute(x,y,z,att);
                }
            }
        }        
    }    
}