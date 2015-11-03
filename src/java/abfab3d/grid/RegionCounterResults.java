package abfab3d.grid;

/**
 * Results from a RegionCounter operation
 *
 * @author Alan Hudson
 */
public class RegionCounterResults {
    int numRegions;
    long largestRegionVoxels;
    double largestRegionVolume;
    boolean maxedCount;
    double totalVolume;
    double voxelSize;

    public int getNumRegions() {
        return numRegions;
    }

    public long getLargestRegionVoxels() {
        return largestRegionVoxels;
    }

    public double getLargestRegionVolume() {
        return largestRegionVolume;
    }

    public boolean isMaxedCount() {
        return maxedCount;
    }

    public double getTotalVolume() {
        return totalVolume;
    }

    public double getVoxelSize() {
        return voxelSize;
    }
}
