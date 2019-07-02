/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.datasources;

import javax.vecmath.Vector3d;

import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Vector;

import abfab3d.core.TriangleProducer;
import abfab3d.core.AttributedTriangleProducer;
import abfab3d.core.AttributedTriangleCollector;
import abfab3d.core.DataSource;
import abfab3d.core.ResultCodes;
import abfab3d.core.Vec;
import abfab3d.core.Bounds;
import abfab3d.core.TriangleCollector;
import abfab3d.core.AttributeGrid;


import abfab3d.util.BoundingBoxCalculator;
import abfab3d.util.PointSetCoordArrays;
import abfab3d.util.MeshRasterizer;
import abfab3d.util.PointSet;

import abfab3d.util.TriangleMeshSurfaceBuilder;
import abfab3d.util.AttributedTriangleMeshSurfaceBuilder;

import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.GridMask;

import abfab3d.grid.op.SurfacePointsFinderDS;
import abfab3d.grid.op.PointSetShellBuilder;
import abfab3d.grid.op.ClosestPointIndexer;
import abfab3d.grid.op.ClosestPointIndexerMT;
import abfab3d.grid.op.GridBlock;


import abfab3d.datasources.TransformableDataSource;

import abfab3d.param.SNodeParameter;
import abfab3d.param.ParamCache;
import abfab3d.param.Parameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.ObjectParameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.BaseParameterizable;
import org.mozilla.javascript.Context;

import static java.lang.Math.floor;
import static java.lang.Math.min;
import static java.lang.Math.abs;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;


/**
   
   calculates distance to surface represented by given DataSource 
   during initialization it does 
   1) calculates points on the surface given by implicit equation f(x,y,z) = 0
   2) generates interior/exterior grid from sign of data values
   3) initialises thin layer of voxel around surface to index closest point on surface 
   4) sweeps thin layer to the whole grid of closest point indices

   during calculation it finds the closest voxel to the given point and uses 
   that voxel closest point index to calculate the actual euclidean distance  
   
   @author Vladimir Bulatov
   
 */
public class DistanceToSurface extends TransformableDataSource {

    static final boolean DEBUG = false;
    static final boolean DEBUG_TIMING = true;
    static final double DEFAULT_VOXEL_SIZE = 0.2*MM;

    static final double MAX_DISTANCE_UNDEFINED = 1.e10;
    
    SNodeParameter mp_source = new SNodeParameter("source", "data source", null);
    DoubleParameter mp_voxelSize = new DoubleParameter("voxelSize", "size of rasterization voxel", DEFAULT_VOXEL_SIZE);
    DoubleParameter mp_surfaceVoxelSize = new DoubleParameter("surfaceVoxelSize", "relative size of voxel used for surface calculations", 1);
    DoubleParameter mp_maxDistance = new DoubleParameter("maxDistance", "max distance to calculate", MAX_DISTANCE_UNDEFINED);
    DoubleParameter mp_shellHalfThickness = new DoubleParameter("shellHalfThickness", "shell half thickness (in voxels)", 1);
    BooleanParameter mp_useMultiPass = new BooleanParameter("useMultiPass", "use Multi Pass algorithm in distance sweeping",false);
    BooleanParameter mp_extendDistance = new BooleanParameter("extendDistance", "whether to extend distance outside of grid",true);
    DoubleParameter mp_xmin = new DoubleParameter("xmin", "xmin", 0);
    DoubleParameter mp_xmax = new DoubleParameter("xmax", "xmax", 0);
    DoubleParameter mp_ymin = new DoubleParameter("ymin", "ymin", 0);
    DoubleParameter mp_ymax = new DoubleParameter("ymax", "ymax", 0);
    DoubleParameter mp_zmin = new DoubleParameter("zmin", "zmin", 0);
    DoubleParameter mp_zmax = new DoubleParameter("zmax", "zmax", 0);

    protected long m_maxGridSize = 1000L*1000L*1000L;
    protected long m_minGridSize = 1000L;

    Parameter[] m_aparams = new Parameter[]{
        mp_source,
        mp_voxelSize,
        mp_maxDistance,
        mp_surfaceVoxelSize,
        mp_shellHalfThickness,
        mp_useMultiPass,
        mp_extendDistance,
        mp_xmin,
        mp_xmax,
        mp_ymin,
        mp_ymax,
        mp_zmin,
        mp_zmax,
    };

    protected String m_savedParamString = "";
    protected String m_currentParamString = "";
    
    // interpolator used to calculate distances 
    IndexedDistanceInterpolator m_distCalc;

