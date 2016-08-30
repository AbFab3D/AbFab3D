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

import static java.lang.Math.floor;
import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.getDistance;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;


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
    static final double UNDEFINED = 0;
    
    // relative voxel size for surface rasterization 
    double m_surfaceVoxelSize = 1.;
    // half thickness of initial shell around the mesh (in voxels )
    double m_shellHalfThickness = 1.0;

    ObjectParameter mp_meshProducer = new ObjectParameter("meshProducer", "mesh producer", null);
    DoubleParameter mp_voxelSize = new DoubleParameter("voxelSize", "size of rasterization voxel", DEFAULT_VOXEL_SIZE);
    DoubleParameter mp_margins = new DoubleParameter("margins", "width of margins around model", DEFAULT_VOXEL_SIZE);
    BooleanParameter mp_attributeLoading = new BooleanParameter("attributeLoading", "Load attribute data",false);
    BooleanParameter mp_useMultiPass = new BooleanParameter("useMultipass", "use Multi Pass algorithm in distance sweeping",false);

    protected long m_maxGridSize = 1000L*1000L*1000L;
    protected long m_minGridSize = 1000L;

    Parameter[] m_aparams = new Parameter[]{
        mp_meshProducer,
        mp_voxelSize,
        mp_margins,
        
        mp_attributeLoading,
        mp_useMultiPass,
    };

    public DistanceToMeshDataSource(TriangleProducer meshProducer){

        super.addParams(m_aparams);

        mp_meshProducer.setValue(meshProducer);

    }


    public int initialize(){
        
        super.initialize();
               
        TriangleProducer producer = (TriangleProducer)mp_meshProducer.getValue();

        int maxDistanceVoxels = 2000;
        int threadCount = 4;
        // find mesh bounds
        Bounds gridBounds = getGridBounds(producer);
        int gridDim[] = gridBounds.getGridSize();

        m_gridDimX = gridDim[0];
        m_gridDimY = gridDim[1];
        m_gridDimZ = gridDim[2];
        m_gridDimX1 = gridDim[0]-1;
        m_gridDimY1 = gridDim[1]-1;
        m_gridDimZ1 = gridDim[2]-1;
        m_gridMinX = gridBounds.xmin;
        m_gridMinY = gridBounds.ymin;
        m_gridMinZ = gridBounds.zmin;
        
        m_rasterizer = new MeshRasterizer(gridBounds, gridDim[0],gridDim[1],gridDim[2]);
        m_rasterizer.setInteriorValue(1);
        
        Bounds surfaceBounds = gridBounds.clone();
        surfaceBounds.setVoxelSize(gridBounds.getVoxelSize() * m_surfaceVoxelSize);
        m_surfaceBuilder = new TriangleMeshSurfaceBuilder(surfaceBounds);        
        m_surfaceBuilder.initialize();

        producer.getTriangles(this);

        int pcount = m_surfaceBuilder.getPointCount();

        m_pntx = new double[pcount];
        m_pnty = new double[pcount];
        m_pntz = new double[pcount];

        m_surfaceBuilder.getPoints(m_pntx, m_pnty, m_pntz);

        PointSetShellBuilder shellBuilder = new PointSetShellBuilder();        
        shellBuilder.setShellHalfThickness(m_shellHalfThickness);
        shellBuilder.setPoints(new PointSetCoordArrays(m_pntx, m_pnty, m_pntz));
        shellBuilder.setShellHalfThickness(m_shellHalfThickness);

        // create index grid 
        m_indexGrid = createIndexGrid(gridBounds);
        // thicken surface points into thin layer 
        shellBuilder.execute(m_indexGrid);

        // create interior grid 
        m_interiorGrid = new GridMask(gridDim[0],gridDim[1],gridDim[2]);        
        m_rasterizer.getRaster(m_interiorGrid);

        // spread distances to the whole grid 
        ClosestPointIndexer.getPointsInGridUnits(m_indexGrid, m_pntx, m_pnty, m_pntz);
        if(mp_useMultiPass.getValue()){
            ClosestPointIndexerMT.PI3_multiPass_MT(m_pntx, m_pnty, m_pntz, maxDistanceVoxels, m_indexGrid, threadCount);
        } else {
            ClosestPointIndexerMT.PI3_MT(m_pntx, m_pnty, m_pntz, maxDistanceVoxels, m_indexGrid, threadCount);
        }        
        ClosestPointIndexer.getPointsInWorldUnits(m_indexGrid, m_pntx, m_pnty, m_pntz);

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
    int m_gridDimX,m_gridDimY,m_gridDimZ,m_gridDimX1,m_gridDimY1,m_gridDimZ1;
    // grid origin 
    double m_gridMinX,m_gridMinY,m_gridMinZ;
    // maximal distace to store in the grid 
    double m_maxDistance;
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
            printf("DistanceToMeshDataSource()  gridBounds: (%7.3f %7.3f; %7.3f %7.3f; %7.3f %7.3f) mm)\n",
                   gridBounds.xmin/MM,gridBounds.xmax/MM,gridBounds.ymin/MM,gridBounds.ymax/MM,gridBounds.zmin/MM,gridBounds.zmax/MM);
        }                
        return gridBounds;
    }

    
    public int getBaseValue(Vec pnt, Vec data){

        double 
            x = pnt.v[0],
            y = pnt.v[1],
            z = pnt.v[2];

        double 
            gx = (pnt.v[0] - m_gridMinX)*m_gridDimX + HALF,
            gy = (pnt.v[1] - m_gridMinY)*m_gridDimY + HALF,
            gz = (pnt.v[2] - m_gridMinZ)*m_gridDimZ + HALF;
        int 
            ix = clamp((int)floor(gx), 0, m_gridDimX1),
            iy = clamp((int)floor(gy), 0, m_gridDimY1),
            iz = clamp((int)floor(gz), 0, m_gridDimZ1);
        int ind = (int)m_indexGrid.getAttribute(ix, iy, iz);
        if(ind == UNDEFINED){
            data.v[0] = m_maxDistance;
        } else {
            data.v[0] = getDistance(x,y,z, m_pntx[ind],m_pnty[ind],m_pntz[ind]);; 
        }
        return ResultCodes.RESULT_OK;

    }
    
}