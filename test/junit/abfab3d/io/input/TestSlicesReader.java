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

package abfab3d.io.input;

// External Imports


import java.util.Map;


// external imports
import abfab3d.grid.query.CountStates;
import abfab3d.util.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports
import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.GridShortIntervals;

import abfab3d.geom.TriangulatedModels;

import abfab3d.io.output.STLWriter;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.SlicesWriter;

import abfab3d.util.MathUtil;


import static abfab3d.util.Units.MM;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;

/**
 * Tests the functionality of SlicesReader
 *
 * @version
 */
public class TestSlicesReader extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestSlicesReader.class);
    }


    /**
       make test suit happy 
     */
    public void testNothing(){
        
    }
    
    /**
       reads test set of slices
     */
    public void readSlices() throws Exception {

        double vs = 0.1*MM;
        int nx = 102; 
        int ny = 102; 
        int nz = 102; 
        String template = "/tmp/slices/density/slice%04d.png";
        int subvoxelResolution = 255;

        SlicesReader reader = new SlicesReader();
        AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, vs, vs);        
        reader.readSlices(grid, template, 0, 0, ny);
        
        SlicesWriter writer = new SlicesWriter();
        writer.writeSlices(grid, "/tmp/slices/dens/slice%04d.png", 0, 0, ny );
        
        

    }
        
    public static void main(String[] args) throws Exception{

        new TestSlicesReader().readSlices();
    }
}
