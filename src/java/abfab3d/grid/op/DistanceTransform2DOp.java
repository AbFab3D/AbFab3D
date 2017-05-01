/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid.op;

import abfab3d.core.GridDataDesc;
import abfab3d.core.GridDataChannel;
import abfab3d.core.Grid2D;
import abfab3d.grid.Grid2DInt;
import abfab3d.grid.Grid2DShort;
import abfab3d.grid.Grid2DByte;
import abfab3d.grid.Operation2D;
import abfab3d.grid.util.GridUtil;

import abfab3d.core.MathUtil;
import abfab3d.core.Bounds;

import abfab3d.param.BaseParameterizable;
import abfab3d.param.Parameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.IntParameter;

import abfab3d.util.PointMap;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;

import static java.lang.Math.max;
import static java.lang.Math.sqrt;
import static java.lang.Math.round;
import static abfab3d.core.MathUtil.iround;

/**
 *
 * calculates signed distance data of 2D shape represented on the Grid2D 
 * 
 * the shape is given via implicit equation AttributeChannel.getValue(grid.getAttribute(x,y)) = threshold
 *  
 * distance inside shape is negative 
 * distance outside shape is positive
 *
 * interior pixels not reached by maxInDistance are initialized to -maxInDstance
 * exterior pixels not reached by maxOutDistance are initialized to maxOutDstance
 *
 * @author Vladimir Bulatov
 */
public class DistanceTransform2DOp extends BaseParameterizable implements Operation2D {

    public static boolean DEBUG = false;
    public static boolean DEBUG_TIMING = true;
    static int debugCount = 100;
    static final double TOL = 1.e-2;
    static final double HALF = 0.5; // half voxel offset to the center of voxel

    DoubleParameter mp_maxOutDist = new DoubleParameter("maxOutDist", "maximal outer distance to make transform", 10*MM);
    DoubleParameter mp_maxInDist = new DoubleParameter("maxInDist", "maximal inner distance to make transform", 10*MM);
    DoubleParameter mp_surfaceValue = new DoubleParameter("surface", "surface of the shape value", 0.5);
    IntParameter mp_interiorSign = new IntParameter("interiorSign", "sign of shape interior values", 1);
    IntParameter mp_interpolation = new IntParameter("interpolation", "type of interpolation", INTERP_LINEAR);
    DoubleParameter mp_surfaceLayerThickness = new DoubleParameter("surfaceLayerThickness", "thickness of initila surface layer", 2);
    
    Parameter m_param[] = new Parameter[]{
        mp_maxOutDist,
        mp_maxInDist,
        mp_surfaceValue,
        mp_interiorSign,
        mp_surfaceLayerThickness,
    };
    

    public static final int INTERP_THRESHOLD = 0, INTERP_LINEAR = 1, INTERP_IF = 2;
    
    /**
     @param inDistance maximal distance to calculate transform inside of the shape. Measured in meters
     @param outDistance maximal distance to calculate transform outside of the shape. Measured in meters
     @param surfaceValue value at surface of the shape 
    */
    public DistanceTransform2DOp(double maxInDistance, double maxOutDistance, double surfaceValue) {
        this(maxInDistance, maxOutDistance, surfaceValue, 1);
    }

    /**
     @param inDistance maximal distance to calculate transform inside of the shape. Measured in meters
     @param outDistance maximal distance to calculate transform outside of the shape. Measured in meters
     @param surfaceValue value at surface of the shape 
     @param interiorSign sign of shape interior
    */
    public DistanceTransform2DOp(double maxInDistance, double maxOutDistance, double surfaceValue, int interiorSign) {
        addParams(m_param);
        mp_maxInDist.setValue(maxInDistance);
        mp_maxOutDist.setValue(maxOutDistance);
        mp_surfaceValue.setValue(surfaceValue);
    }

    //public void setDataChannel(GridDataChannel dataChannel){
    //    m_dataChannel = dataChannel;
    //}

    
    /**
       set sign to be used for interior of the input shape
     */
    public void setInteriorSign(int interiorSign){
        mp_interiorSign.setValue(interiorSign);
    }

