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
   2) generates interior/exterio grid using z-buffer voxelization 
   3) initialies thin layer of voxel around surface to index closest point on surface 
   4) sweeps thin layer to the whole grid of closes point indixes

   during calculation it find the closest voxel to the given point and uses 
   that voxel closest point index to calculate the actual euclidean distance  

   
   @author Vladimir Bulatov
   
 */
public class DistanceToMeshDataSource extends TransformableDataSource {

    static final boolean DEBUG = false;
    static final double DEFAULT_VOXEL_SIZE = 0.2*MM;
    static final double DEFAULT_SURFACE_VOXEL_SIZE  = 0.5;
    static final double DEFAULT_SHELL_HALF_THICKNESS = 2.6;

    static final long INTERIOR_MASK = IndexedDistanceInterpolator.INTERIOR_MASK;

    static final int INTERIOR_VALUE = 1; // interior value for interior grid 
    
    static final public int INTERPOLATION_BOX = IndexedDistanceInterpolator.INTERPOLATION_BOX;
    static final public int INTERPOLATION_LINEAR = IndexedDistanceInterpolator.INTERPOLATION_LINEAR;
    //static final public int INTERPOLATION_COMBINED = IndexedDistanceInterpolator.INTERPOLATION_COMBINED;
    
    static final double MAX_DISTANCE_UNDEFINED = 1.e10;
    
    SNodeParameter mp_meshProducer = new SNodeParameter("meshProducer", "mesh producer", null);
    ObjectParameter mp_meshColorizer = new ObjectParameter("meshColorizer", "mesh colorizer", null);
    DoubleParameter mp_voxelSize = new DoubleParameter("voxelSize", "size of rasterization voxel", DEFAULT_VOXEL_SIZE);
    DoubleParameter mp_surfaceVoxelSize = new DoubleParameter("surfaceVoxelSize", "surface voxel size", 1.);
    DoubleParameter mp_margins = new DoubleParameter("margins", "width of margins around model", DEFAULT_VOXEL_SIZE);
    DoubleParameter mp_maxDistance = new DoubleParameter("maxDistance", "max distance to calculate", MAX_DISTANCE_UNDEFINED);
    DoubleParameter mp_shellHalfThickness = new DoubleParameter("shellHalfThickness", "shell half thickness (in voxels)", 1);
    BooleanParameter mp_attributeLoading = new BooleanParameter("attributeLoading", "Load attribute data",false);
    BooleanParameter mp_useMultiPass = new BooleanParameter("useMultiPass", "use Multi Pass algorithm in distance sweeping",false);
    IntParameter mp_interpolationType = new IntParameter("interpolationType", "0 - box, 1 - linear",INTERPOLATION_LINEAR);
    BooleanParameter mp_useThinLayer = new BooleanParameter("useThinLayer", "use thin layer representation",false);
    BooleanParameter mp_extendDistance = new BooleanParameter("extendDistance", "whether to extend distance outside of grid",true);
    DoubleParameter mp_thinLayerHalfThickness = new DoubleParameter("thinLayerHalfThickness", "half thickness of thin layer (in voxels)",2.0);

    protected long m_maxGridSize = 1000L*1000L*1000L;
    protected long m_minGridSize = 1000L;
    protected double m_maxDistance;

    Parameter[] m_aparams = new Parameter[]{
        mp_meshProducer,
        mp_meshColorizer,
        mp_voxelSize,
        mp_margins,
        mp_maxDistance, 
        mp_shellHalfThickness,
        mp_attributeLoading,
        mp_useMultiPass,
        mp_surfaceVoxelSize,
        mp_interpolationType,
        mp_useThinLayer,
        mp_thinLayerHalfThickness,
        mp_extendDistance
    };

    protected String m_savedParamString = "";
    protected String m_currentParamString = "";
    
    protected Bounds m_meshBounds;

    // interpolator used to calculate distances 
    IndexedDistanceInterpolator m_distCalc;
    
    public DistanceToMeshDataSource(Object meshProducer){

        super.addParams(m_aparams);

        mp_meshProducer.setValue(meshProducer);

    }