    /**
       constructor with plain mesh producer 
     */
    public DistanceToSurface(DataSource source, Bounds bounds){

        super.addParams(m_aparams);
        setSource(source);
        setBounds(bounds);

    }

    public void setSource(DataSource source){
        set("source", source);
    }

    public void setBounds(Bounds bounds){
        super.setBounds(bounds);
        set("xmin", bounds.xmin);
        set("xmax", bounds.xmax);
        set("ymin", bounds.ymin);
        set("ymax", bounds.ymax);
        set("zmin", bounds.zmin);
        set("zmax", bounds.zmax);
    }

    /**
       @return interpolator used to calculate distances 
     */
    public IndexedDistanceInterpolator getDistanceInterpolator(){
        return m_distCalc;
    }

    protected boolean m_cachingEnabled = true;

    public void setCaching(boolean value){
        m_cachingEnabled = value;
    }
    

    /**
       initialization with caching 
     */
    public int initialize(){
        
        if(DEBUG) printf("%s.initialize()  vs: %6.2f mm\n",this,mp_voxelSize.getValue()/MM);
        super.initialize();

        
        String label = getLocalParamString();
        
        // try to get CachedData 
        if(m_cachingEnabled){

            CachedData cd = (CachedData)(ParamCache.getInstance().get(label));            
            if (cd == null) {
                // non cached 
                if(DEBUG) printf("%s : non cached - full init\n",this);                
                initializeUncached();                
                cd = new CachedData(m_distCalc, m_bounds);
                ParamCache.getInstance().put(label, cd);                
            } else {
                
                if(DEBUG) printf("%s : got cached\n", this);
                // init from cached data 
                m_distCalc = cd.distCalc;
                setBounds(cd.bounds);
            }
        } else {            
            initializeUncached();                
        }
        return ResultCodes.RESULT_OK;

    }

    /**
       real non cached initialziation 
       makes all distance calculations here 
    */
    protected int initializeUncached(){
        
        if(DEBUG)printf("DistanceToPointsDataSource.initialize() - full calculation\n"); 

        
        long t0 = time();
        DataSource source = (DataSource)mp_source.getValue();
        initDistance(getSurfacePoints());
        if(DEBUG_TIMING) printf("DistanceToSurface.initializeUncached() time: %d ms\n", (time() - t0));
        return ResultCodes.RESULT_OK;
    }

    /**
       only use local params without transforms
     */
    public String getLocalParamString(){

        return getParamString(getClass().getSimpleName(), m_aparams);
    } 

    protected PointSet getSurfacePoints(){

        SurfacePointsFinderDS finder = new SurfacePointsFinderDS();

        double vs = mp_voxelSize.getValue()*mp_surfaceVoxelSize.getValue();
        if (DEBUG) printf("DistanceToSurface.getSurfacePoints vs: %6.2f mm\n",vs);
        finder.set("voxelSize",vs);
        DataSource source = (DataSource)mp_source.getValue();
        Bounds bounds  = makeBounds();

        PointSet points = finder.findSurfacePoints(source, bounds);
        return points;
        
    }

    protected Bounds makeBounds(){
        return new Bounds(mp_xmin.getValue(),mp_xmax.getValue(),mp_ymin.getValue(),mp_ymax.getValue(),mp_zmin.getValue(),mp_zmax.getValue());
    }


    protected int initDistance(PointSet points){
        
        int threadCount = getThreadCount();
        // find mesh bounds
        Bounds gridBounds = calculateGridBounds(makeBounds());
        super.setBounds(gridBounds);
        double maxDistance = getMaxDistance(gridBounds);
        
        IndexedDistanceInterpolator distData = makeDistanceInterpolator(points, gridBounds, maxDistance, 
                                                                       mp_shellHalfThickness.getValue(),
                                                                       mp_useMultiPass.getValue(), 
                                                                       mp_extendDistance.getValue(),
                                                                       threadCount);            
        

        setInteriorMask(distData.getIndexGrid(), (DataSource)mp_source.getValue(), IndexedDistanceInterpolator.INTERIOR_MASK);
        

        m_distCalc = distData;

        super.m_channelsCount = m_distCalc.getChannelsCount();
        
        m_savedParamString = m_currentParamString;
        
        return ResultCodes.RESULT_OK;
    }
    
    int getThreadCount(){
        //TODO 
        return 8;
    }

