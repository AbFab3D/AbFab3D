/*****************************************************************************
 *                        Copyright Shapeways, Inc (c) 2011
 *                               Java Source
 *
 *
 *
 ****************************************************************************/

package volumesculptor.shell;

import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.RangeCheckAttributeWrapper;
import abfab3d.grid.op.GridMaker;

import static abfab3d.util.Units.MM;

public class World {
    private AttributeGrid grid;
    private GridMaker maker;
    double bounds[] = new double[6];

    public World() {
        this(512,512,512,0.1*MM);
    }

    public World(int w, int h, int d, double vs) {
        System.out.println("Creating grid");
        // TODO: use the expanding version of this
        grid = new RangeCheckAttributeWrapper(new ArrayAttributeGridByte(w,h,d,vs,vs));

        maker = new GridMaker();
        maker.setVoxelSize(grid.getVoxelSize() * Math.sqrt(3.0)/2.0);
        maker.setBounds(bounds);
        maker.setThreadCount(Runtime.getRuntime().availableProcessors());
        grid.getGridBounds(bounds);
    }

    public AttributeGrid getGrid() {
        return grid;
    }

    public GridMaker getMaker() {
        return maker;
    }

    public double[] getBounds() {
        return bounds;
    }

    public void setBounds(double[] val) {
        bounds = val.clone();
        grid.setGridBounds(bounds);
        maker.setBounds(bounds);
    }

    public void setGrid(AttributeGrid grid) {
        this.grid = grid;
        grid.getGridBounds(bounds);
        maker.setBounds(bounds);
        maker.setVoxelSize(grid.getVoxelSize() * Math.sqrt(3.0)/2.0);
    }

    public String toString() {
        return "World: w: " + grid.getWidth() + " h: " + grid.getHeight() + " d: " + grid.getDepth();
    }
}
