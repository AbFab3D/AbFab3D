package abfab3d.datasources;

import abfab3d.grid.AttributeDesc;
import abfab3d.grid.Grid2D;
import abfab3d.param.SourceWrapper;
import abfab3d.util.Bounds;

import static abfab3d.util.Output.printf;

/**
 * Wraps a grid and keep tracks of the source used.  Will clear out the source if any setters are used
 *
 * @author Alan Hudson
 */
public class Grid2DSourceWrapper implements Grid2D, SourceWrapper {
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
    public void getWorldCoords(int x, int y, double[] coords) {

    }

    @Override
    public AttributeDesc getAttributeDesc() {
        return grid.getAttributeDesc();
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
    public void setAttributeDesc(AttributeDesc desc) {
        // TODO: not sure this is necessary
        if (DEBUG) {
            printf("Grid2D altered clearing source");
        }
        source = null;
        grid.setAttributeDesc(desc);
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
}
