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

import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.GridMask;

import abfab3d.grid.op.PointSetShellBuilder;
import abfab3d.grid.op.ClosestPointIndexer;
import abfab3d.grid.op.ClosestPointIndexerMT;


import abfab3d.datasources.TransformableDataSource;

import abfab3d.param.Parameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.ObjectParameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.IntParameter;

import static java.lang.Math.floor;
import static java.lang.Math.min;
import static java.lang.Math.abs;
import static abfab3d.core.MathUtil.lerp3;
import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.getDistance;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;
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
public class DistanceToMeshDataSource extends TransformableDataSource implements TriangleCollector {

    static final boolean DEBUG = true;
    static final double DEFAULT_VOXEL_SIZE = 0.2*MM;
    static final double HALF = 0.5;
    static final double UNDEFINED = 0;  // value of voxels with undefined index 
    static final int INTERIOR_VALUE = 1; // interior value for interior grid 
    
    static final public int INTERPOLATION_BOX = 0;
    static final public int INTERPOLATION_LINEAR = 1;
    
    static final public int INTERPOLATION_NEAREST_NEIGHBOR = 2;
    static final public int INTERPOLATION_BEST_NEIGHBOR = 3;
    static final double MAX_DISTANCE_UNDEFINED = 1.e10;

    // relative voxel size for surface rasterization 
    //double m_surfaceVoxelSize = 1.;
    // half thickness of initial shell around the mesh (in voxels )
    
    ObjectParameter mp_meshProducer = new ObjectParameter("meshProducer", "mesh producer", null);
    DoubleParameter mp_voxelSize = new DoubleParameter("voxelSize", "size of rasterization voxel", DEFAULT_VOXEL_SIZE);
    DoubleParameter mp_surfaceVoxelSize = new DoubleParameter("surfaceVoxelSize", "surface voxel size", 1.);
    DoubleParameter mp_margins = new DoubleParameter("margins", "width of margins around model", DEFAULT_VOXEL_SIZE);
    DoubleParameter mp_maxDistance = new DoubleParameter("maxDistance", "max distance to calculate", MAX_DISTANCE_UNDEFINED);
    DoubleParameter mp_shellHalfThickness = new DoubleParameter("shellHalfThickness", "shell half thickness (in voxels)", 1);
    BooleanParameter mp_attributeLoading = new BooleanParameter("attributeLoading", "Load attribute data",false);
    BooleanParameter mp_useMultiPass = new BooleanParameter("useMultiPass", "use Multi Pass algorithm in distance sweeping",false);
    IntParameter mp_interpolationType = new IntParameter("interpolationType", "0 - nearest neighbor, 1 - best neighbor",INTERPOLATION_BOX);

    protected long m_maxGridSize = 1000L*1000L*1000L;
    protected long m_minGridSize = 1000L;

