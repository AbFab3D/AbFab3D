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
import abfab3d.util.Bounds;
import abfab3d.util.Initializable;

import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.geom.TriangleMeshShellBuilder;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;


/**
   creates antialiased rasterization using distance to mesh calculations
   
   distance is calculated in thin shell around the mesh 

 */
public class DistanceRasterizer implements TriangleCollector, Initializable {


    long m_interiorValue;
    // size of grid 
    int gridX,gridY,gridZ;

    MeshRasterizer m_rasterizer;     
    TriangleMeshShellBuilder m_shellBuilder;
    Bounds m_bounds;
    AttributeGrid m_indexGrid;


    public DistanceRasterizer(Bounds bounds, int gridX, int gridY, int gridZ){
        
        this.gridX = gridX;
        this.gridY = gridY;
        this.gridZ = gridZ;
        this.m_bounds = bounds.clone();

        m_rasterizer = new MeshRasterizer(bounds, gridX, gridY, gridZ);
        m_rasterizer.setInteriorValue(1);
        
    }

    public void setInteriorValue(long value){

        m_interiorValue = value;

    }

    public int initialize(){

        m_indexGrid = makeIndexGrid();
        m_shellBuilder = new TriangleMeshShellBuilder(m_indexGrid, m_interiorValue);
        m_shellBuilder.setShellHalfThickness(1.);
        m_shellBuilder.initialize();
        
        return RESULT_OK;
    }


    protected AttributeGrid makeIndexGrid(){
        
        double vs = m_bounds.getVoxelSize();
        printf("index grid bounds: %s  voxelSize: %7.5f\n", m_bounds, vs);
        return new ArrayAttributeGridInt(m_bounds, vs, vs);
    }

    public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){

        m_rasterizer.addTri(v0, v1, v2);
        m_shellBuilder.addTri(v0, v1, v2);

        return true;
    }

    public void getRaster(AttributeGrid grid){

        printf("DistanceRasterizer.getRaster(grid)\n");
        long t0 = time();
        int pcount = m_shellBuilder.getPointCount();
        printf("pcount: %d\n", pcount);
        double pntx[] = new double[pcount];
        double pnty[] = new double[pcount];
        double pntz[] = new double[pcount];
        m_shellBuilder.getPoints(pntx, pnty, pntz);
        
        printf("DistanceRasterizer.getRaster(grid) done: %d ms\n", (time() - t0));
        
        /*
        ClosesPointIndexer.makeDistanceGrid(m_indexGrid, pntx, pnty, pntz,
                                            interiorGrid, 
                                            distanceGrid, 
                                            m_maxInDistance, 
                                            m_maxOutDistance, 
                                            subvoxelResolution);
        */
    }

}