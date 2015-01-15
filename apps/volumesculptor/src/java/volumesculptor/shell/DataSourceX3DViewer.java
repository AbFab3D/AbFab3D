/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package volumesculptor.shell;

import abfab3d.datasources.Box;
import abfab3d.param.SNode;
import abfab3d.datasources.TransformableDataSource;
import abfab3d.grid.Grid;
import abfab3d.util.DataSource;
import abfab3d.util.VecTransform;
import org.web3d.vrml.sav.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Visualize a data source graph using X3D.
 *
 * Use X3D geometry for primitives such as sphere, box, cylinder.
 * Use bounds geometry for other bounded datasources such as spring.
 * Use grid bounds + 25% for infinite bounds datasources
 * Show grids bounds in white
 *
 * @author Alan Hudson
 */
public class DataSourceX3DViewer {
    private DataSource dataSource;
    private VecTransform transform;

    public DataSourceX3DViewer(DataSource dataSource, VecTransform transform) {
        this.dataSource = dataSource;
        this.transform = transform;
    }

    public void setSource(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public void setTransform(VecTransform transform){
        this.transform = transform;
    }

    /**
     * Generate a visualization of the ShapeJS tree
     *
     * @param grid The grid or null for non specific
     * @param stream
     */
    public void generate(Grid grid, BinaryContentHandler stream) {
        List<VecTransform> trans = new ArrayList<VecTransform>();
        if (transform != null) {
            trans.add(transform);
        }

        viz(dataSource,trans,grid,stream);
    }

    private void viz(DataSource src, List<VecTransform> trans, Grid grid, BinaryContentHandler stream) {
        if (src instanceof SNode) {
            System.out.println("Commented out");
            /*
            DataSource[] children = ((SNode)src).getChildren();

            VecTransform vt = null;
            if (src instanceof TransformableDataSource) {
                vt = ((TransformableDataSource)src).getTransform();
            }

            List<VecTransform> new_trans = null;

            if (vt != null) {
                new_trans = new ArrayList<VecTransform>(trans);
                new_trans.add(vt);
            } else {
                new_trans = trans;
            }

            for(DataSource ds : children) {
                viz(ds,new_trans,grid,stream);
            }
            */
        } else {
            if (src instanceof Box) {
                //outputBox(src,trans,stream);
            }
        }
    }

    private void outputBox(double[] pos, double[] size,
                           List<VecTransform> trans, BinaryContentHandler stream) {
        // can't use x3d primitives as we need to transform each point by the transforms

    }
}