    Parameter[] m_aparams = new Parameter[]{
        mp_meshProducer,
        mp_voxelSize,
        mp_margins,
        mp_maxDistance, 
        mp_shellHalfThickness,
        mp_attributeLoading,
        mp_useMultiPass,
        mp_surfaceVoxelSize,
        mp_interpolationType,
    };

    
    public DistanceToMeshDataSource(TriangleProducer meshProducer){

        super.addParams(m_aparams);

        mp_meshProducer.setValue(meshProducer);

    }

    
    /**
       
     */
    public int initialize(){
        
        super.initialize();
        long t0 = time();
        TriangleProducer producer = (TriangleProducer)mp_meshProducer.getValue();

        int threadCount = 8;
        // find mesh bounds
        Bounds gridBounds = getGridBounds(producer);
        super.setBounds(gridBounds);
        m_maxDistance = mp_maxDistance.getValue();
        if(m_maxDistance == MAX_DISTANCE_UNDEFINED)         
            m_maxDistance = gridBounds.getSizeMax()/2;        
        m_interpolationType = mp_interpolationType.getValue();

        int gridDim[] = gridBounds.getGridSize();
        
        m_voxelSize = gridBounds.getVoxelSize();
        m_gridScale = 1./m_voxelSize;
        
        m_gridDimX1 = gridDim[0]-1;
        m_gridDimY1 = gridDim[1]-1;
        m_gridDimZ1 = gridDim[2]-1;
        m_gridMinX = gridBounds.xmin;
        m_gridMinY = gridBounds.ymin;
        m_gridMinZ = gridBounds.zmin;
        if(DEBUG)printf("m_gridMinX:(%7.2f, %7.2f, %7.2f)mm\n",m_gridMinX/MM,m_gridMinY/MM,m_gridMinZ/MM);

        m_rasterizer = new MeshRasterizer(gridBounds, gridDim[0],gridDim[1],gridDim[2]);
        m_rasterizer.setInteriorValue(INTERIOR_VALUE);
        
        Bounds surfaceBounds = gridBounds.clone();
        surfaceBounds.setVoxelSize(gridBounds.getVoxelSize() * mp_surfaceVoxelSize.getValue());
        m_surfaceBuilder = new TriangleMeshSurfaceBuilder(surfaceBounds);        
        m_surfaceBuilder.initialize();

        producer.getTriangles(this);

        int pcount = m_surfaceBuilder.getPointCount();
        if(DEBUG)printf("DistanceToMeshDataSource pcount: %d\n", pcount);
        m_pntx = new double[pcount];
        m_pnty = new double[pcount];
        m_pntz = new double[pcount];

        m_surfaceBuilder.getPoints(m_pntx, m_pnty, m_pntz);

        PointSetShellBuilder shellBuilder = new PointSetShellBuilder(); 
        shellBuilder.setShellHalfThickness(mp_shellHalfThickness.getValue());
        shellBuilder.setPoints(new PointSetCoordArrays(m_pntx, m_pnty, m_pntz));
        shellBuilder.setShellHalfThickness(mp_shellHalfThickness.getValue());

        // create index grid 
        m_indexGrid = createIndexGrid(gridBounds);
        // thicken surface points into thin layer 
        shellBuilder.execute(m_indexGrid);

        // create interior grid 
        m_interiorGrid = new GridMask(gridDim[0],gridDim[1],gridDim[2]);        
        m_rasterizer.getRaster(m_interiorGrid);
        printf("surface building time: %d ms\n", time() - t0);

        double maxDistanceVoxels = m_maxDistance/m_voxelSize;
        

        if(maxDistanceVoxels > mp_shellHalfThickness.getValue()){
            t0 = time();
            // spread distances to the whole grid 
            ClosestPointIndexer.getPointsInGridUnits(m_indexGrid, m_pntx, m_pnty, m_pntz);
            if(mp_useMultiPass.getValue()){
                ClosestPointIndexerMT.PI3_multiPass_MT(m_pntx, m_pnty, m_pntz, maxDistanceVoxels, m_indexGrid, threadCount);
            } else {
                ClosestPointIndexerMT.PI3_MT(m_pntx, m_pnty, m_pntz, maxDistanceVoxels, m_indexGrid, threadCount);
            }        
            ClosestPointIndexer.getPointsInWorldUnits(m_indexGrid, m_pntx, m_pnty, m_pntz);
            printf("distance sweeping time: %d ms\n", time() - t0);
        }
        return ResultCodes.RESULT_OK;
    }


    protected AttributeGrid createIndexGrid(Bounds bounds){
        //TODO - select appropriate grid to create
        return new ArrayAttributeGridInt(bounds, bounds.getVoxelSize(),bounds.getVoxelSize());

    }

    // z-buffer rasterizer to get mesh interior 
    MeshRasterizer m_rasterizer;     
    // triangles rasterizer 
    TriangleMeshSurfaceBuilder m_surfaceBuilder;
    // builder of shell around rasterized points 
    PointSetShellBuilder m_shellBuilder;
    // coordinates of points 
    double 
        m_pntx[], 
        m_pnty[], 
        m_pntz[]; 
    // on/off grid of interior voxels 
    AttributeGrid m_interiorGrid;
    // indices of closest point to each voxel 
    AttributeGrid m_indexGrid;

    // dimensions of grid 
    int m_gridDimX1,m_gridDimY1,m_gridDimZ1;    
    // grid origin 
    double m_gridMinX,m_gridMinY,m_gridMinZ;
    // scale to convert coord into voxels 
    double m_gridScale;    
    // voxel size 
    double m_voxelSize;
    // maximal distace to store in the grid 
    protected double m_maxDistance;
    // interpolation to use between voxels 
    protected int m_interpolationType = INTERPOLATION_BEST_NEIGHBOR;

