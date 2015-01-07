package gpu;

import abfab3d.grid.NIOAttributeGridByte;

/**
 * Notification of a slice arrival.  Copy the grid if you want to save a copy.
 *
 * @author Alan Hudson
 */
public interface TransferBackListener {
    public void sliceArrived(NIOAttributeGridByte slice, double ymin, double ymax, int idx);
}