/*****************************************************************************
 *                        Copyright Shapeways, Inc (c) 2011
 *                               Java Source
 *
 *
 *
 ****************************************************************************/

package volumesculptor.shell;

import abfab3d.grid.op.GridMaker;

import static abfab3d.core.Units.MM;

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
        int max_threads = ShapeJSGlobal.getMaxThreadCount();
        if (max_threads == 0) {
            max_threads = Runtime.getRuntime().availableProcessors();
        }
        maker.setThreadCount(max_threads);
    }

    public GridMaker getMaker() {
        return maker;
    }
}
