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
    // this is temprorary hack to store max in and out not calculated values of distance grid 
    // the actual default values may be different. They may depend in actual distance grid implementation  
    static final int DEFAULT_IN_VALUE = -Short.MAX_VALUE;
    static final int DEFAULT_OUT_VALUE = Short.MAX_VALUE;
     
    /**
       the extractor 
       inDistance - value for internal surface
       outDistace - value for external surface 
       distanceGrid - input distance grid to be used for density extraction 
       maxInDistance - maximal interior distance actualy stored in the distance grid
       maxOutDistance - maximal exterior distance stored in the distance grid
                       these params are used only to make correct threshold. 
       if inDistance < maxInDistance the object will have no internal surface
       if outDistance > maxOutDistance the density grid will have no exterior surfaceas 
       
    */
    public DensityGridExtractor(double inDistance, double outDistance, AttributeGrid distanceGrid,
                                double maxInDistance, double maxOutDistance, int subvoxelResolution) {
        this.inDistanceValue = inDistance;
        this.outDistanceValue = outDistance;
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
        // -INF,  inDistanceMinus, inDistancePlus, outDistanceMinus, outDistancePlus, +INF
        int inDistanceMinus = (int) (inDistanceValue * subvoxelResolution / vs - subvoxelResolution / 2);
        int inDistancePlus = (int) (inDistanceValue * subvoxelResolution / vs + subvoxelResolution / 2);
        int outDistanceMinus = (int) (outDistanceValue * subvoxelResolution / vs - subvoxelResolution / 2);
        int outDistancePlus = (int) (outDistanceValue * subvoxelResolution / vs + subvoxelResolution / 2);
        if(inDistanceValue < maxInDistance) {
            // no interior shell will be generated 
            inDistanceMinus = inDistancePlus = DEFAULT_IN_VALUE;
        } 
        if(outDistanceValue > maxOutDistance){
            // no exterior shell will be generated 
            outDistanceMinus = outDistancePlus = DEFAULT_OUT_VALUE;
        }
        for(int y=0; y < ny; y++) {
            for(int x=0; x < nx; x++) {
                for(int z=0; z < nz; z++) {
                    long att = (long) (short) distanceGrid.getAttribute(x,y,z);

                    short dest_att;
                    
                    if (att < inDistanceMinus) {
                        dest.setData(x,y,z,Grid.OUTSIDE, subvoxelResolution);
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
                        dest.setData(x,y,z,Grid.OUTSIDE, 0);
                    }
                }
            }
        }

        return dest;
    }

}
