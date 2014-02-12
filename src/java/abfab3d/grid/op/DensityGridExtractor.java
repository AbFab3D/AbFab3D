package abfab3d.grid.op;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.AttributeOperation;
import abfab3d.grid.Grid;

import static java.lang.Math.round;
import static abfab3d.util.Output.printf;

/**
 * Creates a density grid from a distance grid.  The density grid is useful for further boolean operations.
 * The density grid will be filled in from the inDistance to the outDistance.
 *
 * @author Alan Hudson
 */
public class DensityGridExtractor implements AttributeOperation {
    /** The maximum inside distance to span */
    private double inDistance;

    /** The maximum outside distance to span */
    private double outDistance;

    /** The source distance grid */
    private AttributeGrid distanceGrid;

    /** The maximum inDistance of the distanceGrid */
    private double maxInDistance;

    /** The maximum outDistance of the distanceGrid */
    private double maxOutDistance;

    /** The number of sub-voxel distance levels used in the distanceGrid */
    private int distanceLevels;

    public DensityGridExtractor(double inDistance, double outDistance, AttributeGrid distanceGrid,
                                double maxInDistance, double maxOutDistance, int distanceLevels) {
        this.inDistance = inDistance;
        this.outDistance = outDistance;
        this.distanceGrid = distanceGrid;
        this.maxInDistance = maxInDistance;
        this.maxOutDistance = maxOutDistance;
        this.distanceLevels = distanceLevels;
    }

    @Override
    public AttributeGrid execute(AttributeGrid dest) {

        if (dest.getWidth() != distanceGrid.getWidth() || dest.getHeight() != distanceGrid.getHeight() ||
                dest.getDepth() != distanceGrid.getDepth()) {
            printf("Distance grid: %d %d %d\n",distanceGrid.getWidth(), distanceGrid.getHeight(),distanceGrid.getDepth());
            printf("Dest grid: %d %d %d\n",dest.getWidth(), dest.getHeight(),dest.getDepth());
            throw new IllegalArgumentException("DistanceGrid and DensityGrid must be the same dimensions");
        }

        double vs = distanceGrid.getVoxelSize();

        int maxInAtt = (int)round(inDistance*distanceLevels/vs);
        int maxOutAtt = -(int)round(outDistance*distanceLevels/vs);

        int nx = distanceGrid.getWidth();
        int ny = distanceGrid.getHeight();
        int nz = distanceGrid.getDepth();

        // 5 intervals for distance values

        int inDistanceMinus = (int) (-inDistance * distanceLevels / vs - distanceLevels / 2);
        int inDistancePlus = (int) (-inDistance * distanceLevels / vs + distanceLevels / 2);
        int outDistanceMinus = (int) (outDistance * distanceLevels / vs - distanceLevels / 2);
        int outDistancePlus = (int) (outDistance * distanceLevels / vs + distanceLevels / 2);

        for(int y=0; y < ny; y++) {
            for(int x=0; x < nx; x++) {
                for(int z=0; z < nz; z++) {
                    long att = (long) (short) distanceGrid.getAttribute(x,y,z);

                    short dest_att;

                    if (att < inDistanceMinus) {
                        dest.setState(x,y,z,Grid.OUTSIDE);
                    } else if (att <= inDistanceMinus && att < inDistancePlus) {
                        dest_att = (short) (att - inDistanceMinus);
                        dest.setData(x,y,z, Grid.INSIDE,dest_att);
                    } else if (att >= inDistancePlus && att < outDistanceMinus || att == -Short.MAX_VALUE) {
                        dest_att = (short) distanceLevels;
                        dest.setData(x,y,z, Grid.INSIDE,dest_att);
                    } else if (att >= outDistanceMinus && att <= outDistancePlus) {
                        dest_att = (short) (outDistancePlus - att);
                        dest.setData(x,y,z, Grid.INSIDE,dest_att);
                    } else {
                        dest.setState(x,y,z,Grid.OUTSIDE);
                    }
                }
            }
        }

        return dest;
    }

    public AttributeGrid executeOld(AttributeGrid dest) {

        if (dest.getWidth() != distanceGrid.getWidth() || dest.getHeight() != distanceGrid.getHeight() ||
                dest.getDepth() != distanceGrid.getDepth()) {
            throw new IllegalArgumentException("DistanceGrid and DensityGrid must be the same dimensions");
        }

        double vs = distanceGrid.getVoxelSize();

        int maxInAtt = (int)round(inDistance*distanceLevels/vs);
        int maxOutAtt = -(int)round(outDistance*distanceLevels/vs);

        int nx = distanceGrid.getWidth();
        int ny = distanceGrid.getHeight();
        int nz = distanceGrid.getDepth();

        // 5 intervals for distance values

        int inDistanceMinus = (int) (inDistance - distanceLevels / 2);
        int inDistancePlus = (int) (inDistance + distanceLevels / 2);
        int outDistanceMinus = (int) (outDistance - distanceLevels / 2);
        int outDistancePlus = (int) (outDistance + distanceLevels / 2);

        for(int y=0; y < ny; y++) {
            for(int x=0; x < nx; x++) {
                for(int z=0; z < nz; z++) {
                    if (y==64 && z==64 && x > 12) {
                        int j= 4;
                    }
                    long att = (long) (short) distanceGrid.getAttribute(x,y,z);

                    if (att >= maxOutAtt && att <= maxInAtt) {
                        short dest_att;

                        if (att >= outDistanceMinus && att < outDistancePlus) {
//                            dest_att = (short) (att - outDistanceMinus);
                            dest_att = (short) (outDistancePlus + att);
                        } else if (att >= inDistanceMinus && att < inDistancePlus) {
                            dest_att = (short) (att + inDistanceMinus);
                        }
                        /*
                        else if (att < outDistancePlus) {
                            dest_att = 0;
                        }
                        */
                        else if (att < inDistanceMinus) {
                            dest_att = 0;
                        }

                        else {
                            dest_att = 127;  // TODO: Stop hardcoding this
                        }

                        dest.setData(x,y,z, Grid.INSIDE,dest_att);
                        /*
                        short dest_att;

                        if (att < inDistanceMinus) {
                            dest_att = 0;
                        } else if (att >= inDistanceMinus && att < inDistancePlus) {
//                            dest_att = (short) (att - inDistanceMinus);
                            dest_att = (short) (inDistanceMinus + att);
                        } else if (att >= inDistancePlus) {
                            dest_att = (short) distanceLevels;
                        } else {
                            dest_att = 0;
                        }
                        dest.setData(x,y,z, Grid.INSIDE,dest_att);
                        */
                    }
                }
            }
        }

        return dest;
    }

}