    public void setInterpolation(int interpolation){
        mp_interpolation.setValue(interpolation);
    }


    //
    // local class variable 
    //
    private double m_maxInDistance;
    private double m_maxOutDistance;
    private double m_voxelSize;
    private int m_nx;
    private int m_ny;
    private int m_neighbors[];
    private int m_subvoxelResolution = 100;
    private double m_surfaceValue;
    // sign of input data inside of shape 
    private int m_interiorSign;
    int m_interpolation = INTERP_LINEAR;

    private GridDataChannel m_dataChannel;
    private PointMap m_points;
    private Grid2D m_indexGrid;
    private Grid2D m_distanceGrid;


    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid A.
     * @return new grid with distance transform data
     */
    public Grid2D execute(Grid2D grid) {

        long t0 = time();
        double surfaceLayerThickness = mp_surfaceLayerThickness.getValue();
        m_interpolation = mp_interpolation.getValue();
        m_maxInDistance = mp_maxInDist.getValue();
        m_maxOutDistance = mp_maxOutDist.getValue();
        m_surfaceValue = mp_surfaceValue.getValue();
        m_interiorSign = mp_interiorSign.getValue();

        if(DEBUG)printf("DistanceTransform2D.execute(%s)\n", grid.getClass().getName());
        if(DEBUG)printf("  m_inDistance: %7.3f mm  m_outDistance: %7.3f mm \n", m_maxInDistance/MM, m_maxOutDistance/MM);
        m_voxelSize = grid.getVoxelSize();
        m_nx = grid.getWidth();
        m_ny = grid.getHeight();
        
        m_dataChannel = grid.getDataDesc().getChannel(0);
        m_neighbors = Neighborhood.makeDisk(surfaceLayerThickness+1);

        Bounds bounds = grid.getGridBounds();

        m_indexGrid = new Grid2DInt(bounds, m_voxelSize);
        m_distanceGrid = new Grid2DShort(m_nx, m_ny, m_voxelSize);
        GridDataChannel distChannel = new GridDataChannel(GridDataChannel.DISTANCE, "dist", 16, 0, -m_maxInDistance, m_maxOutDistance);
        m_distanceGrid.setDataDesc(new GridDataDesc(distChannel));

        // init dist grid with value outside of syrface layer 
        GridUtil.fill(m_distanceGrid, distChannel.makeAtt(surfaceLayerThickness));

        m_points = new PointMap(0.01); // subvoxel precision ofd point hash map 
        m_points.add(0, 0, 0);
        
        // find surface points 
        // initialize distanceas in thin layer around surface 
        initializeSurfaceLayer(grid, surfaceLayerThickness);
        int pcnt = m_points.getPointCount();
        double px[] = new double[pcnt];
        double py[] = new double[pcnt];
        m_points.getPoints(px, py);

        if(false) {            
            printf("points count: %d\n", pcnt);
            for(int i = 1; i < pcnt; i++){
                // start from 1, pnt[0] - is dummy 
                printf("(%5.2f %5.2f)\n", px[i],py[i]);
            }
        }

        // distribute distances to the whole grid 
        ClosestPointIndexer.PI2(pcnt, px, py, m_indexGrid);

        ClosestPointIndexer.getPointsInWorldUnits(m_indexGrid, px, py);

        Grid2D interiorGrid = makeInteriorGrid(grid, m_dataChannel, m_surfaceValue, m_interiorSign);
        ClosestPointIndexer.makeDistanceGrid2D(m_indexGrid, px,py, 
                                               interiorGrid,                                                
                                               m_maxInDistance, 
                                               m_maxOutDistance,
                                               m_distanceGrid);
        
        if(DEBUG)printf("DistanceTransformIndexed2D.execute() time: %d ms\n", (time() - t0));
        
        return m_distanceGrid;

    }

    public Grid2D getIndexGrid(){
        return m_indexGrid;
    }
    public Grid2D getDistanceGrid(){
        return m_distanceGrid;
    }