    /**
       return max distance from param (if is is undefined return half size dof the bounds) 
     */
    protected double getMaxDistance(Bounds bounds){
        
        double maxDistance = mp_maxDistance.getValue();
        if(maxDistance == MAX_DISTANCE_UNDEFINED)         
            maxDistance = bounds.getSizeMax()/2;        
        return maxDistance;
    }



    /**
       creates interpolator for mesh without interior
     */
    static IndexedDistanceInterpolator makeDistanceInterpolator(PointSet pointSet, 
                                                               Bounds gridBounds, 
                                                               double maxDistance, 
                                                               double shellHalfThickness,
                                                               boolean useMultiPass, 
                                                               boolean extendDistance, 
                                                               int threadCount
                                                               ){
        long t0 = time();
        int gridDim[] = gridBounds.getGridSize();
                
        Bounds surfaceBounds = gridBounds.clone();
        double voxelSize = gridBounds.getVoxelSize();

        int pcount = pointSet.size();

        double pnts[][] = new double[3][pcount];
        
        getPoints(pointSet, pnts[0], pnts[1], pnts[2]);
        
        // builder of thin shell around points 
        PointSetShellBuilder shellBuilder = new PointSetShellBuilder(); 
        shellBuilder.setShellHalfThickness(shellHalfThickness);
        shellBuilder.setPoints(new PointSetCoordArrays(pnts[0], pnts[1], pnts[2]));
        shellBuilder.setShellHalfThickness(shellHalfThickness);

        // create index grid 
        AttributeGrid indexGrid = createIndexGrid(gridBounds, voxelSize);
        // thicken surface points into thin layer 
        shellBuilder.execute(indexGrid);

        if(DEBUG)printf("surface building time: %d ms\n", time() - t0);

        double maxDistanceVoxels = maxDistance/voxelSize;       
        if(maxDistanceVoxels > shellHalfThickness){
            t0 = time();
            // spread distances to the whole grid 
            ClosestPointIndexer.getPointsInGridUnits(indexGrid, pnts[0], pnts[1], pnts[2]);
            ClosestPointIndexerMT.PI3_MT(pnts[0], pnts[1], pnts[2], maxDistanceVoxels, indexGrid, threadCount, useMultiPass);
            ClosestPointIndexer.getPointsInWorldUnits(indexGrid, pnts[0], pnts[1], pnts[2]);
            if(DEBUG)printf("distance sweeping time: %d ms\n", time() - t0);
        }
        return new IndexedDistanceInterpolator(pnts, indexGrid, maxDistance, extendDistance);        

    }
        
    /**
       set mask bits into attributes of grid if interior grid value != 0
       it is used to store information on interior and value info in single grid 
       @param grid grid to add mask to value 
       @param interior signed distance data source which describes the interior (negative data value means point is in the interior) 
       @param mask - bit mask to set if voxel is interior (it is normally the sign bit (1<<31)
     */
    void setInteriorMask(AttributeGrid grid, DataSource interior, long mask){
        long t0 = time();
        int threads = getThreadCount();
        if(threads > 1) 
            setInteriorMaskMT(grid, interior, mask, threads);
        else 
            setInteriorMaskST(grid, interior, mask);

        if(DEBUG_TIMING)printf("DistanceToSurface.setInteriorMask()[%d x %d x %d] time: %d ms\n", 
                               grid.getWidth(), grid.getHeight(), grid.getDepth(),(time() - t0));
    }

