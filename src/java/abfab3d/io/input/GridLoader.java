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

import abfab3d.core.Bounds;
import abfab3d.core.TriangleProducer;
import abfab3d.core.AttributedTriangleProducer;
import abfab3d.core.VecTransform;
import abfab3d.core.DataSource;
import abfab3d.core.Initializable;

import abfab3d.mesh.AreaCalculator;

import abfab3d.util.AbFab3DGlobals;
import abfab3d.util.BoundingBoxCalculator;
import abfab3d.util.AttributedTriangleProducerConverter;
import abfab3d.util.MeshRasterizer;


import abfab3d.grid.op.DistanceTransformLayered;

import abfab3d.core.AttributeGrid;
import abfab3d.core.GridDataChannel;
import abfab3d.core.GridDataDesc;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.ArrayAttributeGridShort;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;
import static abfab3d.core.MathUtil.getMaxValue;

/**
   class to load mesh or grid from a file into a grid 

   @author Vladimir Bulatov
 */
public class GridLoader {

    static final boolean DEBUG = false;
    static final boolean DEBUG_TIMING = false;
    
    protected double m_preferredVoxelSize = 0.2*MM;

    protected int m_densityBitCount = 8;
    protected int m_distanceBitCount = 16;
    protected int m_colorBitCount = 5;
    protected double m_margins = 1*MM;

    protected long m_maxGridSize = 1000L*1000L*1000L;
    protected long m_minGridSize = 1000L;

    protected AttributeGrid m_densityGridTemplate = new ArrayAttributeGridByte(1,1,1, 10*MM, 10*MM);
    protected AttributeGrid m_distanceGridTemplate = new ArrayAttributeGridShort(1,1,1, 10*MM, 10*MM);
    protected AttributeGrid m_distRGBGridTemplate = new ArrayAttributeGridInt(1,1,1, 10*MM, 10*MM);

    public static final int 
        RASTERIZER_WAVELET = 1,   // makes antialised density and makes distance from density 
        RASTERIZER_DISTANCE = 2,  // exact distances in thin shell only
        RASTERIZER_DISTANCE2 = 3, // exact distance in thin shell and fast sweeping to the whole grid 
        RASTERIZER_ZBUFFER = 4;   // non-antialised density rasterizer
    // names used for algorithms
    public static String sm_algNames[] = new String[]{
        "unknown",
        "wavelet",
        "distance",
        "distance2",
        "zbuffer",  
    };


    protected int m_densityAlgorithm = RASTERIZER_WAVELET;
    protected int m_distanceAlgorithm = RASTERIZER_DISTANCE2;
    protected double m_maxOutDistance = 2*MM;
    protected double m_maxInDistance = 2*MM;
    protected double m_shellHalfThickness = 2;
    protected int m_threadCount = 1;
    protected boolean m_useMultiPass = false;
    protected int m_triangleCount;
    // size of surface voxel (in grid voxel units)
    protected double m_surfaceVoxelSize = 1;



    /**

     */
    public GridLoader(){
        
    }
        
    public static String[] getDensityAlgorithmNames(){
        return sm_algNames;
    }
    
    public static String[] getDistanceAlgorithmNames(){
        return sm_algNames;
    }

    /**
       convert alg name inot alg ID
     */
    public static int getAlgorithmId(String algorithmName){
        for(int i = 0; i < sm_algNames.length; i++){
            if(sm_algNames[i].equalsIgnoreCase(algorithmName))
                return i;
        }
        return -1;
    }

    public static String getAlgorithmName(int algorithm){
        switch(algorithm){
        default: 
            return "Unknow Algorithm";
            
        case RASTERIZER_WAVELET: return  "RASTERIZER_WAVELET";
        case RASTERIZER_DISTANCE: return "RASTERIZER_DISTANCE"; 
        case RASTERIZER_DISTANCE2: return "RASTERIZER_DISTANCE2"; 
        case RASTERIZER_ZBUFFER:  return "RASTERIZER_ZBUFFER";
        }
    }
    
    public void setThreadCount(int count){
        if (count < 1) {
            count = Runtime.getRuntime().availableProcessors();
        }

        int max_threads = ((Number) AbFab3DGlobals.get(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY)).intValue();
        if (count > max_threads)
            count = max_threads;

        m_threadCount = count;
    }

