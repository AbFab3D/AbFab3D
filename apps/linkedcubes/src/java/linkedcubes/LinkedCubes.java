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

package linkedcubes;

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
import abfab3d.io.output.BoxesX3DExporter;


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
public class LinkedCubes {
    /** Resolution of the printer in meters.  */
    public static final double PRINTER_RESOLUTION = 0.001;

    public void generate(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ErrorReporter console = new PlainTextErrorReporter();

            X3DClassicRetainedExporter writer = new X3DClassicRetainedExporter(fos,3,0,console);

            Style[][] styles = new Style[6][];

            styles[0] = new Style[4];
            styles[0][0] = Style.TOP_ROW;
            styles[0][1] = Style.BOTTOM_ROW;
            styles[0][2] = Style.LEFT_ROW;
            styles[0][3] = Style.RIGHT_ROW;

            styles[0] = new Style[4];
            styles[0][0] = Style.TOP_ROW;
            styles[0][1] = Style.BOTTOM_ROW;
            styles[0][2] = Style.LEFT_ROW;
            styles[0][3] = Style.RIGHT_ROW;


            styles[1] = new Style[4];
            styles[1][0] = Style.TOP_ROW;
            styles[1][1] = Style.BOTTOM_ROW;
            styles[1][2] = Style.LEFT_ROW;
            styles[1][3] = Style.RIGHT_ROW;

            styles[2] = new Style[4];
            styles[2][0] = Style.TOP_ROW;
            styles[2][1] = Style.BOTTOM_ROW;
            styles[2][2] = Style.LEFT_ROW;
            styles[2][3] = Style.RIGHT_ROW;

            styles[3] = new Style[4];
            styles[3][0] = Style.TOP_ROW;
            styles[3][1] = Style.BOTTOM_ROW;
            styles[3][2] = Style.LEFT_ROW;
            styles[3][3] = Style.RIGHT_ROW;

//            Grid grid = new SliceGrid(80,80,80,PRINTER_RESOLUTION, PRINTER_RESOLUTION, true);
            Grid grid = new ArrayGrid(80,80,80,PRINTER_RESOLUTION, PRINTER_RESOLUTION);

            double x,y,z;
            CubeCreator cg = null;
            int size = 3;
            double boxSize = 0.008;

            double exoffset = 0;
            double eyoffset = 0;
            double ezoffset = 0;
            double exspacer = boxSize / 4;
            double eyspacer = boxSize / 4;
            double ezspacer = -(boxSize + boxSize / 2) / 4;

            double oxoffset = boxSize / 2;
            double oyoffset = boxSize / 2;
            double ozoffset = 0.000;
            double oxspacer = boxSize / 4;
            double oyspacer = boxSize / 4;
            double ozspacer = -(boxSize + boxSize / 2) / 4;

            double xoffset;
            double yoffset;
            double zoffset;
            double xspacer;
            double yspacer;
            double zspacer;

            for(int k=0; k < 2 * size - 1; k++) {
                if (k % 2 == 0) {
                    xspacer = exspacer;
                    yspacer = eyspacer;
                    zspacer = ezspacer;
                    xoffset = exoffset;
                    yoffset = eyoffset;
                    zoffset = ezoffset;

                    z = zoffset + boxSize * (k+1) + zspacer * (k+1);
                } else {
                    xspacer = oxspacer;
                    yspacer = oyspacer;
                    zspacer = ozspacer;
                    xoffset = oxoffset;
                    yoffset = oyoffset;
                    zoffset = ozoffset;

                    z = zoffset + boxSize * (k+1) + zspacer * (k+1);
                }

                int len;

                if (k % 2 == 0) {
                    len = size;
                } else {
                    len = size - 1;
                }

                for(int i=0; i < len; i ++) {
                    for(int j=0; j < len; j++) {
                        if (i % 2 == 0) {
                            x = xoffset + boxSize * (j+1) + xspacer * (j+1);
                            y = yoffset + boxSize * (i+1) + yspacer * (i+1);
                        } else {
                            x = xoffset + boxSize * (j+1) + xspacer * (j+1);
                            y = yoffset + boxSize * (i+1) + yspacer * (i+1);
                        }

                        cg = new CubeCreator(styles, boxSize, boxSize, boxSize,
                            x,y,z,(byte)1);

                        cg.generate(grid);
                    }
                }
            }

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

            BoxesX3DExporter exporter = new BoxesX3DExporter();
            exporter.toX3D(grid, writer, null);

            writer.endDocument();

            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        LinkedCubes c = new LinkedCubes();
        c.generate("out.x3dv");
    }
}