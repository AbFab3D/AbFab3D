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

package dla;

// External Imports
import java.io.*;
import java.util.*;

import abfab3d.io.output.*;
import org.j3d.geom.GeometryData;
import org.j3d.geom.*;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.sav.BinaryContentHandler;

import abfab3d.geom.*;
import abfab3d.grid.*;
import abfab3d.creator.*;
import abfab3d.creator.shapeways.*;

//import java.awt.*;


/**
 * Geometry Kernel for the ImageEditor.
 *
 * Some images don't seem to work right, saving them with paint "fixes" this.  Not sure why.
 *    And example of this is the cat.png image.
 *
 * @author Alan Hudson
 */
public class SnowFlakeKernel extends HostedKernel {
    /** Debugging level.  0-5.  0 is none */
    private static final int DEBUG_LEVEL = 0;

    /** The horizontal and vertical resolution */
    private double resolution;

    /** The width of the body geometry */
    private double bodyWidth;

    /** The height of the body geometry */
    private double bodyHeight;

    /** The depth of the body geometry */
    private double bodyDepth;

    /** The image filename */
    private String filename;
    private String filename2;

    /** Should we invert the image */
    private boolean invert;

    /** The depth of body image */
    private double bodyImageDepth;
    private double bodyImageDepth2;

    /**
     * Get the parameters for this editor.
     *
     * @return The parameters.
     */
    public Map<String,Parameter> getParams() {
        HashMap<String,Parameter> params = new HashMap<String,Parameter>();

        int seq = 0;
        int step = 0;

        params.put("bodyImage", new Parameter("bodyImage", "Image Layer 1", "The image to use for the front body", "images/leaf/5.png", 1,
            Parameter.DataType.STRING, Parameter.EditorType.FILE_DIALOG,
            step, seq++, false, 0, 0.1, null, null)
        );
        params.put("bodyImage2", new Parameter("bodyImage2", "Image Layer 2", "The image to use for the front body", "NONE", 1,
                Parameter.DataType.STRING, Parameter.EditorType.FILE_DIALOG,
                step, seq++, false, 0, 0.1, null, null)
        );

        params.put("bodyImageInvert", new Parameter("bodyImageInvert", "Invert Image", "Should we use black for cutting", "true", 1,
            Parameter.DataType.BOOLEAN, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0, 0.1, null, null)
        );

        params.put("bodyImageType", new Parameter("bodyImageType", "Image Mapping Technique", "The type of image", "SQUARE", 1,
            Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
            step, seq++, true, 0, 0.1, null, new String[] {"SQUARE","CIRCULAR"})
        );

        params.put("bodyImageDepth", new Parameter("bodyImageDepth", "Depth Amount - Layer 1", "The depth of the image", "0.006", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0, 1, null, null)
        );

        params.put("bodyImageDepth2", new Parameter("bodyImageDepth2", "Depth Amount - Layer 2", "The depth of the image", "0.001", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        step++;
        seq = 0;
/*
        params.put("resolution", new Parameter("resolution", "Resolution", "How accurate to model the object", "0.00006", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, true, 0, 0.1, null, null)
        );
*/
        params.put("resolution", new Parameter("resolution", "Resolution", "How accurate to model the object", "0.0015", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 0.1, null, null)
        );

        params.put("bodyWidth", new Parameter("bodyWidth", "Body Width", "The width of the main body", "0.3", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0.01, 1, null, null)
        );

        params.put("bodyHeight", new Parameter("bodyHeight", "Body Height", "The height of the main body", "0.3", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0.01, 1, null, null)
        );

        return params;
    }

    /**
     * @param params The parameters
     * @param acc The accuracy to generate the model
     * @param handler The X3D content handler to use
     */
    public KernelResults generate(Map<String,Object> params, Accuracy acc, BinaryContentHandler handler) throws IOException {

        pullParams(params);

        // Calculate maximum bounds

        // TODO: We should be able to accurately calculate this
        double max_width = bodyWidth * 1.2;
        double max_height = bodyHeight * 1.3;
        double max_depth = (bodyImageDepth + bodyImageDepth2) * 1.2;

        // Setup Grid
        boolean bigIndex = false;
        int voxelsX = (int) Math.ceil(max_width / resolution);
        int voxelsY = (int) Math.ceil(max_height / resolution);
        int voxelsZ = (int) Math.ceil(max_depth / resolution);

        if ((long) voxelsX * voxelsY * voxelsZ > Math.pow(2,31)) {
            bigIndex = true;
        }

System.out.println("Voxels: " + voxelsX + " " + voxelsY + " " + voxelsZ);
        Grid grid = null;

        boolean useBlockBased = true;

        if (bigIndex) {
            grid = new ArrayAttributeGridByteIndexLong(voxelsX, voxelsY, voxelsZ, resolution, resolution);
        } else {
            if (useBlockBased) {
                grid = new BlockBasedAttributeGridByte(voxelsX, voxelsY, voxelsZ, resolution, resolution);
            } else {
                grid = new ArrayAttributeGridByte(voxelsX, voxelsY, voxelsZ, resolution, resolution);
            }
        }

        if (DEBUG_LEVEL > 0) grid = new RangeCheckWrapper(grid);


        System.out.println("Initial Pos: " + (voxelsX / 2 - 1) + "," + (voxelsY / 2 - 1) + "," + (voxelsZ / 2 - 1));
        grid.setState(voxelsX / 2 - 1, voxelsY / 2 - 1, 2, Grid.INSIDE);

        int iter = 10000;

        int[] pos = new int[3];

        Random rand = new Random(42);

        int minw = 0;
        int maxw = grid.getWidth() / 2;
        int minh = 0;
        int maxh = grid.getHeight() / 2;
        int mind = 2;
        int maxd = 2;

        for(int i=0; i < iter; i++) {
            getBorderPosition(rand,grid,minw,maxw+1,minh,maxh+1,0,grid.getDepth(),pos);
            //System.out.println("Start: " + pos[0] + " " + pos[1] + " " + pos[2]);
            randomWalk(rand,grid, minw,maxw,minh,maxh,mind,maxd, pos,0.5f);
            if (i % 1000 == 0) {
                System.out.println("Iter: " + i);
            }
        }

        try {
            ErrorReporter console = new PlainTextErrorReporter();
            //writeDebug(grid, handler, console);
            //writeMarchingCubesMethod(grid, handler, console);
            writeIsoSurfaceMethod(grid);
        } catch(Exception e) {
            e.printStackTrace();

            return new KernelResults(false, KernelResults.INTERNAL_ERROR, "Failed Writing Grid", null, null);
        }

        double[] min_bounds = new double[3];
        double[] max_bounds = new double[3];
        grid.getWorldCoords(0,0,0, min_bounds);
        grid.getWorldCoords(grid.getWidth() - 1, grid.getHeight() - 1, grid.getDepth() - 1, max_bounds);


        System.out.println("-------------------------------------------------");
        return new KernelResults(true, min_bounds, max_bounds);
    }

    private void getBorderPosition(Random rand, Grid grid, int min_width, int max_width, int min_height, int max_height, int min_depth, int max_depth, int[] pos) {
        float xr = rand.nextFloat();
        float yr = rand.nextFloat();
        float zr = rand.nextFloat();

        pos[0] = (int) (Math.round(max_width - min_width) * xr);
        pos[1] = (int) (Math.round(max_height - min_height) * yr);
        if (zr < 0.5) {
            pos[2] = 0;
        } else {
            pos[2] = grid.getDepth() - 1;
        }
    }

    /**
     * Randomly walk till the particle finds a hit.
     *
     * @param grid
     * @param pos
     */
    private void randomWalk(Random rand, Grid grid, int min_width, int max_width, int min_height, int max_height, int min_depth, int max_depth, int[] pos, float prob) {
        int dx,dy,dz;

        int max = 1000 * max_width * max_height * max_depth;
        int count = 0;

        while(true) {
            dx = rand.nextInt(3) - 1;
            dy = rand.nextInt(3) - 1;
            dz = rand.nextInt(3) - 1;

            pos[0] = dx + pos[0];
            pos[1] = dy + pos[1];
            pos[2] = dz + pos[2];

            if (pos[0] < min_width) {
                pos[0] = min_width;
            } else if (pos[0] > max_width) {
                pos[0] = max_width;
            }
            if (pos[1] < min_height) {
                pos[1] = min_height;
            } else if (pos[1] > max_height) {
                pos[1] = max_height;
            }

            if (pos[2] < min_depth) {
                pos[2] = min_depth;
            } else if (pos[2] > (max_depth)) {
                pos[2] = max_depth;
            }

            if (is6Connected(grid, min_width, max_width, min_height, max_height, min_depth, max_depth, pos[0],pos[1],pos[2])) {
                float r = rand.nextFloat();
                if (r < prob) {
                    //System.out.println("New pos: " + pos[0] + "," + pos[1] + "," + pos[2]);
                    grid.setState(pos[0],pos[1],pos[2], Grid.INSIDE);
                    //System.out.println("Mirror pos: " + pos[0] + "," + ((grid.getHeight() ) - pos[1]) + "," + pos[2]);
                    grid.setState(pos[0],grid.getHeight() - pos[1], pos[2], Grid.INSIDE);

                    grid.setState(grid.getWidth() - pos[0],pos[1],pos[2], Grid.INSIDE);
                    grid.setState(grid.getWidth() - pos[0],grid.getHeight() - pos[1], pos[2], Grid.INSIDE);
                }
                return;
            }

            count++;

            if (count > max) {
                System.out.println("Exceeded max walk count: " + count + " lastPos: " + pos[0] + " " + pos[1] + " " + pos[2]);
                return;
            }
        }
    }

    /**
     * Are any of the 6 connected voxels marked?
     *
     * @param grid
     * @param x
     * @param y
     * @param z
     * @return
     */
    private boolean is6Connected(Grid grid, int min_width, int max_width, int min_height, int max_height, int min_depth, int max_depth, int x, int y, int z) {
        if (x > min_width) {
            if (grid.getState(x - 1, y, z) != Grid.OUTSIDE) {
                return true;
            }
        }

        if (x < max_width) {
            if (grid.getState(x + 1, y, z) != Grid.OUTSIDE) {
                return true;
            }
        }

        if (y > min_height) {
            if (grid.getState(x, y - 1, z) != Grid.OUTSIDE) {
                return true;
            }
        }

        if (y < max_height) {
            if (grid.getState(x, y + 1, z) != Grid.OUTSIDE) {
                return true;
            }
        }

        if (z > min_depth) {
            if (grid.getState(x, y, z - 1) != Grid.OUTSIDE) {
                return true;
            }
        }

        if (z < max_depth) {
            if (grid.getState(x, y, z + 1) != Grid.OUTSIDE) {
                return true;
            }
        }

        return false;
    }

    /**
     * Are any of the 18 connected voxels marked?
     *
     * @param grid
     * @param x
     * @param y
     * @param z
     * @return
     */
    private boolean is18Connected(Grid grid, int min_width, int max_width, int min_height, int max_height, int min_depth, int max_depth, int x, int y, int z) {
        if (x > min_width) {
            if (grid.getState(x - 1, y, z) != Grid.OUTSIDE) {
                return true;
            }
            if (y > min_height) {
                if (grid.getState(x - 1, y - 1, z) != Grid.OUTSIDE) {
                    return true;
                }
            }
            if (y < max_height) {
                if (grid.getState(x - 1, y + 1, z) != Grid.OUTSIDE) {
                    return true;
                }
            }

            if (z > min_depth) {
                if (grid.getState(x - 1, y, z - 1) != Grid.OUTSIDE) {
                    return true;
                }
            }

            if (z < max_depth) {
                if (grid.getState(x - 1, y, z + 1) != Grid.OUTSIDE) {
                    return true;
                }
            }
        }

        if (x < max_width) {
            if (grid.getState(x + 1, y, z) != Grid.OUTSIDE) {
                return true;
            }
            if (y > min_height) {
                if (grid.getState(x + 1, y - 1, z) != Grid.OUTSIDE) {
                    return true;
                }
            }
            if (y < max_height) {
                if (grid.getState(x + 1, y + 1, z) != Grid.OUTSIDE) {
                    return true;
                }
            }

            if (z > min_depth) {
                if (grid.getState(x + 1, y, z - 1) != Grid.OUTSIDE) {
                    return true;
                }
            }

            if (z < max_depth) {
                if (grid.getState(x + 1, y, z + 1) != Grid.OUTSIDE) {
                    return true;
                }
            }
        }

        if (y > min_height) {
            if (grid.getState(x, y - 1, z) != Grid.OUTSIDE) {
                return true;
            }

            if (x > min_width) {
                if (grid.getState(x - 1, y - 1, z) != Grid.OUTSIDE) {
                    return true;
                }
            }
            if (x < max_width) {
                if (grid.getState(x + 1, y - 1, z) != Grid.OUTSIDE) {
                    return true;
                }
            }
        }

        if (y < max_height) {
            if (grid.getState(x, y + 1, z) != Grid.OUTSIDE) {
                return true;
            }

            if (x > min_width) {
                if (grid.getState(x - 1, y + 1, z) != Grid.OUTSIDE) {
                    return true;
                }
            }
            if (x < max_width) {
                if (grid.getState(x + 1, y + 1, z) != Grid.OUTSIDE) {
                    return true;
                }
            }
        }

        if (z > min_depth) {
            if (grid.getState(x, y, z - 1) != Grid.OUTSIDE) {
                return true;
            }
        }

        if (z < max_depth) {
            if (grid.getState(x, y, z + 1) != Grid.OUTSIDE) {
                return true;
            }
        }

        return false;
    }

    /**
     * Are any of the 22 connected voxels marked?
     *
     * @param grid
     * @param x
     * @param y
     * @param z
     * @return
     */
    private boolean is22Connected(Grid grid, int min_width, int max_width, int min_height, int max_height, int min_depth, int max_depth, int x, int y, int z) {
        if (x > min_width) {
            if (grid.getState(x - 1, y, z) != Grid.OUTSIDE) {
                return true;
            }
            if (y > min_height) {
                if (grid.getState(x - 1, y - 1, z) != Grid.OUTSIDE) {
                    return true;
                }

                if (z > min_depth) {
                    if (grid.getState(x - 1, y - 1, z - 1) != Grid.OUTSIDE) {
                        return true;
                    }
                }
                if (z < max_depth) {
                    if (grid.getState(x - 1, y - 1, z + 1) != Grid.OUTSIDE) {
                        return true;
                    }
                }
            }
            if (y < max_height) {
                if (grid.getState(x - 1, y + 1, z) != Grid.OUTSIDE) {
                    return true;
                }

                if (z > min_depth) {
                    if (grid.getState(x - 1, y + 1, z - 1) != Grid.OUTSIDE) {
                        return true;
                    }
                }
                if (z < max_depth) {
                    if (grid.getState(x - 1, y + 1, z + 1) != Grid.OUTSIDE) {
                        return true;
                    }
                }

            }

            if (z > min_depth) {
                if (grid.getState(x - 1, y, z - 1) != Grid.OUTSIDE) {
                    return true;
                }
            }

            if (z < max_depth) {
                if (grid.getState(x - 1, y, z + 1) != Grid.OUTSIDE) {
                    return true;
                }
            }
        }

        if (x < max_width) {
            if (grid.getState(x + 1, y, z) != Grid.OUTSIDE) {
                return true;
            }
            if (y > min_height) {
                if (grid.getState(x + 1, y - 1, z) != Grid.OUTSIDE) {
                    return true;
                }

                if (z > min_depth) {
                    if (grid.getState(x + 1, y - 1, z - 1) != Grid.OUTSIDE) {
                        return true;
                    }
                }
                if (z < max_depth) {
                    if (grid.getState(x + 1, y - 1, z + 1) != Grid.OUTSIDE) {
                        return true;
                    }
                }

            }
            if (y < max_height) {
                if (grid.getState(x + 1, y + 1, z) != Grid.OUTSIDE) {
                    return true;
                }

                if (z > min_depth) {
                    if (grid.getState(x + 1, y + 1, z - 1) != Grid.OUTSIDE) {
                        return true;
                    }
                }
                if (z < max_depth) {
                    if (grid.getState(x + 1, y + 1, z + 1) != Grid.OUTSIDE) {
                        return true;
                    }
                }

            }

            if (z > min_depth) {
                if (grid.getState(x + 1, y, z - 1) != Grid.OUTSIDE) {
                    return true;
                }
            }

            if (z < max_depth) {
                if (grid.getState(x + 1, y, z + 1) != Grid.OUTSIDE) {
                    return true;
                }
            }
        }

        if (y > min_height) {
            if (grid.getState(x, y - 1, z) != Grid.OUTSIDE) {
                return true;
            }

            if (x > min_width) {
                if (grid.getState(x - 1, y - 1, z) != Grid.OUTSIDE) {
                    return true;
                }
            }
            if (x < max_width) {
                if (grid.getState(x + 1, y - 1, z) != Grid.OUTSIDE) {
                    return true;
                }
            }
        }

        if (y < max_height) {
            if (grid.getState(x, y + 1, z) != Grid.OUTSIDE) {
                return true;
            }

            if (x > min_width) {
                if (grid.getState(x - 1, y + 1, z) != Grid.OUTSIDE) {
                    return true;
                }
            }
            if (x < max_width) {
                if (grid.getState(x + 1, y + 1, z) != Grid.OUTSIDE) {
                    return true;
                }
            }
        }

        if (z > min_depth) {
            if (grid.getState(x, y, z - 1) != Grid.OUTSIDE) {
                return true;
            }
        }

        if (z < max_depth) {
            if (grid.getState(x, y, z + 1) != Grid.OUTSIDE) {
                return true;
            }
        }

        return false;
    }

    /**
     * Pull the params into local variables
     *
     * @param params The parameters
     */
    private void pullParams(Map<String,Object> params) {
        String pname = null;

        try {
            pname = "resolution";
            resolution = ((Double) params.get(pname)).doubleValue();

            pname = "bodyWidth";
            bodyWidth = ((Double) params.get(pname)).doubleValue();

            pname = "bodyHeight";
            bodyHeight = ((Double) params.get(pname)).doubleValue();

            pname = "bodyImage";
            filename = (String) params.get(pname);

            pname = "bodyImage2";
            filename2 = (String) params.get(pname);

            pname = "bodyImageInvert";
            invert = ((Boolean) params.get(pname)).booleanValue();

            pname = "bodyImageDepth";
            bodyImageDepth = ((Double) params.get(pname)).doubleValue();

            pname = "bodyImageDepth2";
            bodyImageDepth2 = ((Double) params.get(pname)).doubleValue();

        } catch(Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error parsing: " + pname + " val: " + params.get(pname));
        }
    }

    private void writeMarchingCubesMethod(Grid grid, BinaryContentHandler handler, ErrorReporter console) {

        // Output File
        //BoxesX3DExporter exporter = new BoxesX3DExporter(type, os, console);
/*
System.out.println("Creating Regions Exporter");
        RegionsX3DExporter exporter = new RegionsX3DExporter(handler, console, true);
        float[] mat_color = new float[] {0.8f,0.8f,0.8f,0};
        HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
        colors.put(new Integer(1), mat_color);

        exporter.write(grid, colors);
        exporter.close();
*/

        // Sadlt this number needs to change based on resolution
//        EdgeCollapseSimplifier reducer = new EdgeCollapseSimplifier(16, 0.71);
        EdgeCollapseSimplifier reducer = new EdgeCollapseSimplifier(500, 0.61);

        // Use Meshlab instead right now.
        reducer = null;

        MarchingCubesX3DExporter exporter = new MarchingCubesX3DExporter(handler, console, true, reducer);

        Map<Long, float[]> matColors = new HashMap<Long, float[]>();
        matColors.put(0l, new float[] {0.8f,0.8f,0.8f,1f});
        exporter.write(grid, matColors);
        exporter.close();

    }

    private void writeIsoSurfaceMethod(Grid grid) throws IOException {
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        double vs = grid.getVoxelSize();
        int smoothSteps = 0;

        double gbounds[] = new double[]{-nx*vs/2,nx*vs/2,-ny*vs/2,ny*vs/2,-nz*vs/2,nz*vs/2};
        double ibounds[] = extendBounds(gbounds, -vs/2);

        IsosurfaceMaker im = new IsosurfaceMaker();
        im.setIsovalue(0.);
        im.setBounds(ibounds);
        im.setGridSize(nx, ny, nz);

        STLWriter stlmaker = new STLWriter("out.stl");
        im.makeIsosurface(new IsosurfaceMaker.SliceGrid(grid, gbounds, smoothSteps), stlmaker);

        stlmaker.close();

    }

    /**
     return bounds extended by given margin
     */
    static double[] extendBounds(double bounds[], double margin){
        return new double[]{
                bounds[0] - margin,
                bounds[1] + margin,
                bounds[2] - margin,
                bounds[3] + margin,
                bounds[4] - margin,
                bounds[5] + margin,
        };
    }

    private void writeDebug(Grid grid, BinaryContentHandler handler, ErrorReporter console) {
        // Output File

        BoxesX3DExporter exporter = new BoxesX3DExporter(handler, console,true);

        HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
        colors.put(new Integer(Grid.INSIDE), new float[] {1,0,0});
        colors.put(new Integer(Grid.INSIDE), new float[] {0,1,0});
        colors.put(new Integer(Grid.OUTSIDE), new float[] {0,0,1});

        HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
        transparency.put(new Integer(Grid.INSIDE), new Float(0));
        transparency.put(new Integer(Grid.INSIDE), new Float(0.5));
        transparency.put(new Integer(Grid.OUTSIDE), new Float(0.98));

        exporter.writeDebug(grid, colors, transparency);
        exporter.close();
    }

    /**
     * Create a cube.
     */
    private void createCube(Grid grid, double tx, double ty, double tz, double w, double h, double d, int mat) {
        // Create the base structure

        CubeCreator cg = null;
        cg = new CubeCreator(null, w, h, d, tx,ty,tz,mat);
        cg.generate(grid);

    }

    /**
     * Create a cylinder.
     */
    private void createCylinder(Grid grid, double tx, double ty, double tz,
        double rx, double ry, double rz, double ra,
        double height, double radius, int facets, int mat) {

        CylinderCreator cg = null;
        cg = new CylinderCreator(height, radius, tx,ty,tz,rx,ry,rz,ra,mat);
        cg.generate(grid);
    }

    /**
     * Create a torus.
     */
    private void createTorus(Grid grid, double tx, double ty, double tz,
        double rx, double ry, double rz, double ra,
        double ir, double or, int facets, int mat, boolean filled) {

System.out.println("createTorus: " + ir + " or: " + or);
        TorusGenerator tg = new TorusGenerator((float)ir, (float)or, facets, facets);
        GeometryData geom = new GeometryData();
        geom.geometryType = GeometryData.INDEXED_TRIANGLES;
        tg.generate(geom);

        TriangleModelCreator tmc = new TriangleModelCreator(geom,tx,ty,tz,
            rx,ry,rz,ra,mat,mat,filled);

        tmc.generate(grid);
    }

    private void createTorus(Grid grid, double tx, double ty, double tz,
                             double rx, double ry, double rz, double ra,
                             double ir, double or, int zlimit, int facets, int mat, boolean filled) {

        System.out.println("createTorus: " + ir + " or: " + or + " zlimit: " + zlimit + " total: " + grid.getDepth());
        TorusGenerator tg = new TorusGenerator((float)ir, (float)or, facets, facets);
        GeometryData geom = new GeometryData();
        geom.geometryType = GeometryData.INDEXED_TRIANGLES;
        tg.generate(geom);

        TriangleModelCreator tmc = new TriangleModelCreator(geom,tx,ty,tz,
                rx,ry,rz,ra,mat,mat,filled);

        tmc.generate(grid);

        int w = grid.getWidth();
        int h = grid.getHeight();
        int d = grid.getDepth();
        int removed = 0;

        for(int y=0; y < h; y++) {
            for(int x=0; x < w; x++) {
                for(int z=zlimit; z < d; z++) {
                    if (grid.getState(x,y,z) != Grid.OUTSIDE) {
                        removed++;
                        grid.setState(x,y,z, Grid.OUTSIDE);
                    }
                }
            }
        }
        System.out.println("Removed torus: " + removed);
    }

}

class RegionClearer implements RegionTraverser {
    private Grid grid;

    public RegionClearer(Grid grid) {
        this.grid = grid;
    }
    @Override
    public void found(int x, int y, int z) {
        grid.setState(x,y,z,Grid.OUTSIDE);
    }

    @Override
    public boolean foundInterruptible(int x, int y, int z) {
        grid.setState(x,y,z,Grid.OUTSIDE);

        return true;
    }
}
