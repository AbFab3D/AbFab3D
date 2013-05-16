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

package abfab3d.geom;

// External Imports
import java.util.*;
import java.io.*;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.DualWrapper;
import org.web3d.vrml.sav.ContentHandler;

// Internal Imports
import abfab3d.grid.Grid;

/**
 * Creates a cube following the patterns specified.
 *
 * Face numbers start with front face.  The order
 * is front,top,back,bottom,left,right
 *
 * @author Alan Hudson
 */
public class CubeCreator extends GeometryCreator {
    public enum Style {
        FILLED,
        TOP_ROW,
        BOTTOM_ROW,
        LEFT_ROW,
        RIGHT_ROW,
        EMPTY
    }

    /** List of Styles by face. */
    private Style[][] styles;

    protected double width;
    protected double height;
    protected double depth;

    // The center position
    protected double x;
    protected double y;
    protected double z;

    protected long materialID;

    /**
     * Constructor.
     *
     * @param styles The styles for each face.  Front,Back,Left,Right,Top,Bottom
     */
    public CubeCreator(Style[][] styles,
        double w, double h, double d,
        double x, double y, double z,
        long material) {

        if (styles != null) {
            this.styles = new Style[6][styles.length];

            for(int i=0; i < 6; i++) {
                if (styles[i] == null)
                    continue;

                for(int j=0; j < styles[i].length; j++) {
                    this.styles[i][j] = styles[i][j];
                }
            }
        }

        width = w;
        height = h;
        depth = d;
        this.x = x;
        this.y = y;
        this.z = z;
        this.materialID = material;
    }