    void setInteriorMaskST(AttributeGrid grid, DataSource interior, long mask){
        long t0 = time();
        //TODO make code MT 
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        Vec pnt = new Vec(3);
        Vec interiorData = new Vec(4);

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){                    
                    grid.getWorldCoords( x, y, z, pnt.v);
                    interior.getDataValue(pnt,interiorData);                    
                    if(interiorData.v[0] < 0.) {
                        // interior 
                        long a = grid.getAttribute(x,y,z);
                        grid.setAttribute(x,y,z,(a|mask));
                    }                        
                }
            }
        }
    }
    
    static final int BLOCK_SIZE = 20;
    void setInteriorMaskMT(AttributeGrid grid, DataSource interior, long mask, int threadCount){

        Vector<GridBlock> blocks = GridBlock.makeBlocks(grid.getGridBounds(), grid.getVoxelSize(), BLOCK_SIZE, 0);

        if (DEBUG) printf("DistanceToSurface.setInteriorMaskMT(threadCount:%d)\n", threadCount);
        if (DEBUG) printf("                       blocks count: %d\n", blocks.size());
        
        
        BlockManager blockManager = new BlockManager(blocks);
        InteriorBlockProcessor threads[] = new InteriorBlockProcessor[threadCount];

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {

            InteriorBlockProcessor bp = bp = new InteriorBlockProcessor(blockManager, grid, interior, mask);

            threads[i] = bp;
            executor.submit(threads[i]);
        }

        executor.shutdown();
        long maxTime = 5; 
        try {
            executor.awaitTermination(maxTime, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class InteriorBlockProcessor implements Runnable {

        Vec pnt = new Vec(3);
        Vec interiorData = new Vec(4);
        AttributeGrid grid;
        DataSource interior;
        long mask;
        BlockManager blockManager;

        InteriorBlockProcessor(BlockManager blockManager, AttributeGrid grid, DataSource interior, long mask){
            this.mask = mask;
            this.grid = grid;
            this.interior = interior;            
            this.blockManager = blockManager;
        }
        
        void processBlock(GridBlock block){
            int xmin = block.xmin;
            int xmax = block.xmax;
            int ymin = block.ymin;
            int ymax = block.ymax;
            int zmin = block.zmin;
            int zmax = block.zmax;

            for(int y = ymin; y <= ymax; y++){
                for(int x = xmin; x <= xmax; x++){
                    for(int z = zmin; z <= zmax; z++){                    
                        grid.getWorldCoords( x, y, z, pnt.v);
                        interior.getDataValue(pnt,interiorData); 

                        if(interiorData.v[0] < 0.) {
                            // interior 
                            long a = grid.getAttribute(x,y,z);
                            grid.setAttribute(x,y,z,(a|mask));
                        }                        
                    }
                }
            }            
        }

        public void run() {
            Context.enter();

            try {
                GridBlock block;
                while ((block = blockManager.getNext()) != null) {
                    // process block
                    processBlock(block);
                }
            } catch (Throwable t) {
                printf("Error in InteriorBlockProcessor\n");
                t.printStackTrace();
            } finally {
                Context.exit();
            }
        }
    }


    /**
     * class supply next unprocessed block to the block processor
     */
    static class BlockManager {

        AtomicInteger count = new AtomicInteger(0);
        Vector<GridBlock> blocks;

        BlockManager(Vector<GridBlock> blocks) {
            this.blocks = blocks;
        }

        GridBlock getNext() {
            int nextIndex = count.getAndIncrement();
            if (nextIndex >= blocks.size())
                return null;
            else
                return blocks.get(nextIndex);
        }
    }// 

    static AttributeGrid createIndexGrid(Bounds bounds, double voxelSize){

        return new ArrayAttributeGridInt(bounds, voxelSize,voxelSize);

    }
    
    
    protected Bounds calculateGridBounds(Bounds bounds){
        

        double voxelSize = mp_voxelSize.getValue();
        Bounds gridBounds = bounds.clone();
        int ng[] = gridBounds.getGridSize(voxelSize);
        long voxels = (long) ng[0] * ng[1]*ng[2];
        double gridVolume = gridBounds.getVolume();
        if(voxels > m_maxGridSize) {
            voxelSize = Math.pow(gridVolume /m_maxGridSize, 1./3);
        } else if (voxels < m_minGridSize){
            voxelSize = Math.pow(gridVolume/m_minGridSize, 1./3);
        }
        gridBounds.setVoxelSize(voxelSize);
        gridBounds.roundBounds();
        if(DEBUG){
            printf("DistanceToSurface()  grid:[%d x %d x %d] voxelSize: %7.3f mm\n",ng[0],ng[1],ng[2],voxelSize/MM);
            printf("                      gridBounds: (%s)\n",gridBounds);
        }                
        return gridBounds;
    }


    /**
       implementation method of TransformableDataSource 
     */
    public int getBaseValue(Vec pnt, Vec data){

        if(m_distCalc != null)
            m_distCalc.getDataValue(pnt, data);

        return ResultCodes.RESULT_OK;
    }



    static void getPoints(PointSet ps, double px[], double py[], double pz[]){

        int n = ps.size();
        Vector3d p = new Vector3d();
        for(int i = 0; i < n; i++){
            ps.getPoint(i, p);
            px[i] = p.x;
            py[i] = p.y;
            pz[i] = p.z;
        }
    }


    /**
       class data stroed in cache 
     */
    static class CachedData {

        IndexedDistanceInterpolator distCalc;
        Bounds bounds;

        CachedData(IndexedDistanceInterpolator distCalc, Bounds bounds){
            this.bounds = bounds.clone();
            this.distCalc = distCalc;
        }
        
    }

} // class DistanceToSurface