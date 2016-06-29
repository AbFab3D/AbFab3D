package abfab3d.datasources;

import abfab3d.core.ClassTraverser;
import abfab3d.core.Grid;
import abfab3d.core.VoxelClasses;
import abfab3d.core.VoxelData;
import abfab3d.grid.*;
import abfab3d.param.SourceWrapper;
import abfab3d.core.Bounds;

/**
 * Wraps a grid and keep tracks of the source used.  Will clear out the source if any setters are used
 *
 * @author Alan Hudson
 */
public class GridSourceWrapper extends BaseWrapper implements SourceWrapper {
    private String source;

    public GridSourceWrapper(String source, Grid grid) {
        super(grid);

        this.grid = grid;
        this.source = source;
    }

    public GridSourceWrapper(Grid grid) {
        super(grid);
    }

    @Override
    public void getDataWorld(double x, double y, double z, VoxelData data) {
        grid.getDataWorld(x,y,z,data);
    }

    @Override
    public void getData(int x, int y, int z, VoxelData data) {
        grid.getData(x, y, z, data);
    }

    @Override
    public byte getStateWorld(double x, double y, double z) {
        return grid.getStateWorld(x,y,z);
    }

    @Override
    public void getGridCoords(double x, double y, double z, int[] coords) {
        grid.getGridCoords(x,y,z,coords);
    }

    @Override
    public void getWorldCoords(int x, int y, int z, double[] coords) {
        grid.getWorldCoords(x, y, z, coords);
    }

    @Override
    public void getGridBounds(double[] min, double[] max) {
        grid.getGridBounds(min, max);
    }

    @Override
    public Bounds getGridBounds() {
        return grid.getGridBounds();
    }

    @Override
    public void getGridBounds(double[] bounds) {
        grid.getGridBounds(bounds);
    }

    @Override
    public byte getState(int x, int y, int z) {
        return grid.getState(x,y,z);
    }

    @Override
    public int findCount(VoxelClasses vc) {
        return grid.findCount(vc);
    }

    @Override
    public void find(VoxelClasses vc, ClassTraverser t) {
        grid.find(vc,t);
    }

    @Override
    public void find(VoxelClasses vc, ClassTraverser t, int xmin, int xmax, int ymin, int ymax) {
        grid.find(vc,t,xmin,xmax,ymin,ymax);
    }

    @Override
    public void findInterruptible(VoxelClasses vc, ClassTraverser t) {
        grid.findInterruptible(vc, t);
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
    public int getDepth() {
        return grid.getDepth();
    }

    @Override
    public double getVoxelSize() {
        return grid.getVoxelSize();
    }

    @Override
    public double getSliceHeight() {
        return grid.getSliceHeight();
    }

    @Override
    public String toStringSlice(int s) {
        return grid.toStringSlice(s);
    }

    @Override
    public String toStringAll() {
        return grid.toStringAll();
    }

    @Override
    public Grid createEmpty(int w, int h, int d, double pixel, double sheight) {
        return grid.createEmpty(w, h, d, pixel, sheight);
    }

    @Override
    public VoxelData getVoxelData() {
        return grid.getVoxelData();
    }

    @Override
    public boolean insideGrid(int x, int y, int z) {
        return grid.insideGrid(x, y, z);
    }

    @Override
    public boolean insideGridWorld(double wx, double wy, double wz) {
        return grid.insideGridWorld(wx, wy, wz);
    }




    @Override
    public void setState(int x, int y, int z, byte state) {
        source = null;
        grid.setState(x,y,z,state);
    }

    @Override
    public void setStateWorld(double x, double y, double z, byte state) {
        source = null;
        grid.setStateWorld(x, y, z, state);
    }

    @Override
    public void setGridBounds(Bounds bounds) {
        source = null;
        grid.setGridBounds(bounds);
    }

    @Override
    public void setGridBounds(double[] bounds) {
        source = null;
        grid.setGridBounds(bounds);
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

    public Object clone() {
        return super.clone();
    }
}