    /**
     * Generate the geometry and issue commands to the provided handler.
     *
     * @param grid The dest grid
     */
    public void generate(Grid grid) {
        AttributeGrid wrapper = null;

        if (grid instanceof AttributeGrid) {
            wrapper = (AttributeGrid) grid;
        } else {
            wrapper = new DualWrapper(grid);
        }

        if (styles == null) {
            createSolidCube(wrapper);
            return;
        }

        Style[] side_styles = styles[0];

        double x_pos,y_pos,z_pos;
        double vsize = wrapper.getVoxelSize();
        double hvsize = vsize / 2.0;
        double sheight = wrapper.getSliceHeight();
        double hsheight = wrapper.getSliceHeight() / 2.0;
        int[] coords1 = new int[3];
        int[] coords2 = new int[3];
        int start,end;


//System.out.println("hvsize: " + hvsize + " hsheight: " + hsheight);
        int len;
        // Front.  XY plane

//System.out.println("front style: " + side_styles[0]);
        if (side_styles != null) {
//System.out.println("Front");
            for(int i=0; i < side_styles.length; i++) {

                if (side_styles[i] == null)
                    continue;

                double ulx,uly,ulz;     // upper left corner
                double llx,lly,llz;     // lower left corner
                double lrx,lry,lrz;     // lower right corner
                double urx,ury,urz;     // upper right corner

                ulx = x - width / 2.0;
                uly = y + height / 2.0;
                ulz = z + depth / 2.0;

                urx = x + width / 2.0;
                ury = y + height / 2.0;
                urz = z + depth / 2.0;

                llx = x - width / 2.0;
                lly = y - height / 2.0;
                llz = z + depth / 2.0;

                lrx = x + width / 2.0;
                lry = y - height / 2.0;
                lrz = z + depth / 2.0;

//System.out.println("ul: " + ulx + " " + uly + " " + ulz);
//System.out.println("ur: " + urx + " " + ury + " " + urz);
//System.out.println("ll: " + llx + " " + lly + " " + llz);
//System.out.println("lr: " + lrx + " " + lry + " " + lrz);

                if (side_styles[i] == Style.FILLED) {
                    z_pos = ulz;
                    y_pos = 0;

                    wrapper.getGridCoords(ulx,0,z_pos,coords1);
                    wrapper.getGridCoords(urx,height,z_pos,coords2);

                    len = coords2[0] - coords1[0];

                    for(int x_idx = coords1[0]; x_idx <= coords2[0]; x_idx++) {
                        for(int y_idx = coords1[1]; y_idx <= coords2[1]; y_idx++) {
                            wrapper.setData(x_idx, y_idx, coords1[2],Grid.EXTERIOR,materialID);
                        }
                    }
                } else if (side_styles[i] == Style.TOP_ROW) {
                    y_pos = uly;
                    z_pos = ulz;

                    wrapper.getGridCoords(ulx,y_pos,z_pos,coords1);
                    wrapper.getGridCoords(urx,y_pos,z_pos,coords2);

                    len = coords2[0] - coords1[0];

//System.out.println("Top Row: " + len);

                    for(int x_idx = coords1[0]; x_idx <= coords2[0]; x_idx++) {
                        wrapper.setData(x_idx, coords1[1], coords1[2],Grid.EXTERIOR,materialID);
                    }
                } else  if (side_styles[i] == Style.BOTTOM_ROW) {
//System.out.println("Bottom Row");
                    y_pos = lly;
                    z_pos = llz;

                    wrapper.getGridCoords(llx,y_pos,z_pos,coords1);
                    wrapper.getGridCoords(lrx,y_pos,z_pos,coords2);

                    len = coords2[0] - coords1[0];

//System.out.println("Bottom Row: " + len);

                    for(int x_idx = coords1[0]; x_idx <= coords2[0]; x_idx++) {
                        wrapper.setData(x_idx, coords1[1], coords1[2],Grid.EXTERIOR,materialID);
                    }
                } else  if (side_styles[i] == Style.LEFT_ROW) {
                    x_pos = llx;
                    z_pos = llz;

                    wrapper.getGridCoords(x_pos,lly,z_pos,coords1);
                    wrapper.getGridCoords(x_pos,uly,z_pos,coords2);

                    len = coords2[0] - coords1[0];
//System.out.println("Left Row: " + len);

                    for(int y_idx = coords1[1]; y_idx <= coords2[1]; y_idx++) {
                        wrapper.setData(coords1[0],y_idx, coords1[2],Grid.EXTERIOR,materialID);
                    }
                } else  if (side_styles[i] == Style.RIGHT_ROW) {
                    x_pos = lrx;
                    z_pos = lrz;

                    wrapper.getGridCoords(x_pos,lly,z_pos,coords1);
                    wrapper.getGridCoords(x_pos,uly,z_pos,coords2);

                    len = coords2[0] - coords1[0];

                    for(int y_idx = coords1[1]; y_idx <= coords2[1]; y_idx++) {
                        wrapper.setData(coords1[0],y_idx, coords1[2],Grid.EXTERIOR,materialID);
                    }
                }
            }
        }

        side_styles = styles[1];

        // Back.  XY plane

        if (side_styles != null) {
//System.out.println("Back");
            for(int i=0; i < side_styles.length; i++) {
                if (side_styles[i] == null)
                    continue;

                double ulx,uly,ulz;     // upper left corner
                double llx,lly,llz;     // lower left corner
                double lrx,lry,lrz;     // lower right corner
                double urx,ury,urz;     // upper right corner

                ulx = x - width / 2.0;
                uly = y + height / 2.0;
                ulz = z - depth / 2.0;

                urx = x + width / 2.0;
                ury = y + height / 2.0;
                urz = z - depth / 2.0;

                llx = x - width / 2.0;
                lly = y - height / 2.0;
                llz = z - depth / 2.0;

                lrx = x + width / 2.0;
                lry = y - height / 2.0;
                lrz = z - depth / 2.0;
/*
System.out.println("ul: " + ulx + " " + uly + " " + ulz);
System.out.println("ur: " + urx + " " + ury + " " + urz);
System.out.println("ll: " + llx + " " + lly + " " + llz);
System.out.println("lr: " + lrx + " " + lry + " " + lrz);
*/
                if (side_styles[i] == Style.FILLED) {
                    z_pos = ulz;
                    y_pos = 0;

                    wrapper.getGridCoords(ulx,0,z_pos,coords1);
                    wrapper.getGridCoords(urx,height,z_pos,coords2);

                    len = coords2[0] - coords1[0];

                    for(int x_idx = coords1[0]; x_idx <= coords2[0]; x_idx++) {
                        for(int y_idx = coords1[1]; y_idx <= coords2[1]; y_idx++) {
                            wrapper.setData(x_idx, y_idx, coords1[2],Grid.EXTERIOR,materialID);
                        }
                    }
                } else if (side_styles[i] == Style.TOP_ROW) {
                    y_pos = uly;
                    z_pos = ulz;


                    wrapper.getGridCoords(ulx,y_pos,z_pos,coords1);
                    wrapper.getGridCoords(urx,y_pos,z_pos,coords2);

                    len = coords2[0] - coords1[0];
//System.out.println("Top Row: " + len);

                    for(int x_idx = coords1[0]; x_idx <= coords2[0]; x_idx++) {
                        wrapper.setData(x_idx, coords1[1], coords1[2],Grid.EXTERIOR,materialID);
                    }
                } else  if (side_styles[i] == Style.BOTTOM_ROW) {
                    y_pos = lly;
                    z_pos = llz;

                    wrapper.getGridCoords(ulx,y_pos,z_pos,coords1);
                    wrapper.getGridCoords(urx,y_pos,z_pos,coords2);

                    len = coords2[0] - coords1[0];
//System.out.println("Bottom Row: " + len);

                    for(int x_idx = coords1[0]; x_idx <= coords2[0]; x_idx++) {
                        wrapper.setData(x_idx, coords1[1], coords1[2],Grid.EXTERIOR,materialID);
                    }
                } else  if (side_styles[i] == Style.LEFT_ROW) {
                    x_pos = llx;
                    z_pos = llz;

                    wrapper.getGridCoords(x_pos,lly,z_pos,coords1);
                    wrapper.getGridCoords(x_pos,uly,z_pos,coords2);

                    len = coords2[0] - coords1[0];
//System.out.println("Left Row: " + len);

                    for(int y_idx = coords1[1]; y_idx <= coords2[1]; y_idx++) {
                        wrapper.setData(coords1[0],y_idx, coords1[2],Grid.EXTERIOR,materialID);
                    }
                } else  if (side_styles[i] == Style.RIGHT_ROW) {
                    x_pos = lrx;
                    z_pos = lrz;

                    wrapper.getGridCoords(x_pos,lry,z_pos,coords1);
                    wrapper.getGridCoords(x_pos,ury,z_pos,coords2);

                    len = coords2[0] - coords1[0];
//System.out.println("Right Row: " + len);

                    for(int y_idx = coords1[1]; y_idx <= coords2[1]; y_idx++) {
                        wrapper.setData(coords1[0],y_idx, coords1[2],Grid.EXTERIOR,materialID);
                    }
                }
            }
        }

        side_styles = styles[2];

        // Left.  XZ plane

        if (side_styles != null) {
//System.out.println("Left");

            for(int i=0; i < side_styles.length; i++) {
                if (side_styles[i] == null)
                    continue;

                double ulx,uly,ulz;     // upper left corner
                double llx,lly,llz;     // lower left corner
                double lrx,lry,lrz;     // lower right corner
                double urx,ury,urz;     // upper right corner

                ulx = x - width / 2.0;
                uly = y + height / 2.0;
                ulz = z - depth / 2.0;

                urx = x - width / 2.0;
                ury = y + height / 2.0;
                urz = z + depth / 2.0;

                llx = x - width / 2.0;
                lly = y - height / 2.0;
                llz = z - depth / 2.0;

                lrx = x - width / 2.0;
                lry = y - height / 2.0;
                lrz = z + depth / 2.0;
/*
System.out.println("ul: " + ulx + " " + uly + " " + ulz);
System.out.println("ur: " + urx + " " + ury + " " + urz);
System.out.println("ll: " + llx + " " + lly + " " + llz);
System.out.println("lr: " + lrx + " " + lry + " " + lrz);
*/

                if (side_styles[i] == Style.FILLED) {
                    x_pos = ulx;
                    y_pos = uly;

                    wrapper.getGridCoords(x_pos,0,ulz,coords1);
                    wrapper.getGridCoords(x_pos,height,urz,coords2);

                    len = coords2[0] - coords1[0];

                    for(int z_idx = coords1[2]; z_idx <= coords2[2]; z_idx++) {
                        for(int y_idx = coords1[1]; y_idx <= coords2[1]; y_idx++) {
                            wrapper.setData(coords1[0], y_idx, z_idx,Grid.EXTERIOR,materialID);
                        }
                    }
                } else if (side_styles[i] == Style.TOP_ROW) {
                    x_pos = ulx;
                    y_pos = uly;

                    wrapper.getGridCoords(x_pos,y_pos,ulz,coords1);
                    wrapper.getGridCoords(x_pos,y_pos,urz,coords2);

                    len = coords2[0] - coords1[0];

                    for(int z_idx = coords1[2]; z_idx <= coords2[2]; z_idx++) {
                        wrapper.setData(coords1[0], coords1[1], z_idx,Grid.EXTERIOR,materialID);
                    }
                } else  if (side_styles[i] == Style.BOTTOM_ROW) {
                    x_pos = llx;
                    y_pos = lly;

                    wrapper.getGridCoords(x_pos,y_pos,llz,coords1);
                    wrapper.getGridCoords(x_pos,y_pos,lrz,coords2);

                    len = coords2[0] - coords1[0];

                    for(int z_idx = coords1[2]; z_idx <= coords2[2]; z_idx++) {
                        wrapper.setData(coords1[0], coords1[1], z_idx,Grid.EXTERIOR,materialID);
                    }
                } else  if (side_styles[i] == Style.LEFT_ROW) {
//System.out.println("Left Row");
                    x_pos = llx;
                    z_pos = llz;

                    wrapper.getGridCoords(x_pos,lly,z_pos,coords1);
                    wrapper.getGridCoords(x_pos,uly,z_pos,coords2);

                    len = coords2[0] - coords1[0];

                    for(int y_idx = coords1[1]; y_idx <= coords2[1]; y_idx++) {
                        wrapper.setData(coords1[0],y_idx, coords1[2],Grid.EXTERIOR,materialID);
                    }
                } else  if (side_styles[i] == Style.RIGHT_ROW) {
//System.out.println("Right Row");
                    x_pos = lrx;
                    z_pos = lrz;

                    wrapper.getGridCoords(x_pos,lly,z_pos,coords1);
                    wrapper.getGridCoords(x_pos,uly,z_pos,coords2);

                    len = coords2[0] - coords1[0];

                    for(int y_idx = coords1[1]; y_idx <= coords2[1]; y_idx++) {
                        wrapper.setData(coords1[0],y_idx, coords1[2],Grid.EXTERIOR,materialID);
                    }
                }
            }
        }

        side_styles = styles[3];

        // Right.  XZ plane

        if (side_styles != null) {
//System.out.println("Right");
            for(int i=0; i < side_styles.length; i++) {
                if (side_styles[i] == null)
                    continue;

                double ulx,uly,ulz;     // upper left corner
                double llx,lly,llz;     // lower left corner
                double lrx,lry,lrz;     // lower right corner
                double urx,ury,urz;     // upper right corner

                ulx = x + width / 2.0;
                uly = y + height / 2.0;
                ulz = z - depth / 2.0;

                urx = x + width / 2.0;
                ury = y + height / 2.0;
                urz = z + depth / 2.0;

                llx = x + width / 2.0;
                lly = y - height / 2.0;
                llz = z - depth / 2.0;

                lrx = x + width / 2.0;
                lry = y - height / 2.0;
                lrz = z + depth / 2.0;


//System.out.println("ul: " + ulx + " " + uly + " " + ulz);
//System.out.println("ur: " + urx + " " + ury + " " + urz);
//System.out.println("ll: " + llx + " " + lly + " " + llz);
//System.out.println("lr: " + lrx + " " + lry + " " + lrz);

                if (side_styles[i] == Style.FILLED) {
                    x_pos = ulx;
                    y_pos = uly;

                    wrapper.getGridCoords(x_pos,0,ulz,coords1);
                    wrapper.getGridCoords(x_pos,height,urz,coords2);

                    len = coords2[0] - coords1[0];

                    for(int z_idx = coords1[2]; z_idx <= coords2[2]; z_idx++) {
                        for(int y_idx = coords1[1]; y_idx <= coords2[1]; y_idx++) {
                            wrapper.setData(coords1[0], y_idx, z_idx,Grid.EXTERIOR,materialID);
                        }
                    }
                } else if (side_styles[i] == Style.TOP_ROW) {
                    x_pos = ulx;
                    y_pos = uly;

                    wrapper.getGridCoords(x_pos,y_pos,ulz,coords1);
                    wrapper.getGridCoords(x_pos,y_pos,urz,coords2);

                    len = coords2[0] - coords1[0];

                    for(int z_idx = coords1[2]; z_idx <= coords2[2]; z_idx++) {
                        wrapper.setData(coords1[0], coords1[1], z_idx,Grid.EXTERIOR,materialID);
                    }
                } else  if (side_styles[i] == Style.BOTTOM_ROW) {
                    x_pos = llx;
                    y_pos = lly;

                    wrapper.getGridCoords(x_pos,y_pos,llz,coords1);
                    wrapper.getGridCoords(x_pos,y_pos,lrz,coords2);

                    len = coords2[0] - coords1[0];

                    for(int z_idx = coords1[2]; z_idx <= coords2[2]; z_idx++) {
                        wrapper.setData(coords1[0], coords1[1], z_idx,Grid.EXTERIOR,materialID);
                    }
                } else  if (side_styles[i] == Style.LEFT_ROW) {
//System.out.println("Left Row");
                    x_pos = llx;
                    z_pos = llz;

                    wrapper.getGridCoords(x_pos,lly,z_pos,coords1);
                    wrapper.getGridCoords(x_pos,uly,z_pos,coords2);

                    len = coords2[0] - coords1[0];

                    for(int y_idx = coords1[1]; y_idx <= coords2[1]; y_idx++) {
                        wrapper.setData(coords1[0],y_idx, coords1[2],Grid.EXTERIOR,materialID);
                    }
                } else  if (side_styles[i] == Style.RIGHT_ROW) {
//System.out.println("Right Row");
                    x_pos = lrx;
                    z_pos = lrz;

                    wrapper.getGridCoords(x_pos,lry,z_pos,coords1);
                    wrapper.getGridCoords(x_pos,ury,z_pos,coords2);

                    len = coords2[0] - coords1[0];

                    for(int y_idx = coords1[1]; y_idx <= coords2[1]; y_idx++) {
                        wrapper.setData(coords1[0],y_idx, coords1[2],Grid.EXTERIOR,materialID);
                    }
                }
            }
        }

        side_styles = styles[4];

        // Top.

        if (side_styles != null) {
//System.out.println("Top");
            for(int i=0; i < side_styles.length; i++) {
                if (side_styles[i] == null)
                    continue;

                double ulx,uly,ulz;     // upper left corner
                double llx,lly,llz;     // lower left corner
                double lrx,lry,lrz;     // lower right corner
                double urx,ury,urz;     // upper right corner

                ulx = x - width / 2.0;
                uly = y + height / 2.0;
                ulz = z - depth / 2.0;

                urx = x + width / 2.0;
                ury = y + height / 2.0;
                urz = z + depth / 2.0;

                llx = x - width / 2.0;
                lly = y + height / 2.0;
                llz = z - depth / 2.0;

                lrx = x + width / 2.0;
                lry = y + height / 2.0;
                lrz = z + depth / 2.0;


//System.out.println("ul: " + ulx + " " + uly + " " + ulz);
//System.out.println("ur: " + urx + " " + ury + " " + urz);
//System.out.println("ll: " + llx + " " + lly + " " + llz);
//System.out.println("lr: " + lrx + " " + lry + " " + lrz);

                if (side_styles[i] == Style.FILLED) {
                    x_pos = ulx;
                    y_pos = uly;

// TODO: Why not y + height / 2?
                    wrapper.getGridCoords(llx,height,ulz,coords1);
                    wrapper.getGridCoords(lrx,height,urz,coords2);

                    for(int x_idx = coords1[0]; x_idx <= coords2[0]; x_idx++) {
                        for(int z_idx = coords1[2]; z_idx <= coords2[2]; z_idx++) {
                            wrapper.setData(x_idx, coords1[1], z_idx,Grid.EXTERIOR,materialID);
                        }
                    }
                } else {
                    System.out.println("TODO: need to implement other sryles for Top");
                }
            }
        }

        side_styles = styles[5];

        // Bottom

        if (side_styles != null) {
//System.out.println("Bottom");
            for(int i=0; i < side_styles.length; i++) {
                if (side_styles[i] == null)
                    continue;

                double ulx,uly,ulz;     // upper left corner
                double llx,lly,llz;     // lower left corner
                double lrx,lry,lrz;     // lower right corner
                double urx,ury,urz;     // upper right corner

                ulx = x - width / 2.0;
                uly = y - height / 2.0;
                ulz = z - depth / 2.0;

                urx = x + width / 2.0;
                ury = y - height / 2.0;
                urz = z + depth / 2.0;

                llx = x - width / 2.0;
                lly = y - height / 2.0;
                llz = z - depth / 2.0;

                lrx = x + width / 2.0;
                lry = y - height / 2.0;
                lrz = z + depth / 2.0;


//System.out.println("ul: " + ulx + " " + uly + " " + ulz);
//System.out.println("ur: " + urx + " " + ury + " " + urz);
//System.out.println("ll: " + llx + " " + lly + " " + llz);
//System.out.println("lr: " + lrx + " " + lry + " " + lrz);

                if (side_styles[i] == Style.FILLED) {
//System.out.println("filled");
                    x_pos = ulx;
                    y_pos = uly;

                    wrapper.getGridCoords(llx,0,ulz,coords1);
                    wrapper.getGridCoords(lrx,0,urz,coords2);

                    for(int x_idx = coords1[0]; x_idx <= coords2[0]; x_idx++) {
                        for(int z_idx = coords1[2]; z_idx <= coords2[2]; z_idx++) {
                            wrapper.setData(x_idx, coords1[1], z_idx,Grid.EXTERIOR,materialID);
                        }
                    }
                } else {
                    System.out.println("TODO: need to implement other sryles for Top");
                }
            }
        }

//        System.out.println("Final Grid:");
//        System.out.println(wrapper.toStringAll());
    }

