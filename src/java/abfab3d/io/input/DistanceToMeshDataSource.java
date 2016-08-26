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
package abfab3d.io.input;

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

import abfab3d.geom.TriangleMeshSurfaceBuilder;

import abfab3d.grid.ArrayAttributeGridInt;

import abfab3d.grid.op.PointSetShellBuilder;


import abfab3d.datasources.TransformableDataSource;

import abfab3d.param.Parameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.ObjectParameter;
import abfab3d.param.BooleanParameter;

import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;


/**
   
   represent distance to trinagle mesh as data source 
   
 */
public class DistanceToMeshDataSource extends TransformableDataSource implements TriangleCollector {

    static final boolean DEBUG = true;
    static final double DEFAULT_VOXEL_SIZE = 0.2*MM;

    // relative voxel size for surface rasterization 
    double m_surfaceVoxelSize = 1.;
    // half thickness of initial shell around the mesh (in voxels )
    double m_shellHalfThickness = 1.0;

    ObjectParameter mp_meshProducer = new ObjectParameter("meshProducer", "mesh producer", null);
    DoubleParameter mp_voxelSize = new DoubleParameter("voxelSize", "size of rasterization voxel", DEFAULT_VOXEL_SIZE);
    DoubleParameter mp_margins = new DoubleParameter("margins", "width of margins around model", DEFAULT_VOXEL_SIZE);
    BooleanParameter mp_attributeLoading = new BooleanParameter("attributeLoading", "Load attribute data",false);

    protected long m_maxGridSize = 1000L*1000L*1000L;
    protected long m_minGridSize = 1000L;

    Parameter[] m_aparams = new Parameter[]{
        mp_voxelSize,
        mp_attributeLoading,
    };

    public DistanceToMeshDataSource(TriangleProducer meshProducer){

        super.addParams(m_aparams);

        mp_meshProducer.setValue(meshProducer);

    }


    public int initialize(){
        
        super.initialize();
               
        TriangleProducer producer = (TriangleProducer)mp_meshProducer.getValue();

        // find mesh bounds
        Bounds gridBounds = getGridBounds(producer);
        int ng[] = gridBounds.getGridSize();
        if(DEBUG)printf("grid bounds: %s\n", gridBounds);
        
        m_rasterizer = new MeshRasterizer(m_bounds, ng[0],ng[1],ng[2]);
        m_rasterizer.setInteriorValue(1);
        
        Bounds surfaceBounds = gridBounds.clone();
        surfaceBounds.setVoxelSize(gridBounds.getVoxelSize()*m_surfaceVoxelSize);
        m_surfaceBuilder = new TriangleMeshSurfaceBuilder(surfaceBounds);        
        m_surfaceBuilder.initialize();


        int pcount = m_surfaceBuilder.getPointCount();
        double pntx[] = new double[pcount];
        double pnty[] = new double[pcount];
        double pntz[] = new double[pcount];

        m_surfaceBuilder.getPoints(pntx, pnty, pntz);

        PointSetShellBuilder shellBuilder = new PointSetShellBuilder();        
        shellBuilder.setShellHalfThickness(m_shellHalfThickness);
        shellBuilder.setPoints(new PointSetCoordArrays(pntx, pnty, pntz));
        shellBuilder.setShellHalfThickness(m_shellHalfThickness);

        // create index grid 
        AttributeGrid indexGrid = createIndexGrid(gridBounds);
        shellBuilder.execute(indexGrid);

        // create interior grid 
        // rasterize surface into thin layer 
        // spread distances to the whole grid 
        return ResultCodes.RESULT_OK;
    }

    protected AttributeGrid createIndexGrid(Bounds bounds){
        return new ArrayAttributeGridInt(bounds, bounds.getVoxelSize(),bounds.getVoxelSize());
    }

    // z-buffer rasterizer to get mesh interior 
    MeshRasterizer m_rasterizer;     
    // triangles rasterizer 
    TriangleMeshSurfaceBuilder m_surfaceBuilder;
    // builder of shell around rasterized points 
    PointSetShellBuilder m_shellBuilder;


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
        if(DEBUG)printf("DistancreToMeshDataSource uses voxelSize: %7.3fmm\n",voxelSize/MM);
                
        return gridBounds;
    }

    
    public int getBaseValue(Vec pnt, Vec data){
        return ResultCodes.RESULT_OK;
    }

    
}