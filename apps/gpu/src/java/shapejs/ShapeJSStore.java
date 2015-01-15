package shapejs;

import abfab3d.grid.Bounds;
import abfab3d.grid.Grid;
import abfab3d.grid.op.GridMaker;
import abfab3d.util.DataSource;

import java.util.ArrayList;
import java.util.List;

import static abfab3d.util.Output.printf;

/**
 * Store datasources
 *
 * @author Alan Hudson
 */
public class ShapeJSStore {
    private ArrayList<DataSource> sources = new ArrayList<DataSource>();
    private ArrayList<Bounds> bounds = new ArrayList<Bounds>();

    public void addMaker(GridMaker maker, Grid grid) {
        // TODO: Likely need to clone to avoid user code changing
        sources.add(maker.getDataSource());
        bounds.add(grid.getGridBounds());
    }

    public List<DataSource> getDataSources() {
        return sources;
    }

    public List<Bounds> getBounds() {
        return bounds;
    }
}