    private void createSolidCube(Grid grid) {
        AttributeGrid wrapper = null;

        if (grid instanceof AttributeGrid) {
            wrapper = (AttributeGrid) grid;
        } else {
            wrapper = new DualWrapper(grid);
        }

        int[] coords1 = new int[3];
        int[] coords2 = new int[3];
        int start,end;

        double ulx,uly,ulz;     // upper left corner
        double llx,lly,llz;     // lower left corner
        double lrx,lry,lrz;     // lower right corner
        double urx,ury,urz;     // upper right corner

        ulx = x - width / 2.0;
        uly = y + height / 2.0;
        ulz = z - depth / 2.0;

        urx = x + width / 2.0;
        ury = y + height / 2.0;
        urz = z + depth / 2.0;

        llx = x - width / 2.0;
        lly = y - height / 2.0;
        llz = z - depth / 2.0;

        lrx = x + width / 2.0;
        lry = y - height / 2.0;
        lrz = z + depth / 2.0;

//System.out.println("ll: " + llx + " " + lly + " " + llz);
//System.out.println("ur: " + urx + " " + ury + " " + urz);
        wrapper.getGridCoords(llx, lly, llz, coords1);
        wrapper.getGridCoords(urx, ury, urz, coords2);

//System.out.println("center: " + x + " " + y + " " + z);
//System.out.println("dims: " + width + " " + height + " " + depth);
//System.out.println("llx: " + java.util.Arrays.toString(coords1));
//System.out.println("urx: " + java.util.Arrays.toString(coords2));
        int cnt = 0;
        for(int x_idx = coords1[0]; x_idx <= coords2[0]; x_idx++) {
            for(int y_idx = coords1[1]; y_idx <= coords2[1]; y_idx++) {
                for(int z_idx = coords1[2]; z_idx <= coords2[2]; z_idx++) {
                    wrapper.setData(x_idx, y_idx, z_idx,Grid.EXTERIOR,materialID);
                    cnt++;
                }
            }
        }

        System.out.println("cube voxels: " + cnt);
    }
}