    public DistanceToMeshDataSource(Object meshProducer, DataSource colorizer){

        super.addParams(m_aparams);

        mp_meshProducer.setValue(meshProducer);
        mp_meshColorizer.setValue(colorizer);

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
    public Bounds getMeshBounds(){
        
        if(m_meshBounds == null)
            initialize();
            
        return m_meshBounds;

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
            cd.meshBounds = m_meshBounds;            
            ParamCache.getInstance().put(m_currentParamString, cd);

        } else {

            if(DEBUG) printf("%s : got cached\n", this);
            // init from chached data 
            m_distCalc = cd.distCalc;
            m_meshBounds = cd.meshBounds;
            m_bounds = cd.bounds;
        }
        return ResultCodes.RESULT_OK;

    }

    /**
       real non cached initialziation 
       makes all distance calculations here 
    */
    protected int fullInitialize(){
        
        if(DEBUG)printf("DistanceToMeshDataSo.initialize() - full calculation\n"); 

        
        long t0 = time();
        Object producer = mp_meshProducer.getValue();

        if(producer instanceof AttributedTriangleProducer){
            AttributedTriangleProducer atp = (AttributedTriangleProducer)producer;
            if(atp.getDataDimension() == 3) 
                return initPlainMesh((TriangleProducer)producer);            
            else 
                return initAttributedMesh((AttributedTriangleProducer)producer);
        } else if(producer instanceof TriangleProducer){
            return initPlainMesh((TriangleProducer)producer);            
        }

        throw new RuntimeException(fmt("don't know how to handle mesh %s",producer));
    }

    protected int initAttributedMesh(AttributedTriangleProducer atProducer){

        if(DEBUG)printf("%s.initAttributedMesh(%s)\n", getClass().getName(),atProducer);

        int threadCount = 8;
        // find mesh bounds
        Bounds gridBounds = calculateGridBounds(BoundingBoxCalculator.getBounds(atProducer));
        super.setBounds(gridBounds);

        if(DEBUG)printf("gridBounds: %s\n", gridBounds);
        double maxDistance = getMaxDistance(gridBounds);
        DataSource meshColorizer = (DataSource)mp_meshColorizer.getValue();
        IndexedDistanceInterpolator distData = makeAttributedDistanceInterpolator(atProducer, meshColorizer,
                                                                                  gridBounds, maxDistance, 
                                                                                  mp_surfaceVoxelSize.getValue(), 
                                                                                  mp_shellHalfThickness.getValue(),
                                                                                  false, // preserveZero 
                                                                                  mp_useMultiPass.getValue(), 
                                                                                  mp_extendDistance.getValue(),
                                                                                  threadCount);
        
        m_distCalc = distData;
        super.m_channelsCount = m_distCalc.getChannelsCount();
        
        m_savedParamString = m_currentParamString;
        
        return ResultCodes.RESULT_OK;
        
    }


    /**
       only use local params without transforms
     */
    public String getLocalParamString(){

        return getParamString(getClass().getSimpleName(), m_aparams);
    } 

