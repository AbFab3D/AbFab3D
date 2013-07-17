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
    private GridMaker maker;

    public World() {
        this(512,512,512,0.1*MM);
    }

    public World(int w, int h, int d, double vs) {
        maker = new GridMaker();
        //maker.setVoxelSize(grid.getVoxelSize() * Math.sqrt(3.0)/2.0);
        maker.setMaxAttributeValue(255);
        //maker.setBounds(bounds);
        maker.setThreadCount(Runtime.getRuntime().availableProcessors());
    }

    public GridMaker getMaker() {
        return maker;
    }
}
