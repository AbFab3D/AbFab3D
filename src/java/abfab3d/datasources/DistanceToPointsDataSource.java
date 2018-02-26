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

import abfab3d.grid.op.PointSetShellBuilder;
import abfab3d.grid.op.ClosestPointIndexer;
import abfab3d.grid.op.ClosestPointIndexerMT;


import abfab3d.datasources.TransformableDataSource;

import abfab3d.param.SNodeParameter;
import abfab3d.param.ParamCache;
import abfab3d.param.Parameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.ObjectParameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.BaseParameterizable;

import static java.lang.Math.floor;
import static java.lang.Math.min;
import static java.lang.Math.abs;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;


/**
   
   represent distance to triangle mesh as data source 
   during initialization it does 
   1) rasterizes the mesh into set of points on its surface on the grid
   2) generates interior/exterior grid using z-buffer voxelization 
   3) initialises thin layer of voxel around surface to index closest point on surface 
   4) sweeps thin layer to the whole grid of closest point indices

   during calculation it finds the closest voxel to the given point and uses 
   that voxel closest point index to calculate the actual euclidean distance  

   
   @author Vladimir Bulatov
   
 */
public class DistanceToPointsDataSource extends TransformableDataSource {

    static final boolean DEBUG = false;
    static final double DEFAULT_VOXEL_SIZE = 0.2*MM;
    static final double DEFAULT_SURFACE_VOXEL_SIZE  = 0.5;
    static final double DEFAULT_SHELL_HALF_THICKNESS = 2.6;

    static final long INTERIOR_MASK = IndexedDistanceInterpolator.INTERIOR_MASK;

    static final int INTERIOR_VALUE = 1; // interior value for interior grid 
        
    static final double MAX_DISTANCE_UNDEFINED = 1.e10;
    
    SNodeParameter mp_pointsProducer = new SNodeParameter("pointsProducer", "points producer", null);
    DoubleParameter mp_voxelSize = new DoubleParameter("voxelSize", "size of rasterization voxel", DEFAULT_VOXEL_SIZE);
    DoubleParameter mp_margins = new DoubleParameter("margins", "width of margins around model", DEFAULT_VOXEL_SIZE);
    DoubleParameter mp_maxDistance = new DoubleParameter("maxDistance", "max distance to calculate", MAX_DISTANCE_UNDEFINED);
    DoubleParameter mp_shellHalfThickness = new DoubleParameter("shellHalfThickness", "shell half thickness (in voxels)", 1);
    BooleanParameter mp_useMultiPass = new BooleanParameter("useMultiPass", "use Multi Pass algorithm in distance sweeping",false);
    BooleanParameter mp_extendDistance = new BooleanParameter("extendDistance", "whether to extend distance outside of grid",true);

    protected long m_maxGridSize = 1000L*1000L*1000L;
    protected long m_minGridSize = 1000L;
    protected double m_maxDistance;

    Parameter[] m_aparams = new Parameter[]{
        mp_pointsProducer,
        mp_voxelSize,
        mp_margins,
        mp_maxDistance, 
        mp_shellHalfThickness,
        mp_useMultiPass,
        mp_extendDistance,
    };

    protected String m_savedParamString = "";
    protected String m_currentParamString = "";
    
    protected Bounds m_pointsBounds;

    // interpolator used to calculate distances 
    IndexedDistanceInterpolator m_distCalc;
    
    /**
       constructor with plain mesh producer 
     */
    public DistanceToPointsDataSource(Object pointsProducer){

        super.addParams(m_aparams);

        mp_pointsProducer.setValue(pointsProducer);

    }

    /**
       @return interpolator used to calculate distances 
     */
    public IndexedDistanceInterpolator getDistanceInterpolator(){
        return m_distCalc;
    }

    /**
       @return physical bounds of the mesh
     */
    public Bounds getPointsBounds(){
        
        if(m_pointsBounds == null)
            initialize();
            
        return m_pointsBounds;

    }


