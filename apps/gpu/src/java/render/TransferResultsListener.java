package render;


/**
 * Notification of a tile arrival.  Copy the results if you want a copy
 *
 * @author Alan Hudson
 */
public interface TransferResultsListener {
    public void tileArrived(RenderTile tile);
}