    /**
       forces to use slower but more precise distance calculation alg
    */
    public void setUseMultiPass(boolean value){

        m_useMultiPass = value;

    }


    public void setMaxGridSize(long maxGridSize){
        m_maxGridSize = maxGridSize;
    }

    public void setMinGridSize(long minGridSize){
        m_minGridSize = minGridSize;
    }

    public void setPreferredVoxelSize(double voxelSize){
        m_preferredVoxelSize = voxelSize;
    }

    public void setMaxOutDistance(double value){
        m_maxOutDistance = value;
    }

    public void setMaxInDistance(double value){
        m_maxInDistance = value;
    }

    public void setShellHalfThickness(double value){  
        m_shellHalfThickness = value;
    }

    public void setMargins(double margins){
        m_margins = margins;
    }
    
    public void setSurfaceVoxelSize(double value){  
        m_surfaceVoxelSize = value;
    }

    public void setDensityAlgorithm(String algorithmName){
        setDensityAlgorithm(getAlgorithmId(algorithmName));
    }


    public void setDensityAlgorithm(int algorithm){
        switch(algorithm){
        default: throw new IllegalArgumentException(fmt("unknown Density Rasterization Algorithm: %d",algorithm));
        case RASTERIZER_ZBUFFER:
        case RASTERIZER_WAVELET:
        case RASTERIZER_DISTANCE:
        case RASTERIZER_DISTANCE2:
            m_densityAlgorithm = algorithm;
            break;
        }
    }

    public void setDistanceAlgorithm(String algorithmName){
        setDistanceAlgorithm(getAlgorithmId(algorithmName));
    }

    public void setDistanceAlgorithm(int algorithm){
        switch(algorithm){
        default: throw new IllegalArgumentException(fmt("unknown Distance Rasterization Algorithm: %d",algorithm));
        case RASTERIZER_WAVELET:
        case RASTERIZER_DISTANCE:
        case RASTERIZER_DISTANCE2:
            m_distanceAlgorithm = algorithm;
            break;
        }
    }
    
    /**
       set bit count to be used for density grid representation 
     */
    public void setDensityBitCount(int bitCount){
        
        m_densityBitCount = bitCount;

    }

    /**
       set bit count used for distance grid 
     */
    public void setDistanceBitCount(int bitCount){
        
        m_distanceBitCount = bitCount;

    }


    public int getTriangleCount() {

        return m_triangleCount;

    }

    public AttributeGrid loadDistanceGrid(String filepath) {
        if(DEBUG)printf("loadDistanceGrid(%s)\n",filepath);

        MeshReader reader = new MeshReader(filepath);
        return loadDistanceGrid(reader);
    }

    public AttributeGrid loadDistanceGrid(String filepath, VecTransform trans) {
        if(DEBUG)printf("loadDistanceGrid(%s)\n",filepath);

        MeshReader reader = new MeshReader(filepath);
        reader.setTransform(trans);
        return loadDistanceGrid(reader);
    }