    /**
       @Override
     */
    public Bounds getBounds(){

        if(m_bounds == null) initialize();
        return m_bounds;

    }

    

    /**
       initialization wih caching 
     */
    public int initialize(){
        
        if(DEBUG) printf("%s.initialize()\n",this);
        super.initialize();
        m_currentParamString = getLocalParamString();
        if(!paramChanged()){
            // avoid multiple initialization 
            if(false)printf("initialize() - no change\n"); 
            return ResultCodes.RESULT_OK;
        }

        // try to get CachedData 
        CachedData cd = (CachedData)(ParamCache.getInstance().get(m_currentParamString));

        if (cd == null) {
            // non cached 
            if(DEBUG) printf("%s : non cached - full init\n",this);
            fullInitialize();
            cd = new CachedData();
            cd.distCalc = m_distCalc;
            cd.bounds = m_bounds;
            cd.pointsBounds = m_pointsBounds;            
            ParamCache.getInstance().put(m_currentParamString, cd);

        } else {

            if(DEBUG) printf("%s : got cached\n", this);
            // init from chached data 
            m_distCalc = cd.distCalc;
            m_pointsBounds = cd.pointsBounds;
            m_bounds = cd.bounds;
        }
        return ResultCodes.RESULT_OK;

    }

    /**
       real non cached initialziation 
       makes all distance calculations here 
    */
    protected int fullInitialize(){
        
        if(DEBUG)printf("DistanceToPointsDataSource.initialize() - full calculation\n"); 

        
        long t0 = time();
        Object producer = mp_pointsProducer.getValue();

        if(producer instanceof PointSet)
            return initPlainPoints((PointSet)producer);            
        throw new RuntimeException(fmt("don't know how to handle points %s",producer));
    }

    /**
       only use local params without transforms
     */
    public String getLocalParamString(){

        return getParamString(getClass().getSimpleName(), m_aparams);
    } 

    protected int initPlainPoints(PointSet points){

        int threadCount = 8;
        // find mesh bounds
        Bounds gridBounds = calculateGridBounds(BoundingBoxCalculator.getBounds(points));
        super.setBounds(gridBounds);
        double maxDistance = getMaxDistance(gridBounds);
        
        IndexedDistanceInterpolator distData = makePlainInterpolator(points, gridBounds, maxDistance, 
                                                                       mp_shellHalfThickness.getValue(),
                                                                       mp_useMultiPass.getValue(), 
                                                                       mp_extendDistance.getValue(),
                                                                       threadCount);            
        
        m_distCalc = distData;
        super.m_channelsCount = m_distCalc.getChannelsCount();
        
        m_savedParamString = m_currentParamString;
        
        return ResultCodes.RESULT_OK;
    }

    private boolean paramChanged(){
        return !m_savedParamString.equals(m_currentParamString);
        
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
    static IndexedDistanceInterpolator makePlainInterpolator(PointSet pointSet, 
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
        
    static AttributeGrid createIndexGrid(Bounds bounds, double voxelSize){

        //TODO - select here appropriate grid to create         
        return new ArrayAttributeGridInt(bounds, voxelSize,voxelSize);

    }
    
    
    protected Bounds calculateGridBounds(Bounds bounds){
        
        m_pointsBounds = bounds;

        double margins = mp_margins.getValue();
        double voxelSize = mp_voxelSize.getValue();
        Bounds gridBounds = m_pointsBounds.clone();
        gridBounds.expand(margins);
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
            printf("DistanceToPointsDataSource()  grid:[%d x %d x %d] voxelSize: %7.3f mm\n",ng[0],ng[1],ng[2],voxelSize/MM);
            printf("                      pointsBounds: (%s)\n",m_pointsBounds);
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


    static class CachedData {

        IndexedDistanceInterpolator distCalc;
        Bounds pointsBounds;
        Bounds bounds;
        
    }

} // class DistanceToPointsDataSource