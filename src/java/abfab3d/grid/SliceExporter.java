package abfab3d.grid;

import abfab3d.util.LongConverter; 

/**
 * Interface for Slice Exporter
 * 
 */
public interface SliceExporter {
    
    /**
       @param colorMaker converter from voxel value to color 
     */
    public void writeSlices(Grid grid, long maxAttribute, String filePattern, int start, int end, LongConverter colorMaker);
}
