/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
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

import abfab3d.core.AttributedTriangleCollector;
import abfab3d.core.AttributedTriangleProducer;
import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;
import abfab3d.core.Vec;

import javax.vecmath.Vector3d;
import java.util.ArrayList;
import static abfab3d.core.Output.printf;

/**
 * Container for attributed triangular mesh data
 *
 * @author Alan Hudson
 */
public interface AttributedMesh extends AttributedTriangleProducer, TriangleProducer, AttributedTriangleCollector, TriangleCollector {
    public int getTriCount();

    public DataSource getAttributeCalculator();

    public void setAttributeCalculator(DataSource ac);

    public void setDataDimension(int dd);

    public Bounds getBounds();

}