    /**
       interface of triangle consumer 
     */
    public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){

        m_rasterizer.addTri(v0, v1, v2);
        m_surfaceBuilder.addTri(v0, v1, v2);
        //m_triCount++;
        return true;

    }


    Bounds getGridBounds(TriangleProducer producer){

        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        producer.getTriangles(bb);

        double margins = mp_margins.getValue();
        double voxelSize = mp_voxelSize.getValue();
        Bounds meshBounds = bb.getBounds(); 
        Bounds gridBounds = meshBounds.clone();
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
        if(DEBUG){
            printf("DistanceToMeshDataSource()  grid:[%d x %d x %d] voxelSize: %7.3f mm\n",ng[0],ng[1],ng[2],voxelSize/MM);
            printf("DistanceToMeshDataSource()  meshBounds: (%7.3f %7.3f; %7.3f %7.3f; %7.3f %7.3f) mm)\n",
                   meshBounds.xmin/MM,meshBounds.xmax/MM,meshBounds.ymin/MM,meshBounds.ymax/MM,meshBounds.zmin/MM,meshBounds.zmax/MM);
            printf("DistanceToMeshDataSource()  gridBounds: (%7.3f %7.3f; %7.3f %7.3f; %7.3f %7.3f) mm)\n",
                   gridBounds.xmin/MM,gridBounds.xmax/MM,gridBounds.ymin/MM,gridBounds.ymax/MM,gridBounds.zmin/MM,gridBounds.zmax/MM);
        }                
        return gridBounds;
    }


    public int getBaseValue(Vec pnt, Vec data){
        
        switch(m_interpolationType){
        default: 
        case INTERPOLATION_BEST_NEIGHBOR:
            getValueBestNeighbor(pnt, data);
            break;

        case INTERPOLATION_NEAREST_NEIGHBOR:
            getValueNearestNeighbor(pnt, data);
            break;
        case INTERPOLATION_BOX:
            getValueBox(pnt, data);
            break;

        case INTERPOLATION_LINEAR:
            getValueLinear(pnt, data);
            break;
        }
        return ResultCodes.RESULT_OK;
    }

    /**
       calculates distance to arbitrary pnt(x,y,z) as closest distance to points closest to 8 voxels centers
     */
    public int getValueBestNeighbor(Vec pnt, Vec data){

        double 
            x = pnt.v[0],
            y = pnt.v[1],
            z = pnt.v[2];

        double 
            gx = (pnt.v[0] - m_gridMinX)*m_gridScale - HALF,
            gy = (pnt.v[1] - m_gridMinY)*m_gridScale - HALF,
            gz = (pnt.v[2] - m_gridMinZ)*m_gridScale - HALF;
        int 
            ix = clamp((int)floor(gx), 0, m_gridDimX1),
            iy = clamp((int)floor(gy), 0, m_gridDimY1),
            iz = clamp((int)floor(gz), 0, m_gridDimZ1);
        double dist = distance(ix, iy, iz, x,y,z);
        dist = bestDist(dist, distance(ix+1, iy,   iz, x,y,z));
        dist = bestDist(dist, distance(ix,   iy+1, iz, x,y,z));
        dist = bestDist(dist, distance(ix+1, iy+1, iz, x,y,z));
        dist = bestDist(dist, distance(ix+1, iy,   iz+1, x,y,z));
        dist = bestDist(dist, distance(ix,   iy+1, iz+1, x,y,z));
        dist = bestDist(dist, distance(ix+1, iy+1, iz+1, x,y,z));
        
        data.v[0] = dist;
    
        return ResultCodes.RESULT_OK;

    }

    /**
       calculates distance to arbitrary pnt(x,y,z) as distance to center of nearest voxel 
     */
    public int getValueBox(Vec pnt, Vec data){

        double 
            x = pnt.v[0],
            y = pnt.v[1],
            z = pnt.v[2];

        int  
            ix = (int)((pnt.v[0] - m_gridMinX)*m_gridScale),
            iy = (int)((pnt.v[1] - m_gridMinY)*m_gridScale),
            iz = (int)((pnt.v[2] - m_gridMinZ)*m_gridScale);

        double d000 = distance(ix, iy, iz);
        
        data.v[0] = d000;
    
        return ResultCodes.RESULT_OK;

    }

    /**
       calculates distance to arbitrary pnt(x,y,z) as linear interpolation of distances to centers of 8 nearest voxel 
     */
    public int getValueLinear(Vec pnt, Vec data){

        double 
            x = pnt.v[0],
            y = pnt.v[1],
            z = pnt.v[2];

        double   
            gx = (pnt.v[0] - m_gridMinX)*m_gridScale - HALF,
            gy = (pnt.v[1] - m_gridMinY)*m_gridScale  - HALF,
            gz = (pnt.v[2] - m_gridMinZ)*m_gridScale - HALF;
        int 
            ix = (int)gx,
            iy = (int)gy,
            iz = (int)gz;

        double 
            dx = gx - ix,
            dy = gy - iy,
            dz = gz - iz;

        double 
            d000 = distance(ix  , iy,   iz),
            d100 = distance(ix+1, iy,   iz),
            d110 = distance(ix+1, iy+1, iz),
            d010 = distance(ix  , iy+1, iz),
            d001 = distance(ix  , iy,   iz+1),
            d101 = distance(ix+1, iy,   iz+1),
            d111 = distance(ix+1, iy+1, iz+1),
            d011 = distance(ix  , iy+1, iz+1);

        data.v[0] = lerp3(d000,d100,d010,d110,d001,d101,d011,d111,dx, dy, dz);
           
        return ResultCodes.RESULT_OK;

    }

    /**
       calculates distance to arbitrary pnt(x,y,z) as distance to point closest to the nearest voxel center
     */
    public int getValueNearestNeighbor(Vec pnt, Vec data){

        double 
            x = pnt.v[0],
            y = pnt.v[1],
            z = pnt.v[2];
        int 
            ix = (int)((pnt.v[0] - m_gridMinX)*m_gridScale),
            iy = (int)((pnt.v[1] - m_gridMinY)*m_gridScale),
            iz = (int)((pnt.v[2] - m_gridMinZ)*m_gridScale);

        data.v[0] = distance(ix, iy, iz, x, y, z);

        return ResultCodes.RESULT_OK;

    }

    /**
       calculates distance from nearest point to (ix, iy, iz) to (x, y, z)
     */
    double distance(int ix, int iy, int iz, double x, double y, double z){
        ix = clamp(ix, 0, m_gridDimX1);
        iy = clamp(iy, 0, m_gridDimY1);
        iz = clamp(iz, 0, m_gridDimZ1);
        int ind = (int)m_indexGrid.getAttribute(ix, iy, iz);
        int sign = 1;
        if(m_interiorGrid != null && m_interiorGrid.getAttribute(ix, iy, iz) != 0)
            sign = -1;
        if(ind == UNDEFINED){
            return sign*m_maxDistance;
        } else {
            return sign*getDistance(x,y,z, m_pntx[ind],m_pnty[ind],m_pntz[ind]);
        }
    } 
    
    /**
       return distance associated with voxel ix, iy, iz 
     */
    double distance(int ix, int iy, int iz){

        ix = clamp(ix, 0, m_gridDimX1);
        iy = clamp(iy, 0, m_gridDimY1);
        iz = clamp(iz, 0, m_gridDimZ1);

        int ind = (int)m_indexGrid.getAttribute(ix, iy, iz);
        int sign = 1;
        if(m_interiorGrid != null && m_interiorGrid.getAttribute(ix, iy, iz) != 0)
            sign = -1;
        if(ind == UNDEFINED){
            return sign*m_maxDistance;
        } else {
            double x = (ix+HALF)*m_voxelSize + m_gridMinX;
            double y = (iy+HALF)*m_voxelSize + m_gridMinY;
            double z = (iz+HALF)*m_voxelSize + m_gridMinZ;            
            return sign*getDistance(x,y,z, m_pntx[ind],m_pnty[ind],m_pntz[ind]);
        }        
    }
 
    static final double bestDist(double dist1, double dist2){
        if(abs(dist1) < abs(dist2)){
            return dist1;
        } else {
            return dist2;
        }
    }
    
}