    /**
       open mesh on given path and return distance grid to that mesh 
       
       @return distance grid to the rasterized mesh
     */
    public AttributeGrid loadDistanceGrid(TriangleProducer reader){
        
        long t0 = time();
        Bounds bounds = getModelBounds(reader);

        AreaCalculator ac = new AreaCalculator(); 
        reader.getTriangles(ac);
        if(DEBUG_TIMING)printf("Area calc: %f %d\n",ac.getArea(),(time() - t0));
        
        int nx = bounds.getGridWidth();
        int ny = bounds.getGridHeight();
        int nz = bounds.getGridDepth();
        double voxelSize = bounds.getVoxelSize();

        
        switch(m_distanceAlgorithm){
        default: 
            throw new IllegalArgumentException(fmt("unsupported Distance Rasterization Algorithm: %d",m_distanceAlgorithm));
            
        case RASTERIZER_WAVELET:
            {
                WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, nx, ny, nz);
                rasterizer.setSubvoxelResolution(getMaxValue(m_densityBitCount));        
                reader.getTriangles(rasterizer);        
                AttributeGrid densityGrid = createDensityGrid(bounds);
                rasterizer.getRaster(densityGrid);
                if(DEBUG_TIMING)printf("WaveletRasterizer() done %d ms\n", time() - t0);
                int svr = (1<<m_densityBitCount)-1;
                double maxInDist = m_maxInDistance;
                double maxOutDist = m_maxOutDistance;

                DistanceTransformLayered dt = new DistanceTransformLayered(svr, maxInDist, maxOutDist);
                dt.setThreadCount(m_threadCount);
                                
                AttributeGrid distanceGrid = dt.execute(densityGrid);
                //TODO - move this into DistanceTransformLayered
                GridDataChannel channel = new GridDataChannel(GridDataChannel.DISTANCE,"distance",voxelSize/svr, -maxInDist, maxOutDist);
                distanceGrid.setDataDesc(new GridDataDesc(channel));
                return distanceGrid;
                
            }
            

        case RASTERIZER_DISTANCE:
            {
                DistanceRasterizer rasterizer = new DistanceRasterizer(bounds, nx, ny, nz);
                
                // set params 
                rasterizer.setMaxInDistance(m_maxInDistance);
                rasterizer.setMaxOutDistance(m_maxOutDistance);                
                rasterizer.setShellHalfThickness(m_shellHalfThickness);
                rasterizer.setThreadCount(m_threadCount);
                // run rasterization
                int estimatedPoints = (int) (ac.getArea() / (voxelSize * voxelSize) * m_shellHalfThickness * 2 * 1.4);  // 40% overage to avoid allocations
                if(DEBUG)printf("Estimated points: %d\n",estimatedPoints);
                rasterizer.setEstimatePoints(estimatedPoints);
                AttributeGrid distanceGrid = createDistanceGrid(bounds);
                rasterizer.getDistances(reader, distanceGrid);
                return distanceGrid;
            }
            
        case RASTERIZER_DISTANCE2:
            {
                DistanceRasterizer2 rasterizer = new DistanceRasterizer2(bounds, nx, ny, nz);
                
                // set params 
                rasterizer.setMaxInDistance(m_maxInDistance);
                rasterizer.setMaxOutDistance(m_maxOutDistance);                
                rasterizer.setShellHalfThickness(m_shellHalfThickness);
                rasterizer.setSurfaceVoxelSize(m_surfaceVoxelSize);
                rasterizer.setThreadCount(m_threadCount);
                rasterizer.setUseMultiPass(m_useMultiPass);
                // run rasterization
                AttributeGrid distanceGrid = createDistanceGrid(bounds);
                rasterizer.getDistances(reader, distanceGrid);
                return distanceGrid;
            }
            
        }
        
    }

    /**
       open mesh on given path and return density grid for that mesh 
       
       @return ddensity grid to the rasterized mesh
     */
    public AttributeGrid loadDensityGrid(String filePath){
                
        if(DEBUG)printf("loadDensityGrid(%s)\n",filePath);
        
        MeshReader reader = new MeshReader(filePath);

        long t0 = time();
        Bounds bounds = getModelBounds(reader);
        if(DEBUG_TIMING)printf("getModelBounds(reader): %d ms\n",(time()-t0));
        
        int nx = bounds.getGridWidth();
        int ny = bounds.getGridHeight();
        int nz = bounds.getGridDepth();
        double voxelSize = bounds.getVoxelSize();
        
        AttributeGrid densityGrid = createDensityGrid(bounds);

        switch(m_densityAlgorithm){
        default: 
            throw new IllegalArgumentException(fmt("unknown Density Rasterization Algorithm: %d",m_densityAlgorithm));
            
        case RASTERIZER_DISTANCE:
            {
                t0 = time();
                DistanceRasterizer rasterizer = new DistanceRasterizer(bounds, nx, ny, nz);
                //rasterizer.setSubvoxelResolution(getMaxValue(m_densityBitCount)); 
                rasterizer.getDensity(reader, densityGrid);                
                if(DEBUG_TIMING)printf("DistanceRasterizer() done %d ms\n", time() - t0);
            }
            break;

        case RASTERIZER_DISTANCE2:
            {
                t0 = time();
                DistanceRasterizer2 rasterizer = new DistanceRasterizer2(bounds, nx, ny, nz);
                rasterizer.getDensity(reader, densityGrid);                
                if(DEBUG_TIMING)printf("DistanceRasterizer2() done %d ms\n", time() - t0);
            }
            break;

        case RASTERIZER_WAVELET:
            {
                t0 = time();
                WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, nx, ny, nz);
                rasterizer.setSubvoxelResolution(getMaxValue(m_densityBitCount));        
                reader.getTriangles(rasterizer);        
                rasterizer.getRaster(densityGrid);
                if(DEBUG_TIMING)printf("WaveletRasterizer() done %d ms\n", time() - t0);
            }
            break;

        case RASTERIZER_ZBUFFER:
            {
                t0 = time();
                MeshRasterizer rasterizer = new MeshRasterizer(bounds, nx, ny, nz);
                rasterizer.setInteriorValue(getMaxValue(m_densityBitCount));        
                reader.getTriangles(rasterizer);        
                rasterizer.getRaster(densityGrid);
                if(DEBUG_TIMING)printf("ZBufferRasterizer() done %d ms\n", time() - t0);
            }            
        }
        
        return densityGrid;
                
    }

    /**
     open mesh on given path and return density grid for that mesh

     @return ddensity grid to the rasterized mesh
     */
    public AttributeGrid loadDensityGrid(TriangleProducer reader){

        long t0 = time();
        Bounds bounds = getModelBounds(reader);
        if(DEBUG_TIMING)printf("getModelBounds(reader): %d ms\n",(time()-t0));

        int nx = bounds.getGridWidth();
        int ny = bounds.getGridHeight();
        int nz = bounds.getGridDepth();
        double voxelSize = bounds.getVoxelSize();

        AttributeGrid densityGrid = createDensityGrid(bounds);

        switch(m_densityAlgorithm){
            default:
                throw new IllegalArgumentException(fmt("unknown Density Rasterization Algorithm: %d",m_densityAlgorithm));

            case RASTERIZER_DISTANCE:
            {
                t0 = time();
                DistanceRasterizer rasterizer = new DistanceRasterizer(bounds, nx, ny, nz);
                //rasterizer.setSubvoxelResolution(getMaxValue(m_densityBitCount));
                rasterizer.getDensity(reader, densityGrid);
                if(DEBUG_TIMING)printf("DistanceRasterizer() done %d ms\n", time() - t0);
            }
            break;

            case RASTERIZER_DISTANCE2:
            {
                t0 = time();
                DistanceRasterizer2 rasterizer = new DistanceRasterizer2(bounds, nx, ny, nz);
                rasterizer.getDensity(reader, densityGrid);
                if(DEBUG_TIMING)printf("DistanceRasterizer2() done %d ms\n", time() - t0);
            }
            break;

            case RASTERIZER_WAVELET:
            {
                t0 = time();
                WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, nx, ny, nz);
                rasterizer.setSubvoxelResolution(getMaxValue(m_densityBitCount));
                reader.getTriangles(rasterizer);
                rasterizer.getRaster(densityGrid);
                if(DEBUG_TIMING)printf("WaveletRasterizer() done %d ms\n", time() - t0);
            }
            break;

            case RASTERIZER_ZBUFFER:
            {
                t0 = time();
                MeshRasterizer rasterizer = new MeshRasterizer(bounds, nx, ny, nz);
                rasterizer.setInteriorValue(getMaxValue(m_densityBitCount));
                reader.getTriangles(rasterizer);
                rasterizer.getRaster(densityGrid);
                if(DEBUG_TIMING)printf("ZBufferRasterizer() done %d ms\n", time() - t0);
            }
        }

        return densityGrid;

    }

    /**
       calculates model bonds and select approproate voxel size 
       voxel size is returned in bounds 
       @return bounds and selected voxel size of the model 
     */
    protected Bounds getModelBounds(TriangleProducer triProducer){

        //if(DEBUG)printf("getModelBounds(%s)\n", triProducer);
        long t0 = time();
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        triProducer.getTriangles(bb);
        m_triangleCount = bb.getTriangleCount();
        if(DEBUG)printf("model triangle count: %d\n", m_triangleCount);
        if(DEBUG)printf("model read time time: %d ms\n", (time() - t0));
        if(m_triangleCount <= 0) throw new RuntimeException(fmt("bad mesh triangle count: %d\n", m_triangleCount));
        Bounds modelBounds = bb.getBounds(); 
        if(DEBUG)printf("modelBounds: %s\n", modelBounds);
        Bounds gridBounds = modelBounds.clone();
        double voxelSize = m_preferredVoxelSize;

        gridBounds.setVoxelSize(voxelSize);
        gridBounds.expand(m_margins);
        
        gridBounds.roundBounds();
        
        int ng[] = gridBounds.getGridSize();
        long voxels = (long) ng[0] * ng[1]*ng[2];
        if(voxels > m_maxGridSize) {
            voxelSize = Math.pow(gridBounds.getVolume()/m_maxGridSize, 1./3);
            gridBounds.setVoxelSize(voxelSize);
            gridBounds.roundBounds();
        } else if (voxels < m_minGridSize){
            voxelSize = Math.pow(gridBounds.getVolume()/m_minGridSize, 1./3);
            gridBounds.setVoxelSize(voxelSize);
            gridBounds.roundBounds();
        }

        if(false)printf("actual voxelSize: %7.3fmm\n",voxelSize/MM);
        int nx = gridBounds.getGridWidth();
        int ny = gridBounds.getGridHeight();
        int nz = gridBounds.getGridDepth();

        if(false)printf("  grid size: [%d x %d x %d] = %d\n", nx, ny, nz, (long) nx*ny*nz);
        if(false)printf("  grid bounds: [ %8.3f, %8.3f], [%8.3f, %8.3f], [%8.3f, %8.3f] mm; vs: %5.3f mm\n",
               gridBounds.xmin/MM, gridBounds.xmax/MM, gridBounds.ymin/MM, gridBounds.ymax/MM, gridBounds.zmin/MM, gridBounds.zmax/MM, voxelSize/MM);
        
        if(nx < 2 || ny < 2 || nz < 2) throw new IllegalArgumentException(fmt("bad grid size (%d x %d x %d)\n", nx, ny, nz));
      
        return gridBounds;
    }

    protected AttributeGrid createDistanceGrid(Bounds bounds){
        
        int nx = bounds.getGridWidth();
        int ny = bounds.getGridHeight();
        int nz = bounds.getGridDepth();
        double voxelSize = bounds.getVoxelSize();
        AttributeGrid distanceGrid = (AttributeGrid)m_distanceGridTemplate.createEmpty(nx, ny, nz, voxelSize, voxelSize);
        distanceGrid.setGridBounds(bounds);
        GridDataChannel distanceChannel = new GridDataChannel(GridDataChannel.DISTANCE, "dist", m_distanceBitCount, 0, -m_maxInDistance, m_maxOutDistance);
        distanceGrid.setDataDesc(new GridDataDesc(distanceChannel));
        return distanceGrid;
    }


    protected AttributeGrid createDensityGrid(Bounds bounds){

        int nx = bounds.getGridWidth();
        int ny = bounds.getGridHeight();
        int nz = bounds.getGridDepth();
        double voxelSize = bounds.getVoxelSize();

        AttributeGrid densityGrid = (AttributeGrid)m_densityGridTemplate.createEmpty(nx, ny, nz, voxelSize, voxelSize);
        densityGrid.setGridBounds(bounds);
        GridDataChannel densityChannel = new GridDataChannel(GridDataChannel.DENSITY, "dens", m_densityBitCount, 0, 0., 1.);
        densityGrid.setDataDesc(new GridDataDesc(densityChannel));
        return densityGrid;

    }

    /**
       creates distance RGB grod for given bounds and other class members data 
     */
    protected AttributeGrid createDistRGBGrid(Bounds bounds){

        int nx = bounds.getGridWidth();
        int ny = bounds.getGridHeight();
        int nz = bounds.getGridDepth();
        double voxelSize = bounds.getVoxelSize();

        AttributeGrid grid = (AttributeGrid)m_distRGBGridTemplate.createEmpty(nx, ny, nz, voxelSize, voxelSize);
        grid.setGridBounds(bounds);  
        if(m_distanceBitCount == 16) {
            grid.setDataDesc(makeColorDesc(m_maxInDistance, m_maxOutDistance, 16, 5, 6, 5));
        } else if(m_distanceBitCount == 24) {
            grid.setDataDesc(makeColorDesc(m_maxInDistance, m_maxOutDistance, 24, 3, 3, 2));
        } else if(m_distanceBitCount == 29) {
            grid.setDataDesc(makeColorDesc(m_maxInDistance, m_maxOutDistance, 29, 1, 1, 1));
        } else {
            grid.setDataDesc(makeColorDesc(m_maxInDistance, m_maxOutDistance, 8, 8, 8, 8));
        }
        
        return grid;
        
    }

    protected GridDataDesc makeColorDesc(double maxInDist, double maxOutDist, int distBits, int redBits, int greenBits, int blueBits) {

        if(DEBUG)printf("makeColorDesc(%d, %d, %d, %d)\n", distBits, redBits, greenBits, blueBits);

        // distance channel value 0 corresponds to maxOutDIstance
        // distance channel value FF corresponds to -maxInDIstance
        // in case if (distBits == 8) this makes interior of shapes have value FF which is convenient to be used as alpha channel in 32 bit png
        int offset = 0;
        GridDataChannel dist = new GridDataChannel(GridDataChannel.DISTANCE,    "dist", distBits,  offset, maxOutDist, -maxInDist);
        offset += distBits;
        GridDataChannel red = new GridDataChannel(GridDataChannel.COLOR_RED,    "red",  redBits,   offset,    0.,1.);
        offset += redBits;
        GridDataChannel green = new GridDataChannel(GridDataChannel.COLOR_GREEN,"green",greenBits, offset, 0.,1.);
        offset += greenBits;
        GridDataChannel blue = new GridDataChannel(GridDataChannel.COLOR_BLUE,  "blue", blueBits,  offset, 0.,1.);

        return new GridDataDesc(dist, red, green, blue);

    }


    /**
        creates grid from textured triangle mesh 
        @param attTriProducer produces texured triangles via TriangleColletor2 interface
        @param attributeColorizer
        @return grid which contains distance and color information to given limits 
     */
    public AttributeGrid rasterizeAttributedTriangles(AttributedTriangleProducer attTriProducer, DataSource attributeColorizer){
        
        if(DEBUG) printf("GridLoader.rasterizeTexturedTriangles(%s)\n", attTriProducer);
        TriangleProducer tp = new AttributedTriangleProducerConverter(attTriProducer);
        if(attributeColorizer instanceof Initializable) ((Initializable)attributeColorizer).initialize();
        Bounds bounds = getModelBounds(tp);
        if(DEBUG) printf("GridLoader.model bounds: %s\n", bounds);
        int dims = attTriProducer.getDataDimension();
        AttributeGrid grid = greateOutputGrid(bounds, dims);
        if(DEBUG)printf("grid: [%d x %d x %d]  dims: %d\n", grid.getWidth(),grid.getHeight(),grid.getDepth(),dims);
        if(DEBUG)printf("voxelSize: %7.5f\n", grid.getVoxelSize()); 
        AttributedDistanceRasterizer rasterizer = new AttributedDistanceRasterizer(bounds, grid.getWidth(),grid.getHeight(),grid.getDepth());

        rasterizer.setDataDimension(dims);
        rasterizer.setShellHalfThickness(m_shellHalfThickness);
        rasterizer.setSurfaceVoxelSize(m_surfaceVoxelSize);
        rasterizer.setThreadCount(m_threadCount);
        rasterizer.setUseMultiPass(m_useMultiPass);
        rasterizer.setDistanceRange(-m_maxInDistance, m_maxOutDistance);
       
        rasterizer.getAttributedDistances(attTriProducer, attributeColorizer, grid);
                
        return grid;
        
    }

    /**
       rasterizes attributed triangles coming from mesh reader 
     */
    public AttributeGrid rasterizeAttributedTriangles(AttributedMeshReader reader){

        if(DEBUG) printf("GridLoader.rasterizeTexturedTriangles(%s)\n", reader);

        DataSource attributeColorizer = reader.getAttributeCalculator();
        if(attributeColorizer instanceof Initializable) ((Initializable)attributeColorizer).initialize();

        return rasterizeAttributedTriangles(reader,attributeColorizer);
 
    }

    /**
       creates apropriate output grid  
     */
    protected AttributeGrid greateOutputGrid(Bounds bounds, int dataDimension){
        
        switch(dataDimension) {
        case 3:
            return createDistanceGrid(bounds);
        case 5:
            return createDistRGBGrid(bounds);
        case 6:
            return createDistRGBGrid(bounds);
        default:
            throw new IllegalArgumentException("Unhandled case.  dataDimension: " + dataDimension);
        }
    }
}
