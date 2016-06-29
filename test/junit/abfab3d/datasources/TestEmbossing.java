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
public class TestEmbossing extends TestCase {

    public static boolean DEBUG = true;
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestEmbossing.class);
    }

    public void testNothing(){
    }
    
    public void devTestEmbossingText() throws Exception{

        double vs = 0.1*MM;
        double R = 10*MM;
        double b = R + 1*MM;
        double maxDist = 1*MM;
        Bounds bounds = new Bounds(-b,b,-b,b,-b,b, vs);
        
        Sphere s = new Sphere(R);
        
        DataSourceMixer cs = new DataSourceMixer(s, new Constant(0,0,1));
        Text2D text = new Text2D("TEXT");
        text.set("fontName","Courier New");
        ImageMap textMap = new ImageMap(text, 10*MM, 6*MM, 30*MM);
        textMap.set("repeatX", false);
        textMap.set("repeatY", false);
        textMap.set("blurWidth",0.05*MM);
        textMap.set("blackDisplacement", 0.5*MM);
        textMap.set("whiteDisplacement", 0*MM);

        DataSourceMixer ctextMap = new DataSourceMixer(textMap, new Constant(1,0,0));
        
        Embossing emb = new Embossing(cs, ctextMap);
        emb.set("minValue",0.0*MM);
        emb.set("maxValue",0.5*MM);
        emb.set("factor", 1);
        emb.set("mixThreshold", 0.1);
        emb.set("mixAmount", 0.2);
        
        //AttributeGrid grid = makeDensBGRGrid(bounds);
        AttributeGrid grid = makeDistBGRGrid(bounds, maxDist);
                
        GridMaker gm = new GridMaker();
        gm.setSource(emb);
        gm.makeGrid(grid);
        GridSaver saver = new GridSaver();
        saver.setWriteTexturedMesh(true);
        saver.write(grid,"/tmp/test/embossing.x3d");
        
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

    static AttributeGrid makeDistBGRGrid(Bounds bounds, double maxDist) {

        double vs = bounds.getVoxelSize();
        long nx = bounds.getWidthVoxels(vs);
        long ny = bounds.getHeightVoxels(vs);
        long nz = bounds.getDepthVoxels(vs);
        AttributeGrid grid;

        grid = new ArrayAttributeGridInt(bounds, vs, vs);
        
        grid.setDataDesc(GridDataDesc.getDistBGR(maxDist));

        AttributePacker ap = grid.getDataDesc().getAttributePacker();
        return grid;
    }


   public static void main(String[] args) {
       try {
           new TestEmbossing().devTestEmbossingText();
       } catch(Exception e){
           e.printStackTrace();
       }
    }
}