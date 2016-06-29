package abfab3d.grid.op;

import abfab3d.core.AttributeGrid;
import abfab3d.grid.AttributeOperation;

import static java.lang.Math.round;
import static abfab3d.core.Output.printf;

/**
 * Creates a density grid from a distance grid.  The density grid is useful for further boolean operations.
 * The density grid will be filled in from the inDistanceLevel to the outDistanceLevel.
 *  The values of inDistanceLevel and outDistanceLevel are signed: 
 *      inside of the original shape - negative 
 *      outside of the original shape - positive 
 *  
 *
 *  @author Alan Hudson
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
    private double maxInDistanceValue;

    /** The maximum outDistance of the distanceGrid */
    private double maxOutDistanceValue;

    /** The number of sub-voxel distance levels used in the distanceGrid */
    private int subvoxelResolution;
    // this is temporary hack to store max in and out not calculated values of distance grid
    // the actual default values may be different. They may depend in actual distance grid implementation  
    static final int DEFAULT_IN_VALUE = -Short.MAX_VALUE;
    static final int DEFAULT_OUT_VALUE = Short.MAX_VALUE;
     
    /**
       the extractor 
       @param inDistance - signed distance value for internal surface
       @param outDistace - signed distance value for external surface 
       @param distanceGrid - input distance grid to be used for density extraction 
       @param maxInDistanceValue - maximal interior distance actually stored in the distance grid
       @param maxOutDistanceValue - maximal exterior distance actually stored in the distance grid
                       the maxInDistanceValue and maxOutDistanceValue are used only to make correct threshold. 
       if inDistanceVal < maxInDistanceValue the object will have no internal surface
       if outDistanceVal > maxOutDistanceValue the density grid will have no exterior surfaces
       
    */
    public DensityGridExtractor(double inDistanceValue, double outDistanceValue, AttributeGrid distanceGrid,
                                double maxInDistanceValue, double maxOutDistanceValue, int subvoxelResolution) {
        this.inDistanceValue = inDistanceValue;
        this.outDistanceValue = outDistanceValue;
        this.distanceGrid = distanceGrid;
        this.maxInDistanceValue = maxInDistanceValue;
        this.maxOutDistanceValue = maxOutDistanceValue;
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
        int inDistanceMinus = (int) ((inDistanceValue/vs - 0.5)* subvoxelResolution);
        int inDistancePlus = (int) ((inDistanceValue/vs + 0.5)* subvoxelResolution);
        int outDistanceMinus = (int) ((outDistanceValue/vs - 0.5)* subvoxelResolution);
        int outDistancePlus = (int) ((outDistanceValue/vs + 0.5)* subvoxelResolution);
        if(inDistanceValue < maxInDistanceValue) {
            // no interior shell will be generated 
            inDistanceMinus = inDistancePlus = DEFAULT_IN_VALUE;
        } 
        if(outDistanceValue > maxOutDistanceValue){
            // no exterior shell will be generated 
            outDistanceMinus = outDistancePlus = DEFAULT_OUT_VALUE;
        }
        //
        //TODO make it MT 
        //
        for(int y=0; y < ny; y++) {
            for(int x=0; x < nx; x++) {
                for(int z=0; z < nz; z++) {
                    long att = (long) (short) distanceGrid.getAttribute(x,y,z);

                    short dest_att;
                    
                    if (att < inDistanceMinus) {
                        dest.setAttribute(x, y, z, 0);
                    } else if (att >= inDistanceMinus && att < inDistancePlus) {
                        dest_att = (short) (att - inDistanceMinus);
                        dest.setAttribute(x,y,z,dest_att);
                    } else if (att >= inDistancePlus && att < outDistanceMinus || att == DEFAULT_IN_VALUE) {
                        dest_att = (short) subvoxelResolution;
                        dest.setAttribute(x,y,z,dest_att);
                    } else if (att >= outDistanceMinus && att <= outDistancePlus) {
                        dest_att = (short) (outDistancePlus - att);
                        dest.setAttribute(x,y,z,dest_att);
                    } else {
                        dest.setAttribute(x, y, z, 0);
                    }
                }
            }
        }

        return dest;
    }

}
