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

package booleanops;

// External Imports
import java.util.*;
import java.io.*;
import org.web3d.vrml.sav.ContentHandler;
import org.web3d.vrml.export.*;
import org.web3d.util.ErrorReporter;

// Internal Imports
import abfab3d.geom.*;
import abfab3d.geom.CubeCreator.Style;
import abfab3d.grid.*;


/**
 * Linked Cubes.
 *
 * Would be nice to support this style as well:
 *   http://www.shapeways.com/model/130911/cubichain_22cm_6mm.html?mode=3d
 *
 *   I suspect its done via solid modeling.  Not sure about the inner edges
 *   not being straight though
 *
 * @author Alan Hudson
 */
public class BooleanOps {
    /** Resolution of the printer in meters.  */
    public static final double PRINTER_RESOLUTION = 0.001;

    public void generate(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ErrorReporter console = new PlainTextErrorReporter();

            X3DClassicRetainedExporter writer = new X3DClassicRetainedExporter(fos,3,0,console);

            Style[][] styles = new Style[6][];

            styles[0] = new Style[1];
            styles[0][0] = Style.FILLED;

            styles[1] = new Style[1];
            styles[1][0] = Style.FILLED;

            styles[2] = new Style[1];
            styles[2][0] = Style.FILLED;

            styles[3] = new Style[1];
            styles[3][0] = Style.FILLED;

            styles[4] = new Style[1];
            styles[4][0] = Style.FILLED;

            styles[5] = new Style[1];
            styles[5][0] = Style.FILLED;

            Grid grid = new SliceGrid(80,80,80,0.001, 0.001, true);

            double x,y,z;
            CubeCreator cg = null;
            int size = 4;
            double boxSize = 0.008;

System.out.println("Generating cube");
            cg = new CubeCreator(styles, boxSize, boxSize, boxSize,
                boxSize,boxSize,boxSize,(byte)1);

            cg.generate(grid);

            writer.startDocument("","", "utf8", "#X3D", "V3.0", "");
            writer.profileDecl("Immersive");
            writer.startNode("NavigationInfo", null);
            writer.startField("avatarSize");
            writer.fieldValue(new float[] {0.01f, 1.6f, 0.75f}, 3);
            writer.endNode(); // NavigationInfo
            writer.startNode("Viewpoint", null);
            writer.startField("position");
            writer.fieldValue(new float[] {0.028791402f,0.005181627f,0.11549001f},3);
            writer.startField("orientation");
            writer.fieldValue(new float[] {-0.06263941f,0.78336f,0.61840385f,0.31619227f},4);
            writer.endNode(); // Viewpoint

            grid.toX3D(writer, null);
            writer.endDocument();

            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        BooleanOps c = new BooleanOps();
        c.generate("out.x3dv");
    }
}