    void initializeSurfaceLayer(Grid2D dataGrid, double surfaceThickness){
        int nx = dataGrid.getWidth();
        int ny = dataGrid.getHeight();
        GridDataChannel dataConverter = m_dataChannel;
        int nx1 = nx-1;
        int ny1 = ny-1;
        double vs = dataGrid.getVoxelSize();
        Bounds bounds = dataGrid.getGridBounds();
        double xmin = bounds.xmin;
        double ymin = bounds.ymin;
        double th = m_surfaceValue;


        for(int iy = 0; iy < ny1; iy++){
            double y = iy + HALF;
            for(int ix = 0; ix < nx1; ix++){
                double x = ix + HALF;
                double v0 = dataConverter.getValue(dataGrid.getAttribute(ix,iy))-th;
                double vx = dataConverter.getValue(dataGrid.getAttribute(ix+1,iy))-th;
                double vy = dataConverter.getValue(dataGrid.getAttribute(ix,iy+1))-th;

                if((v0 <= 0. && vx > 0) || (v0 >= 0. && vx < 0)){
                    // add point on x segment 
                    addPoint(x+coeff(v0, vx), y, surfaceThickness);
                }
                if((v0 <= 0. && vy > 0) || (v0 >= 0. && vy < 0)){
                    // add point on y segment 
                    addPoint(x, y+coeff(v0, vy),surfaceThickness);                    
                }
            }
        }
    } 

    final double coeff(double v0, double v1){
        switch(m_interpolation){
        default: 
        case INTERP_LINEAR:
            return coeff_linear(v0, v1);
        case INTERP_IF:
            return coeff_IF(v0, v1);
        case INTERP_THRESHOLD:
            return coeff_threshold(v0, v1);
        }
    }

    final double coeff_threshold(double v0, double v1){
        return (v0/(v0-v1) < 0.5)? 0: 1;
    }

    /**
       root of linear map (0,1) -> (v0, v1) 
       v0 + x*(v1-v0) = 0
     */
    final double coeff_linear(double v0, double v1){
        return v0/(v0-v1);
    }

    final double coeff_IF(double v0, double v1){
        return MathUtil.coeffIF((v0+m_surfaceValue), (v1+m_surfaceValue));
    }

    // add point to the neigborhood in indexGrid 
    // and update layerDistanceGrid 
    final void addPoint(double px, double py,double surfaceThickness){

        //printf("addPoint(%7.4f, %7.4f)\n", px, py);
        int x0 = iround(px);
        int y0 = iround(py);
        int index = 0;

        int ncount = m_neighbors.length;
        
        for(int i = 0; i < ncount; i+=3){
            
            int 
                vx = x0 + m_neighbors[i],
                vy = y0 + m_neighbors[i+1];
            
            if( vx >= 0 && vy >= 0 && vx < m_nx && vy < m_ny ){
                double 
                    dx = vx + HALF - px,
                    dy = vy + HALF - py;
                double dist = sqrt(dx*dx + dy*dy);
                if(dist <= surfaceThickness-TOL) {
                    int newdist = (int)(dist*m_subvoxelResolution + 0.5);
                    int olddist = (int)m_distanceGrid.getAttribute(vx, vy);
                    if(newdist < olddist){
                        // better point found
                        m_distanceGrid.setAttribute(vx, vy, newdist);
                        if(index == 0){
                            // point is being added first time 
                            index = m_points.add(px, py, 0.);
                            if(false)printf("adding new point (%7.4f, %7.4f) index: %d \n", px, py, index);
                        }
                        m_indexGrid.setAttribute(vx, vy, index);
                    }                    
                }
            }        
        }
    }

    Grid2D makeInteriorGrid(Grid2D grid, GridDataChannel dataConverter, double threshold, int interiorSign){

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        Grid2D interiorGrid = new Grid2DByte(grid.getGridBounds(),grid.getVoxelSize());
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                double d = interiorSign*(dataConverter.getValue(grid.getAttribute(x,y))-threshold);                
                if(d <= 0.) {
                    // exterior 
                    interiorGrid.setAttribute(x,y,0);
                } else {
                    // interior 
                    interiorGrid.setAttribute(x,y,1);
                }
            }
        }        
        return interiorGrid;
    }
    
    
} // class DistanceTransformIndexed2D 
