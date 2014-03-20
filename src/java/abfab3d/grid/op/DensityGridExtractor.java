package abfab3d.grid.op;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.AttributeOperation;
import abfab3d.grid.Grid;

import static java.lang.Math.round;
import static abfab3d.util.Output.printf;

/**
 * Creates a density grid from a distance grid.  The density grid is useful for further boolean operations.
 * The density grid will be filled in from the inDistanceLevel to the outDistanceLevel.
 *  The values of inDistanceLevel and outDistanceLevel are signed: 
 *      inside of original shape - negative 
 *      outside of original shape - positive 
 *  
 *
 * @author Alan Hudson
 *  @author Vladimir Bulatov
 */
public class DensityGridExtractor implements AttributeOperation {

    /** The value of maximum inside distance to span */
    private double inDistanceValue;

    /** The maximum outside distance to span */
    private double outDistanceValue;

    /** The source distance grid */
    private AttributeGrid distanceGrid;

    /** The maximum inDistance of the distanceGrid */
    private double maxInDistance;

    /** The maximum outDistance of the distanceGrid */
    private double maxOutDistance;

    /** The number of sub-voxel distance levels used in the distanceGrid */
    private int subvoxelResolution;
    
     
    /**
       if inDistanceLevel 
     */
    public DensityGridExtractor(double inDistanceValue, double outDistanceValue, AttributeGrid distanceGrid,
                                double maxInDistance, double maxOutDistance, int subvoxelResolution) {
        this.inDistanceValue = inDistanceValue;
        this.outDistanceValue = outDistanceValue;
        this.distanceGrid = distanceGrid;
        this.maxInDistance = maxInDistance;
        this.maxOutDistance = maxOutDistance;
        this.subvoxelResolution = subvoxelResolution;
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

        int nx = distanceGrid.getWidth();
        int ny = distanceGrid.getHeight();
        int nz = distanceGrid.getDepth();

        // 5 intervals for distance values

        int inDistanceMinus = (int) (inDistanceValue * subvoxelResolution / vs - subvoxelResolution / 2);
        int inDistancePlus = (int) (inDistanceValue * subvoxelResolution / vs + subvoxelResolution / 2);
        int outDistanceMinus = (int) (outDistanceValue * subvoxelResolution / vs - subvoxelResolution / 2);
        int outDistancePlus = (int) (outDistanceValue * subvoxelResolution / vs + subvoxelResolution / 2);

        for(int y=0; y < ny; y++) {
            for(int x=0; x < nx; x++) {
                for(int z=0; z < nz; z++) {
                    long att = (long) (short) distanceGrid.getAttribute(x,y,z);

                    short dest_att;

                    if (att < inDistanceMinus) {
                        dest.setState(x,y,z,Grid.OUTSIDE);
                        //dest.setData(x,y,z,Grid.INSIDE, subvoxelResolution);
                    } else if (att >= inDistanceMinus && att < inDistancePlus) {
                        dest_att = (short) (att - inDistanceMinus);
                        dest.setData(x,y,z, Grid.INSIDE,dest_att);
                    } else if (att >= inDistancePlus && att < outDistanceMinus || att == -Short.MAX_VALUE) {
                        dest_att = (short) subvoxelResolution;
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

}
