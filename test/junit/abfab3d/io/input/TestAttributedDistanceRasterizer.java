/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
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

import abfab3d.datasources.Constant;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.GridDataChannel;
import abfab3d.grid.GridDataDesc;
import abfab3d.grid.util.GridUtil;
import abfab3d.util.BoundingBoxCalculator;
import abfab3d.util.Bounds;
import abfab3d.util.DataSource;
import abfab3d.util.Vec;

import abfab3d.io.output.GridSaver;


import junit.framework.TestCase;

import java.io.IOException;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Units.MM;


/**
 * Test AttributedMeshReader
 *
 * @author Alan Hudson
 */
public class TestAttributedDistanceRasterizer extends TestCase {


    public void testMesh() throws IOException {
        double bx = 1*MM;
        double by = 1*MM;
        double bz = 1*MM;

        double s  = Math.max(bx,Math.max(by,bz));

        AttributedMesh box = makeBox(bx,by,bz);
        Bounds bounds = box.getBounds();
        Constant colorizer = new Constant(0.8,0.8,0.8);
        colorizer.initialize();

        double vs = 0.1*MM;
        int size = (int) (2*s / vs);
        printf("size: %d\n",size);
        AttributedDistanceRasterizer rasterizer = new AttributedDistanceRasterizer(bounds, size,size,size);
        rasterizer.setDataDimension(3);
        
        AttributeGrid grid = new ArrayAttributeGridByte(bounds,vs,vs);
        GridDataDesc attDesc = new GridDataDesc();
        GridDataChannel gdc = new GridDataChannel(GridDataChannel.DISTANCE, "dist", 8,0.5*MM,-0.5*MM);
        attDesc.addChannel(gdc);
        grid.setDataDesc(attDesc);

        rasterizer.getAttributedDistances(box, colorizer, grid);

        GridUtil.printSliceAttribute(grid,size/2);
    }

    public void devTestBox() throws IOException {

        double bx = 10*MM;
        double by = 10*MM;
        double bz = 10*MM;

        double s  = Math.max(bx,Math.max(by,bz));

        AttributedMesh box = makeBox(bx,by,bz);
        Bounds bounds = box.getBounds();
        Constant colorizer = new Constant(0.9,0.2,0.0);
        colorizer.initialize();

        GridLoader loader = new GridLoader();
        loader.setThreadCount(8);
        loader.setMaxInDistance(1*MM);
        loader.setMaxOutDistance(1*MM);           
        loader.setMargins(0.2*MM);   
        loader.setPreferredVoxelSize(0.1*MM);   

        AttributeGrid grid = loader.rasterizeAttributedTriangles(box, colorizer);
        GridSaver writer = new GridSaver();
        writer.setWriteTexturedMesh(true);
        writer.setTexPixelSize(1);
        writer.setMeshSmoothingWidth(1);
        writer.setTexTriExt(1.5);
        writer.setTexTriGap(1.5);
        //String outPath = "/tmp/tex/box.svx";
        String outPath = "/tmp/tex/box.x3d";
        printf("writing: %s\n", outPath);
        writer.write(grid, outPath);
        printf("done\n");        
    }

    private AttributedMesh makeBox(double bx,double by,double bz) {
        AttributedMesh box = new AttributedMesh(3);
        Vec v0 = new Vec(bx,by,bz,0,0,0);
        Vec v1 = new Vec(-bx,by,bz,0,0,0);
        Vec v2 = new Vec(-bx,-by,bz,0,0,0);
        Vec v3 = new Vec(bx,-by,bz,0,0,0);
        Vec v4 = new Vec(bx,by,-bz,0,0,0);
        Vec v5 = new Vec(-bx,by,-bz,0,0,0);
        Vec v6 = new Vec(-bx,-by,-bz,0,0,0);
        Vec v7 = new Vec(bx,-by,-bz,0,0,0);

        // add vertices
        box.addAttTri(v0,v1,v2);
        box.addAttTri(v0,v2,v3);
        box.addAttTri(v7,v0,v3);
        box.addAttTri(v7,v4,v0);
        box.addAttTri(v1,v5,v6);
        box.addAttTri(v1,v6,v2);
        box.addAttTri(v4,v5,v1);
        box.addAttTri(v4,v1,v0);
        box.addAttTri(v3,v2,v6);
        box.addAttTri(v3,v6,v7);
        box.addAttTri(v7,v6,v5);
        box.addAttTri(v7,v5,v4);

        return box;

    }

    public static void main(String arg[])throws Exception {
        new TestAttributedDistanceRasterizer().devTestBox();
    }

}