    protected int initPlainMesh(TriangleProducer producer){

        int threadCount = 8;
        // find mesh bounds
        Bounds gridBounds = calculateGridBounds(BoundingBoxCalculator.getBounds(producer));
        super.setBounds(gridBounds);
        double maxDistance = getMaxDistance(gridBounds);
        //TODO - use it 
        int interpolationType = mp_interpolationType.getValue();
        
        IndexedDistanceInterpolator distData = makeDistanceInterpolator(producer, gridBounds, maxDistance, 
                                                                        mp_surfaceVoxelSize.getValue(), 
                                                                        mp_shellHalfThickness.getValue(),
                                                                        false, // preserveZero 
                                                                        mp_useMultiPass.getValue(), 
                                                                        mp_extendDistance.getValue(),
                                                                        threadCount);
        //TODO
        // handle mutli resolution case 
        
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
       creates distance interpolator for given triangle mesh 
       @param producer triangle mesh
       @param gridBounds for generated
       @param maxDistance maximal distance to calculate 
     */
    static IndexedDistanceInterpolator makeDistanceInterpolator(TriangleProducer producer, 
                                                                Bounds gridBounds, 
                                                                double maxDistance, 
                                                                double surfaceVoxelSize,
                                                                double shellHalfThickness,
                                                                boolean preserveZero,
                                                                boolean useMultiPass, 
                                                                boolean extendDistance, 
                                                                int threadCount
                                                                ){
        long t0 = time();
        int gridDim[] = gridBounds.getGridSize();
        // z-buffer rasterizer to get mesh interior 
        MeshRasterizer interiorRasterizer = new MeshRasterizer(gridBounds, gridDim[0],gridDim[1],gridDim[2]);
        interiorRasterizer.setInteriorValue(INTERIOR_VALUE);
                
        Bounds surfaceBounds = gridBounds.clone();
        double voxelSize = gridBounds.getVoxelSize();

        // surface voxel ratio = volumeVoxelSize/surfaceVoxelSize
        int svratio = Math.max(1, (int)Math.round(1./surfaceVoxelSize));
        // surface voxel size 
        double svs = gridBounds.getVoxelSize()/svratio;
        surfaceBounds.setVoxelSize(svs);
        if((svratio & 1) == 0){ // even ratio
            double shift = svs/2;
            // shift grid of surface rasterization by half voxel to align centers of surface grid with center of volume grid
            surfaceBounds.translate(shift,shift,shift);
        }

        // triangles rasterizer         
        TriangleMeshSurfaceBuilder surfaceBuilder = new TriangleMeshSurfaceBuilder(surfaceBounds);        
        
        surfaceBuilder.initialize();

        // aggregator of 2 triangle collectors 
        TC2 tc2 = new TC2(interiorRasterizer, surfaceBuilder);
        
        // get mesh from producer 
        producer.getTriangles(tc2);

        int pcount = surfaceBuilder.getPointCount();
        if(DEBUG)printf("DistanceToMeshDataSource pcount: %d\n", pcount);
        double pnts[][] = new double[3][pcount];
        surfaceBuilder.getPoints(pnts[0], pnts[1], pnts[2]);
        
        // builder of shell around rasterized points 
        PointSetShellBuilder shellBuilder = new PointSetShellBuilder(); 
        shellBuilder.setShellHalfThickness(shellHalfThickness);
        shellBuilder.setPoints(new PointSetCoordArrays(pnts[0], pnts[1], pnts[2]));
        shellBuilder.setShellHalfThickness(shellHalfThickness);

        // create index grid 
        AttributeGrid indexGrid = createIndexGrid(gridBounds, voxelSize);
        // thicken surface points into thin layer 
        shellBuilder.execute(indexGrid);

        // create interior grid 
        AttributeGrid interiorGrid = new GridMask(gridDim[0],gridDim[1],gridDim[2]);        

        interiorRasterizer.getRaster(interiorGrid);
        printf("surface building time: %d ms\n", time() - t0);

        double maxDistanceVoxels = maxDistance/voxelSize;       
        if(maxDistanceVoxels > shellHalfThickness){
            t0 = time();
            // spread distances to the whole grid 
            ClosestPointIndexer.getPointsInGridUnits(indexGrid, pnts[0], pnts[1], pnts[2]);
            ClosestPointIndexerMT.PI3_MT(pnts[0], pnts[1], pnts[2], maxDistanceVoxels, indexGrid, threadCount, useMultiPass);
            ClosestPointIndexer.getPointsInWorldUnits(indexGrid, pnts[0], pnts[1], pnts[2]);
            printf("distance sweeping time: %d ms\n", time() - t0);
        }
        
        setInteriorMask(indexGrid, interiorGrid, INTERIOR_MASK, preserveZero);

        return new IndexedDistanceInterpolator(pnts, indexGrid, maxDistance, extendDistance);        

    }

    static IndexedDistanceInterpolator makeAttributedDistanceInterpolator(AttributedTriangleProducer atProducer, 
                                                                          DataSource meshColorizer,
                                                                          Bounds gridBounds, 
                                                                          double maxDistance, 
                                                                          double surfaceVoxelSize,
                                                                          double shellHalfThickness,
                                                                          boolean preserveZero,
                                                                          boolean useMultiPass, 
                                                                          boolean extendDistance, 
                                                                          int threadCount
                                                                          ){
        long t0 = time();
        int gridDim[] = gridBounds.getGridSize();
        // z-buffer rasterizer to get mesh interior 
        MeshRasterizer interiorRasterizer = new MeshRasterizer(gridBounds, gridDim[0],gridDim[1],gridDim[2]);
        interiorRasterizer.setInteriorValue(INTERIOR_VALUE);
        int dataDimension = atProducer.getDataDimension();        
        if(DEBUG) printf("dataDimension: %d\n", dataDimension);
        Bounds surfaceBounds = gridBounds.clone();
        double voxelSize = gridBounds.getVoxelSize();

        // surface voxel ratio = volumeVoxelSize/surfaceVoxelSize
        int svratio = Math.max(1, (int)Math.round(1./surfaceVoxelSize));
        // surface voxel size 
        double svs = gridBounds.getVoxelSize()/svratio;
        surfaceBounds.setVoxelSize(svs);
        if((svratio & 1) == 0){ // even ratio
            double shift = svs/2;
            // shift grid of surface rasterization by half voxel to align centers of surface grid with center of volume grid
            surfaceBounds.translate(shift,shift,shift);
        }

        // triangles rasterizer         
        AttributedTriangleMeshSurfaceBuilder aSurfaceBuilder = new AttributedTriangleMeshSurfaceBuilder(surfaceBounds);        
        aSurfaceBuilder.setDataDimension(dataDimension);
        aSurfaceBuilder.initialize();

        // aggregator of 2 triangle collectors 
        TC2A tc2a = new TC2A(interiorRasterizer, aSurfaceBuilder);
        
        // get mesh from producer 
        atProducer.getAttTriangles(tc2a);
        
        int pcount = aSurfaceBuilder.getPointCount();
        if(DEBUG)printf("DistanceToMeshDataSource pcount: %d\n", pcount);
        //int pntsDimension = Math.max(dataDimension,6); 
        int pntsDimension = 6; //xyz + rgb
        double pnts[][] = new double[pntsDimension][pcount];
        aSurfaceBuilder.getPoints(pnts);
        
        // builder of shell around rasterized points 
        PointSetShellBuilder shellBuilder = new PointSetShellBuilder(); 
        shellBuilder.setShellHalfThickness(shellHalfThickness);
        shellBuilder.setPoints(new PointSetCoordArrays(pnts[0], pnts[1], pnts[2]));
        shellBuilder.setShellHalfThickness(shellHalfThickness);

        // create index grid 
        AttributeGrid indexGrid = createIndexGrid(gridBounds, voxelSize);
        // thicken surface points into thin layer 
        shellBuilder.execute(indexGrid);

        // create interior grid 
        AttributeGrid interiorGrid = new GridMask(gridDim[0],gridDim[1],gridDim[2]);        

        interiorRasterizer.getRaster(interiorGrid);
        printf("surface building time: %d ms\n", time() - t0);

        double maxDistanceVoxels = maxDistance/voxelSize;       
        if(maxDistanceVoxels > shellHalfThickness){
            t0 = time();
            // spread distances to the whole grid 
            ClosestPointIndexer.getPointsInGridUnits(indexGrid, pnts[0], pnts[1], pnts[2]);
            ClosestPointIndexerMT.PI3_MT(pnts[0], pnts[1], pnts[2], maxDistanceVoxels, indexGrid, threadCount, useMultiPass);
            ClosestPointIndexer.getPointsInWorldUnits(indexGrid, pnts[0], pnts[1], pnts[2]);
            printf("distance sweeping time: %d ms\n", time() - t0);
        }
        
        setInteriorMask(indexGrid, interiorGrid, INTERIOR_MASK, preserveZero);
        if(meshColorizer != null){
            if(DEBUG)printf("(meshColorizer != null) conversion from texture coord to colors\n");

            Vec tex = new Vec(3);
            Vec color = new Vec(3);            
            for(int i = 1; i < pcount; i++){
                switch(dataDimension){
                case 6: tex.v[2] = pnts[5][i]; // no break here
                case 5: tex.v[1] = pnts[4][i];
                case 4: tex.v[0] = pnts[3][i];
                }

                meshColorizer.getDataValue(tex, color);
                // store rgb color in place of texture coordinates
                pnts[5][i] = color.v[2]; // no break here
                pnts[4][i] = color.v[1];
                pnts[3][i] = color.v[0];
            }        
        } else {
            if(DEBUG)printf("(meshColorizer == null) leaving texture coordinates as is\n");
        }
        
        return new IndexedDistanceInterpolator(pnts, indexGrid, maxDistance, extendDistance, pntsDimension); 

    }

    /**
       set mask bits into attributes of grid if interior grid value != 0
       it is used to store information interior and value info in single grid 
       caller is responsible that mask will fit into voxel storage size 
       @param grid grid to add mask to value 
       @param interior grid of inerior voxels 
       @param mask - bit mask to set if voxel is interior 
       @param preserveZero set interior bits even if original voxel value is 0
     */
    static public void setInteriorMask(AttributeGrid grid, AttributeGrid interior, long mask, boolean preserveZero){

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        boolean ignoreZero = !preserveZero;

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    if(interior.getAttribute(x,y,z) != 0) {
                        // interior 
                        long a = grid.getAttribute(x,y,z);
                        if(ignoreZero || (a != 0)) {                            
                            a |= mask;
                            grid.setAttribute(x,y,z,a);
                        }
                    }                        
                }
            }
        }
    }

    static AttributeGrid createIndexGrid(Bounds bounds, double voxelSize){

        //TODO - select here appropriate grid to create         
        return new ArrayAttributeGridInt(bounds, voxelSize,voxelSize);

    }
    
    
    protected Bounds calculateGridBounds(Bounds bounds){
        
        m_meshBounds = bounds;

        double margins = mp_margins.getValue();
        double voxelSize = mp_voxelSize.getValue();
        Bounds gridBounds = m_meshBounds.clone();
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
            printf("DistanceToMeshDataSource()  grid:[%d x %d x %d] voxelSize: %7.3f mm\n",ng[0],ng[1],ng[2],voxelSize/MM);
            printf("                      meshBounds: (%s)\n",m_meshBounds);
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


    /**
       aggregator for 2 triangle collector 
     */
    static class TC2 implements TriangleCollector {
        
        TriangleCollector tc1;
        TriangleCollector tc2;

        TC2(TriangleCollector tc1, TriangleCollector tc2){
            this.tc1 = tc1;
            this.tc2 = tc2;
            
        }
        
        /**
           interface of triangle consumer 
        */
        public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){
            
            tc1.addTri(v0, v1, v2);
            tc2.addTri(v0, v1, v2);
            return true;
            
        }
    } // class TC2 

    // aggregator for TriangleCollector and AttributedTriangleCollector 

    static class TC2A implements AttributedTriangleCollector {
        
        TriangleCollector m_tc1;
        AttributedTriangleCollector m_atc2;
        
        // work variables 
        Vector3d m_p0 = new Vector3d();
        Vector3d m_p1 = new Vector3d();
        Vector3d m_p2 = new Vector3d();

        TC2A(TriangleCollector tc1, AttributedTriangleCollector atc2){
            m_tc1 = tc1;
            m_atc2 = atc2;            
        }
        
        /**
           interface of triangle consumer 
        */
        public boolean addAttTri(Vec v0,Vec v1,Vec v2){
            
            v0.get(m_p0);
            v1.get(m_p1);
            v2.get(m_p2);

            m_tc1.addTri(m_p0, m_p1, m_p2); 
            m_atc2.addAttTri(v0, v1, v2);
            return true;
            
        }
    } // class TC2A 


    static class CachedData {

        IndexedDistanceInterpolator distCalc;
        Bounds meshBounds;
        Bounds bounds;
        
    }

} // class DistanceToMeshDataSource