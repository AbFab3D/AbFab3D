package abfab3d.datasources;

import javax.vecmath.Tuple3d;

import abfab3d.core.GridDataDesc;
import abfab3d.core.Grid2D;
import abfab3d.param.SourceWrapper;
import abfab3d.core.Bounds;
import abfab3d.core.Grid2DProducer;

import static abfab3d.core.Output.printf;

/**
 * Wraps a grid and keep tracks of the source used.  Will clear out the source if any setters are used
 *
 * @author Alan Hudson
 */
public class Grid2DSourceWrapper implements Grid2D, SourceWrapper, Grid2DProducer {
    private static final boolean DEBUG = true;
    private Grid2D grid;
    private String source;

    public Grid2DSourceWrapper(String source, Grid2D grid) {
        this.grid = grid;
        this.source = source;
    }

    public Grid2DSourceWrapper(Grid2D grid) {
        this.grid = grid;
    }

    @Override
    public int getHeight() {
        return grid.getHeight();
    }

    @Override
    public int getWidth() {
        return grid.getWidth();
    }

    @Override
    public Bounds getGridBounds() {
        return grid.getGridBounds();
    }

    @Override
    public long getAttribute(int x, int y) {
        return grid.getAttribute(x, y);
    }

    @Override
    public double getVoxelSize() {
        return grid.getVoxelSize();
    }

    @Override
    public void getWorldCoords(int x, int y, Tuple3d coords) {
        grid.getWorldCoords(x,y,coords);
    }

    @Override
    public void getGridCoords(double x, double y, Tuple3d coords) {
        grid.getGridCoords(x,y,coords);
    }

    @Override
    public GridDataDesc getDataDesc() {
        return grid.getDataDesc();
    }

    @Override
        public boolean insideGrid(int x, int y) {
        return grid.insideGrid(x,y);
    }

    @Override
    public Grid2D createEmpty(int w, int h, double pixel) {
        return grid.createEmpty(w, h, pixel);
    }




    @Override
    public void setGridBounds(Bounds bounds) {
        if (DEBUG) {
            printf("Grid2D altered clearing source");
        }
        source = null;
        grid.setGridBounds(bounds);
    }


    @Override
    public void setAttribute(int x, int y, long attribute) {
        if (DEBUG) {
            printf("Grid2D altered clearing source");
        }
        source = null;
        grid.setAttribute(x, y, attribute);
    }

    @Override
    public void setDataDesc(GridDataDesc desc) {
        // TODO: not sure this is necessary
        if (DEBUG) {
            printf("Grid2D altered clearing source");
        }
        source = null;
        grid.setDataDesc(desc);
    }

    /**
     * Set the source for this grid.  This will be returned as the getParamString for this object until a setter is called.
     */
    public void setSource(String val) {
        this.source = val;
    }

    public String getParamString() {
        if (source == null) return toString();
        return source;
    }

    public void getParamString(StringBuilder sb) {
        if (source == null) sb.append(toString());
        sb.append(source);
    }

    /**
     * Return a Grid2D produced by the object 
     * @return The grid
     */
    public Grid2D getGrid2D(){
        return grid;
    }

}
