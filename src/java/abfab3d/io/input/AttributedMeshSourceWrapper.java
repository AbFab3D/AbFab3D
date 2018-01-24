/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2018
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */
package abfab3d.io.input;

import abfab3d.core.*;
import abfab3d.param.SourceWrapper;

import javax.vecmath.Vector3d;

/**
 * Container for attributed triangular mesh data
 *
 * @author Alan Hudson
 */
public class AttributedMeshSourceWrapper implements SourceWrapper, AttributedTriangleProducer, TriangleProducer, AttributedTriangleCollector, TriangleCollector {
    private String source;
    private AttributedMesh mesh;

    public AttributedMeshSourceWrapper(String source, AttributedMesh mesh) {
        this.source = source;
        this.mesh = mesh;
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
        if (source == null) {
            sb.append(toString());
            return;
        }
        sb.append(source);
    }

    public boolean addAttTri(Vec v0, Vec v1, Vec v2) {
        source = null;
        return mesh.addAttTri(v0,v1,v2);
    }

    public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2) {
        source = null;
        return mesh.addTri(v0,v1,v2);
    }

    public Bounds getBounds() {
        return mesh.getBounds();
    }

    public int getTriCount() {
        return mesh.getTriCount();
    }

    public DataSource getAttributeCalculator() {
        return mesh.getAttributeCalculator();
    }

    public void setAttributeCalculator(DataSource ac)
    {
        source = null;
        mesh.setAttributeCalculator(ac);
    }

    public void setDataDimension(int dd) {
        source = null;
        mesh.setDataDimension(dd);
    }

    public boolean getAttTriangles(AttributedTriangleCollector tc) {
        return mesh.getAttTriangles(tc);
    }

    public int getDataDimension() {
        return mesh.getDataDimension();
    }

    public boolean getTriangles(TriangleCollector tc) {
        return mesh.getTriangles(tc);
    }
}
