/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.datasources;

// External Imports


import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.awt.Font;
import java.awt.Insets;


import java.io.File;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Matrix4d;

// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports
import abfab3d.core.AttributeGrid;
import abfab3d.core.GridDataDesc;
import abfab3d.core.GridDataChannel;
import abfab3d.core.Vec;
import abfab3d.core.MathUtil;
import abfab3d.core.VecTransform;
import abfab3d.core.Bounds;
import abfab3d.core.AttributePacker;

import abfab3d.param.Parameter;

import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.ArrayAttributeGridShort;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.GridShortIntervals;

import abfab3d.grid.op.GridMaker;


import abfab3d.util.TextUtil;
import abfab3d.util.Symmetry;

import abfab3d.datasources.DataSourceGrid;

import abfab3d.io.output.STLWriter;
import abfab3d.io.output.GridSaver;

import abfab3d.util.ImageMipMap;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;
import static abfab3d.core.MathUtil.TORAD;
import static abfab3d.core.VecTransform.RESULT_OK;
import static abfab3d.core.Units.MM;

import static java.lang.System.currentTimeMillis;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static java.lang.Math.PI;


/**
 * Tests the functionality of Embossing
 *
 * @version
 */
public class TestCompositeShape extends TestCase {

    public static boolean DEBUG = true;
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestCompositeShape.class);
    }

    public void testNothing(){
    }
    
    public void devTestPendant() throws Exception{

        CompositeShape pendant = new CompositeShape(new FractalPendant());
        Parameter param[] = pendant.getParams();
        for(int i = 0; i < param.length; i++){
            printf("%s : %s\n", param[i].getName(),param[i].getValue());
        }
        pendant.set("distOffset", -0.2*MM);
        pendant.set("torusAxis", new Vector3d(0,1,1));
        pendant.set("showGens", true);
        pendant.set("genThickness", 2*MM);

        double vs = 0.025*MM;
        double bx = 15*MM;
        double bz = 3*MM;
        double maxDist  = 1*MM;
        double r = 0.5*MM;
        Bounds bounds = new Bounds(-bx,bx,-bx,bx,-bz,bz, vs);
        
        //AttributeGrid grid = makeDensityGrid(bounds);
        AttributeGrid grid = makeDistanceGrid(bounds);

        double RR = 12*MM;
        Torus ring = new Torus(RR, r);
        //Union pendantInRing = new Union(pendant, ring);
        Union pendantInRing = new Union(new Intersection(new Sphere(0,0,0,RR),pendant), ring);
        pendantInRing.set("blend", 0.1*MM);
        
        GridMaker gm = new GridMaker();
        gm.setSource(pendantInRing);
        //gm.setSource(ring);
        gm.makeGrid(grid);
        GridSaver saver = new GridSaver();
        saver.setWriteTexturedMesh(false);
        saver.write(grid,"/tmp/test/pendant_dist.stl");
        
    }
    
    static AttributeGrid makeDensBGRGrid(Bounds bounds) {

        double vs = bounds.getVoxelSize();
        long nx = bounds.getWidthVoxels(vs);
        long ny = bounds.getHeightVoxels(vs);
        long nz = bounds.getDepthVoxels(vs);
        AttributeGrid grid;

        grid = new ArrayAttributeGridInt(bounds, vs, vs);
        
        grid.setDataDesc(GridDataDesc.getDensBGR());

        AttributePacker ap = grid.getDataDesc().getAttributePacker();
        return grid;
    }

    static AttributeGrid makeDensityGrid(Bounds bounds) {

        double vs = bounds.getVoxelSize();
        long nx = bounds.getWidthVoxels(vs);
        long ny = bounds.getHeightVoxels(vs);
        long nz = bounds.getDepthVoxels(vs);
        AttributeGrid grid;

        grid = new ArrayAttributeGridByte(bounds, vs, vs);
        
        grid.setDataDesc(GridDataDesc.getDensity(8));

        return grid;
    }

    static AttributeGrid makeDistanceGrid(Bounds bounds) {

        double vs = bounds.getVoxelSize();
        long nx = bounds.getWidthVoxels(vs);
        long ny = bounds.getHeightVoxels(vs);
        long nz = bounds.getDepthVoxels(vs);
        AttributeGrid grid;

        grid = new ArrayAttributeGridByte(bounds, vs, vs);
        
        grid.setDataDesc(GridDataDesc.getDistance(8, 2*vs));

        return grid;
    }

    static AttributeGrid makeDistBGRGrid(Bounds bounds, double maxDist) {

        double vs = bounds.getVoxelSize();
        long nx = bounds.getWidthVoxels(vs);
        long ny = bounds.getHeightVoxels(vs);
        long nz = bounds.getDepthVoxels(vs);
        AttributeGrid grid;

        grid = new ArrayAttributeGridInt(bounds, vs, vs);        
        grid.setDataDesc(GridDataDesc.getDistBGR(maxDist));

        return grid;
    }


   public static void main(String[] args) {
       try {
           new TestCompositeShape().devTestPendant();
       } catch(Exception e){
           e.printStackTrace();
       }
    }
}