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

package abfab3d.creator;

/**
 * Results from a Geometry Kernel generate command.
 *
 * @author Alan Hudson
 */
public class KernelResults {
    /** ReasonCode: The provided params are invalid */
    public static final int INVALID_PARAMS = 1;
    public static final int STRUCTURALLY_UNSOUND = 2;
    public static final int INTERNAL_ERROR = 3;
    public static final int INVALID_SESSION = 4;
    public static final int INTERRUPTED = 5;

    /** The failure code if it failed, 0 otherwise */
    public int failureCode;

    /** The reason the kernel failed */
    private String reason;

    /** Was the operation successful. */
    private boolean success;

    /** The min bounds of the created object */
    private double[] boundsMin;

    /** The max bounds of the created object */
    private double[] boundsMax;

    /** The volume in m ^ 3 or -1 if not calculated */
    private double volume;

    /** The surface area in m ^ 3 or -1 if not calculated */
    private double surfaceArea;

    /** The number of regions removed from the original design */
    private int regionsRemoved;


    public KernelResults(boolean success, int failureCode, String reason,
        double[] boundsMin, double[] boundsMax, double volume, double surfaceArea, int regionsRemoved) {

        this.success = success;

        if (boundsMin != null) {
            this.boundsMin = boundsMin.clone();
        }

        if (boundsMax != null) {
            this.boundsMax = boundsMax.clone();
        }

        this.failureCode = failureCode;
        this.reason = reason;
        this.volume = volume;
        this.surfaceArea = surfaceArea;
        this.regionsRemoved = regionsRemoved;

    }
    public KernelResults(boolean success, int failureCode, String reason,
                         double[] boundsMin, double[] boundsMax) {

        this(success, failureCode, reason, null, null, -1,-1,0);
    }

    public KernelResults(boolean success, double[] boundsMin, double[] boundsMax) {
        this(success, 0, null, boundsMin, boundsMax,-1,-1,0);
    }

    public KernelResults(boolean success, double[] boundsMin, double[] boundsMax, double volume, double surfaceArea, int regionsRemoved) {
        this(success, 0, null, boundsMin, boundsMax,volume,surfaceArea, regionsRemoved);
    }

    public KernelResults(boolean success, double[] boundsMin, double[] boundsMax, double volume, double surfaceArea) {
        this(success, 0, null, boundsMin, boundsMax,volume,surfaceArea,0);
    }

    public KernelResults(int failureCode, String reason) {
        this(false, failureCode, reason, null, null,-1,-1,0);
    }

    /**
     * Was the operation successful.
     *
     * @ret True when successfull
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * Set was the operation successful.
     *
     * @param val True when successfull
     */
    public void setSuccess(boolean val) {
        success = val;
    }

    public double[] getMinBounds() {
        return boundsMin;
    }

    public double[] getMaxBounds() {
        return boundsMax;
    }

    public int getFailureCode() {
        return failureCode;
    }

    public String getReason() {
        return reason;
    }

    public double getVolume() {
        return volume;
    }

    public double getSurfaceArea() {
        return surfaceArea;
    }

    public int getRegionsRemoved() {
        return regionsRemoved;
    }
}